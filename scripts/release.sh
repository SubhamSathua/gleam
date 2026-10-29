#!/usr/bin/env bash
set -euo pipefail

# Gleam Release Build Script (Bash)
# Builds stable release APK variants (Universal, arm64-v8a, armeabi-v7a, x86_64)

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"
cd "${PROJECT_ROOT}"

VERSION="${1:-}"

if [ -z "${VERSION}" ]; then
  # Try reading from AppConstants.kt
  APP_CONST_FILE="${PROJECT_ROOT}/app/src/main/java/com/prism/gleam/AppConstants.kt"
  if [ -f "${APP_CONST_FILE}" ]; then
    VERSION=$(grep -oE 'APP_VERSION\s*=\s*"[^"]+"' "${APP_CONST_FILE}" | cut -d'"' -f2 || true)
  fi
fi

VERSION="${VERSION:-1.0.0}"
CLEAN_VER="${VERSION#v}"

# Validate stable version (no beta/alpha)
if echo "${CLEAN_VER}" | grep -iE "(beta|alpha|rc|preview|dev)"; then
  echo "Error: Version '${CLEAN_VER}' is not allowed. Release builds must be stable versions only." >&2
  exit 1
fi

echo "Building Gleam stable release v${CLEAN_VER}..."

# Ensure release notes exist
RELEASE_NOTE_PATH="${PROJECT_ROOT}/.github/releases/${CLEAN_VER}.md"
if [ ! -f "${RELEASE_NOTE_PATH}" ]; then
  echo "Notice: Creating default release note at ${RELEASE_NOTE_PATH}"
  mkdir -p "${PROJECT_ROOT}/.github/releases"
  cat <<EOF > "${RELEASE_NOTE_PATH}"
Welcome to Gleam v${CLEAN_VER} 🥳

Another stable update rolled out fresh from the oven. No beta nonsense, just pure sunlight boost for your screen.

### What is New in v${CLEAN_VER}
- Performance improvements and sensor synchronization enhancements.
- Updated stability and fluid touch animations.

### APK Variants Provided
- **Universal:** Works on all Android devices (recommended if unsure).
- **arm64-v8a:** Optimized for modern 64-bit Android smartphones.
- **armeabi-v7a:** For older 32-bit Android phones.
- **x86_64:** For Android emulators and ChromeOS tablets.

Enjoy the sunshine 🫡
EOF
fi

chmod +x ./gradlew
./gradlew assembleRelease

OUT_DIR="${PROJECT_ROOT}/build/releases/${CLEAN_VER}"
mkdir -p "${OUT_DIR}"
APK_SRC="${PROJECT_ROOT}/app/build/outputs/apk/release"

cp "${APK_SRC}/app-universal-release.apk" "${OUT_DIR}/Gleam-${CLEAN_VER}-universal.apk"
cp "${APK_SRC}/app-arm64-v8a-release.apk" "${OUT_DIR}/Gleam-${CLEAN_VER}-arm64-v8a.apk"
cp "${APK_SRC}/app-armeabi-v7a-release.apk" "${OUT_DIR}/Gleam-${CLEAN_VER}-armeabi-v7a.apk"
cp "${APK_SRC}/app-x86_64-release.apk" "${OUT_DIR}/Gleam-${CLEAN_VER}-x86_64.apk"

echo "Release package created successfully at ${OUT_DIR}:"
ls -lh "${OUT_DIR}"
