<#
.SYNOPSIS
    Gleam Release Build Script.
    Builds stable release APK variants (Universal, arm64-v8a, armeabi-v7a, x86_64)
    and verifies corresponding release notes in .github/releases/<version>.md.

.PARAMETER Version
    Stable release version string (e.g. 1.0.0). No beta/alpha allowed.
#>

param (
    [string]$Version = ""
)

$ErrorActionPreference = "Stop"

# Navigate to project root
$ProjectRoot = Resolve-Path (Join-Path $PSScriptRoot "..")
Set-Location $ProjectRoot

# 1. Detect or Validate Version
if (-not $Version) {
    # Extract version from AppConstants.kt
    $AppConstantsPath = Join-Path $ProjectRoot "app\src\main\java\com\prism\gleam\AppConstants.kt"
    if (Test-Path $AppConstantsPath) {
        $Match = Select-String -Path $AppConstantsPath -Pattern 'APP_VERSION\s*=\s*"([^"]+)"'
        if ($Match -and $Match.Matches.Groups.Count -gt 1) {
            $Version = $Match.Matches.Groups[1].Value
        }
    }
}

if (-not $Version) {
    $Version = "1.0.0"
}

# Strip any leading 'v'
$CleanVersion = $Version.TrimStart('v', 'V')

# Strict validation: Stable version only, no beta/alpha/rc
if ($CleanVersion -match "(?i)(beta|alpha|rc|preview|dev)") {
    Write-Error "Invalid version '$CleanVersion'. Release builds must be stable versions only (e.g., 1.0.0, 1.1.0). No beta allowed."
    exit 1
}

Write-Host ""
Write-Host "========================================================" -ForegroundColor Cyan
Write-Host "   Gleam Production Release Builder v$CleanVersion" -ForegroundColor Yellow
Write-Host "========================================================" -ForegroundColor Cyan
Write-Host ""

# 2. Check / Prepare Release Notes (.github/releases/<version>.md)
$ReleasesDir = Join-Path $ProjectRoot ".github\releases"
if (-not (Test-Path $ReleasesDir)) {
    New-Item -ItemType Directory -Path $ReleasesDir -Force | Out-Null
}

$ReleaseNotePath = Join-Path $ReleasesDir "$CleanVersion.md"
if (-not (Test-Path $ReleaseNotePath)) {
    Write-Host "[!] Release notes not found at .github/releases/$CleanVersion.md. Generating starter template..." -ForegroundColor Yellow
    $TemplateLines = @(
        "Welcome to Gleam v$CleanVersion ",
        "",
        "Another stable update rolled out fresh from the oven. No beta nonsense, just pure sunlight boost for your screen.",
        "",
        "### What is New in v$CleanVersion",
        "- Performance improvements and sensor synchronization enhancements.",
        "- Updated stability and fluid touch animations.",
        "",
        "### APK Variants Provided",
        "- **Universal:** Works on all Android devices (recommended if unsure).",
        "- **arm64-v8a:** Optimized for modern 64-bit Android smartphones.",
        "- **armeabi-v7a:** For older 32-bit Android phones.",
        "- **x86_64:** For Android emulators and ChromeOS tablets.",
        "",
        "Enjoy the sunshine "
    )
    $TemplateContent = $TemplateLines -join "`n"
    Set-Content -Path $ReleaseNotePath -Value $TemplateContent -Encoding UTF8
    Write-Host "[+] Created release notes: $ReleaseNotePath" -ForegroundColor Green
} else {
    Write-Host "[✓] Found release notes: $ReleaseNotePath" -ForegroundColor Green
}

# 3. Execute Gradle assembleRelease
Write-Host ""
Write-Host "[*] Compiling Release APK Variants via Gradle..." -ForegroundColor Cyan
$Gradlew = Join-Path $ProjectRoot "gradlew.bat"

& $Gradlew assembleRelease
if ($LASTEXITCODE -ne 0) {
    Write-Error "Gradle build failed with exit code $LASTEXITCODE"
    exit $LASTEXITCODE
}

# 4. Package Artifacts into build/releases/<version>/
$ReleaseOutputDir = Join-Path $ProjectRoot "build\releases\$CleanVersion"
if (-not (Test-Path $ReleaseOutputDir)) {
    New-Item -ItemType Directory -Path $ReleaseOutputDir -Force | Out-Null
}

$ApkSourceDir = Join-Path $ProjectRoot "app\build\outputs\apk\release"

$VariantMapping = [ordered]@{
    "app-universal-release.apk"   = "Gleam-$CleanVersion-universal.apk"
    "app-arm64-v8a-release.apk"   = "Gleam-$CleanVersion-arm64-v8a.apk"
    "app-armeabi-v7a-release.apk" = "Gleam-$CleanVersion-armeabi-v7a.apk"
    "app-x86_64-release.apk"      = "Gleam-$CleanVersion-x86_64.apk"
}

Write-Host ""
Write-Host "[*] Packaging Release APK Variants into $ReleaseOutputDir :" -ForegroundColor Green

$PackagedFiles = @()
foreach ($SourceFile in $VariantMapping.Keys) {
    $SourcePath = Join-Path $ApkSourceDir $SourceFile
    $TargetName = $VariantMapping[$SourceFile]
    $TargetPath = Join-Path $ReleaseOutputDir $TargetName

    if (Test-Path $SourcePath) {
        Copy-Item -Path $SourcePath -Destination $TargetPath -Force
        $SizeMb = [math]::Round((Get-Item $TargetPath).Length / 1MB, 2)
        $PackagedFiles += [PSCustomObject]@{
            Variant = $TargetName
            "Size (MB)" = "$SizeMb MB"
            Status = "Ready"
        }
    } else {
        Write-Warning "Source APK not found: $SourcePath"
    }
}

$PackagedFiles | Format-Table -AutoSize

Write-Host ""
Write-Host "========================================================" -ForegroundColor Cyan
Write-Host "   Build Complete for Gleam v$CleanVersion!" -ForegroundColor Green
Write-Host "   Artifacts Directory : $ReleaseOutputDir" -ForegroundColor White
Write-Host "   Release Notes File  : $ReleaseNotePath" -ForegroundColor White
Write-Host "========================================================" -ForegroundColor Cyan
Write-Host ""
