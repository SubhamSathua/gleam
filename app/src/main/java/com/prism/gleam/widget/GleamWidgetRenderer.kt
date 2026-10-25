package com.prism.gleam.widget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.prism.gleam.AppConstants
import com.prism.gleam.R
import kotlin.math.min
import kotlin.math.roundToInt

enum class WidgetSize {
    SIZE_1X1,
    SIZE_2X2, // 2 width x 1 height pill capsule
    SIZE_4X4
}

object GleamWidgetRenderer {

    /**
     * Builds a mathematically safe, non-inverting squircle / rounded rectangle path:
     * - If radius >= half min dimension (Capsule): renders a true semicircular capsule pill without pinch artifacts.
     * - If smoothing == 0: renders a precise circular arc rounded rectangle.
     * - If smoothing > 0: renders continuous curvature G2 squircle corners where tangents never cross over.
     */
    fun createSquirclePath(rect: RectF, radius: Float, smoothingPercent: Int): Path {
        val path = Path()
        val maxR = min(rect.width(), rect.height()) / 2f
        val r = radius.coerceIn(0f, maxR)

        if (r <= 0f) {
            path.addRect(rect, Path.Direction.CW)
            return path
        }

        // For Capsule (radius at or near half-height), preserve true semicircular pill geometry
        val isCapsule = (r >= maxR * 0.95f)
        val smoothing = (smoothingPercent.coerceIn(0, 100) / 100f)

        if (isCapsule || smoothing <= 0.05f) {
            path.addRoundRect(rect, r, r, Path.Direction.CW)
            return path
        }

        // Corner smoothing for rectangular / squircle corners:
        // Borrow a fraction of the available straight edge without exceeding the midpoint
        val availableEdgeX = (rect.width() - 2 * r).coerceAtLeast(0f) / 2f
        val availableEdgeY = (rect.height() - 2 * r).coerceAtLeast(0f) / 2f

        val extX = (smoothing * min(r * 0.45f, availableEdgeX))
        val extY = (smoothing * min(r * 0.45f, availableEdgeY))

        val dX = r + extX
        val dY = r + extY

        val tension = (0.55228475f - (smoothing * 0.12f))
        val cpOffsetX = dX * tension
        val cpOffsetY = dY * tension

        val l = rect.left
        val t = rect.top
        val rgt = rect.right
        val b = rect.bottom

        path.moveTo(l + dX, t)

        // 1. Top Edge to Top-Right Corner
        path.lineTo(rgt - dX, t)
        path.cubicTo(
            rgt - dX + cpOffsetX, t,
            rgt, t + dY - cpOffsetY,
            rgt, t + dY
        )

        // 2. Right Edge to Bottom-Right Corner
        path.lineTo(rgt, b - dY)
        path.cubicTo(
            rgt, b - dY + cpOffsetY,
            rgt - dX + cpOffsetX, b,
            rgt - dX, b
        )

        // 3. Bottom Edge to Bottom-Left Corner
        path.lineTo(l + dX, b)
        path.cubicTo(
            l + dX - cpOffsetX, b,
            l, b - dY + cpOffsetY,
            l, b - dY
        )

        // 4. Left Edge to Top-Left Corner
        path.lineTo(l, t + dY)
        path.cubicTo(
            l, t + dY - cpOffsetY,
            l + dX - cpOffsetX, t,
            l + dX, t
        )

        path.close()
        return path
    }

    /**
     * Renders a pixel-perfect, responsive widget bitmap:
     * - 1x1: Always 1:1 perfect square, centered in the cell.
     * - 2x1 (2x2 slot): 2 width x 1 height capsule pill, sleek aspect ratio, centered vertically.
     * - 4x4: Always 1:1 perfect square, centered in the cell.
     */
    fun renderWidgetBitmap(
        context: Context,
        widgetSize: WidgetSize,
        config: WidgetConfig,
        isPoweredOn: Boolean,
        widthPx: Int,
        heightPx: Int,
        drawOutline: Boolean = false
    ): Bitmap {
        val w = widthPx.coerceAtLeast(100)
        val h = heightPx.coerceAtLeast(100)
        val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val density = context.resources.displayMetrics.density
        val isDark = config.theme.isDark(context)
        val alphaFraction = (config.opacityPercent.coerceIn(0, 100) / 100f)
        val alphaInt = (alphaFraction * 255).roundToInt().coerceIn(0, 255)

        val margin = 3f * density

        // Determine card bounds with strict geometry guarantees:
        val cardRect: RectF = when (widgetSize) {
            WidgetSize.SIZE_1X1, WidgetSize.SIZE_4X4 -> {
                // Strictly 1:1 Square
                val cardSide = min(w - 2 * margin, h - 2 * margin)
                val left = (w - cardSide) / 2f
                val top = (h - cardSide) / 2f
                RectF(left, top, left + cardSide, top + cardSide)
            }

            WidgetSize.SIZE_2X2 -> {
                // 2 Width x 1 Height Sleek Capsule Pill:
                // Height is constrained to an optimal pill aspect ratio (~2.15:1 to 2.3:1)
                val availableW = w - 2 * margin
                val availableH = h - 2 * margin
                val idealH = (availableW / 2.2f)
                val actualH = min(availableH, idealH)
                val actualW = availableW
                val left = margin
                val top = (h - actualH) / 2f
                RectF(left, top, left + actualW, top + actualH)
            }
        }

        // Determine calibrated corner radius:
        // - Stop 3 (>= 65dp): True Capsule / Full Circle
        // - Stop 1 (<= 18dp): Minimal subtle curve
        // - Stop 2 (Rounded / Default 30dp): Proportional rounded corners preserving clean flat straight edges
        val minDimension = min(cardRect.width(), cardRect.height())
        val maxCapsuleRadius = minDimension / 2f

        val effectiveRadiusPx = when {
            config.radiusDp >= 65 -> {
                maxCapsuleRadius
            }
            config.radiusDp <= 18 -> {
                when (widgetSize) {
                    WidgetSize.SIZE_1X1 -> 7f * density
                    WidgetSize.SIZE_2X2 -> 8f * density
                    WidgetSize.SIZE_4X4 -> 15f * density
                }.coerceAtMost(maxCapsuleRadius)
            }
            else -> {
                // Rounded (Default): maintains straight edges, prevents 1x1 & 2x1 from becoming circular
                when (widgetSize) {
                    WidgetSize.SIZE_1X1 -> 14f * density
                    WidgetSize.SIZE_2X2 -> 16f * density
                    WidgetSize.SIZE_4X4 -> 30f * density
                }.coerceAtMost(maxCapsuleRadius * 0.72f)
            }
        }

        // 1. Draw Background Path (Plain or Minimal Gradient)
        // In 1x1, corner smoothing is explicitly removed as requested
        val effectiveSmoothing = if (widgetSize == WidgetSize.SIZE_1X1) 0 else config.smoothingPercent
        val backgroundPath = createSquirclePath(cardRect, effectiveRadiusPx, effectiveSmoothing)
        val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            isDither = true
        }

        when (config.colorStyle) {
            WidgetColorStyle.PLAIN -> {
                val baseColor = if (isDark) Color.BLACK else Color.WHITE
                backgroundPaint.color = Color.argb(
                    alphaInt,
                    Color.red(baseColor),
                    Color.green(baseColor),
                    Color.blue(baseColor)
                )
            }
            WidgetColorStyle.MINIMAL_GRADIENT -> {
                val startColorInt: Int
                val endColorInt: Int
                if (!isDark) {
                    // Light: FFFFFF Top -> B9B9B9 Btm
                    startColorInt = Color.argb(alphaInt, 0xFF, 0xFF, 0xFF)
                    endColorInt = Color.argb(alphaInt, 0xB9, 0xB9, 0xB9)
                } else {
                    // Dark: 242424 Top -> 000000 Btm
                    startColorInt = Color.argb(alphaInt, 0x24, 0x24, 0x24)
                    endColorInt = Color.argb(alphaInt, 0x00, 0x00, 0x00)
                }
                backgroundPaint.shader = LinearGradient(
                    cardRect.centerX(), cardRect.top,
                    cardRect.centerX(), cardRect.bottom,
                    startColorInt, endColorInt,
                    Shader.TileMode.CLAMP
                )
            }
        }

        canvas.drawPath(backgroundPath, backgroundPaint)

        // Draw 50% opacity outline if requested (e.g. for preview or widget boundaries)
        if (drawOutline) {
            val outlinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeWidth = 1.5f * density
                color = if (isDark) {
                    Color.argb(128, 255, 255, 255) // 50% white
                } else {
                    Color.argb(128, 0, 0, 0)       // 50% black
                }
            }
            canvas.drawPath(backgroundPath, outlinePaint)
        }

        // 2. Select Faceted Sun Icon
        val iconResId = if (isPoweredOn) {
            R.drawable.ic_sun_faceted_on
        } else {
            if (isDark) R.drawable.ic_sun_faceted_light else R.drawable.ic_sun_faceted_dark
        }
        val iconDrawable = ContextCompat.getDrawable(context, iconResId)

        // 3. Load Urbanist Typeface
        val urbanistTypeface = try {
            ResourcesCompat.getFont(context, R.font.urbanist) ?: Typeface.DEFAULT_BOLD
        } catch (_: Exception) {
            Typeface.DEFAULT_BOLD
        }

        val primaryTextColor = if (isDark) Color.WHITE else Color.parseColor("#121418")
        val secondaryTextColor = if (isDark) Color.parseColor("#B0B6C2") else Color.parseColor("#5A6272")

        // 4. Render Widget Content based on WidgetSize
        when (widgetSize) {
            WidgetSize.SIZE_1X1 -> {
                // Centered 1:1 Square Faceted Sun
                if (iconDrawable != null) {
                    val iconSize = (cardRect.width() * 0.58f).roundToInt()
                    val iconLeft = (cardRect.centerX() - iconSize / 2f).roundToInt()
                    val iconTop = (cardRect.centerY() - iconSize / 2f).roundToInt()
                    iconDrawable.setBounds(iconLeft, iconTop, iconLeft + iconSize, iconTop + iconSize)
                    iconDrawable.draw(canvas)
                }
            }

            WidgetSize.SIZE_2X2 -> {
                // 2 Width x 1 Height Pill Card: Icon on Left + Two Lines of Text on Right
                val cardH = cardRect.height()
                val iconSize = (cardH * 0.54f).roundToInt()

                // Icon positioned on left with comfortable capsule cap margin
                val iconLeft = (cardRect.left + cardH * 0.28f).roundToInt()
                val iconTop = (cardRect.centerY() - iconSize / 2f).roundToInt()

                if (iconDrawable != null) {
                    iconDrawable.setBounds(iconLeft, iconTop, iconLeft + iconSize, iconTop + iconSize)
                    iconDrawable.draw(canvas)
                }

                // Generous spacing between icon and text, moving text towards the right
                val iconRight = iconLeft + iconSize
                val gap = (cardH * 0.30f).coerceAtLeast(18f * density)
                val textLeft = iconRight + gap
                val textBlockCenterY = cardRect.centerY()

                // Typography sizes scale proportionally
                val titleSize = (cardH * 0.23f).coerceAtLeast(14f * density)
                val subtextSize = (cardH * 0.15f).coerceAtLeast(10f * density)

                val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = primaryTextColor
                    textSize = titleSize
                    typeface = Typeface.create(urbanistTypeface, Typeface.BOLD)
                }

                val subtextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = secondaryTextColor
                    textSize = subtextSize
                    typeface = Typeface.create(urbanistTypeface, Typeface.NORMAL)
                }

                val titleText = AppConstants.WIDGET_DEFAULT_TITLE
                val subText = if (isPoweredOn) AppConstants.WIDGET_STATUS_ON else AppConstants.WIDGET_STATUS_OFF

                val titleMetrics = titlePaint.fontMetrics
                val subMetrics = subtextPaint.fontMetrics

                val titleHeight = titleMetrics.descent - titleMetrics.ascent
                val subHeight = subMetrics.descent - subMetrics.ascent
                val spacing = 2.5f * density

                val totalTextHeight = titleHeight + spacing + subHeight
                val titleY = textBlockCenterY - (totalTextHeight / 2f) - titleMetrics.ascent
                val subY = titleY + titleMetrics.descent + spacing - subMetrics.ascent

                canvas.drawText(titleText, textLeft, titleY, titlePaint)
                canvas.drawText(subText, textLeft, subY, subtextPaint)
            }

            WidgetSize.SIZE_4X4 -> {
                // Centered 1:1 Square Layout: Big Icon + Upper Title + Upper Subtext
                val cardSide = cardRect.width()
                val iconSize = (cardSide * 0.44f).roundToInt()

                val titleSize = (cardSide * 0.10f).coerceAtLeast(15f * density)
                val subtextSize = (cardSide * 0.08f).coerceAtLeast(12f * density)

                val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = primaryTextColor
                    textSize = titleSize
                    typeface = Typeface.create(urbanistTypeface, Typeface.BOLD)
                    textAlign = Paint.Align.CENTER
                }

                val subtextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = secondaryTextColor
                    textSize = subtextSize
                    typeface = Typeface.create(urbanistTypeface, Typeface.NORMAL)
                    textAlign = Paint.Align.CENTER
                }

                val titleText = AppConstants.WIDGET_DEFAULT_TITLE.uppercase()
                val subText = if (isPoweredOn) AppConstants.WIDGET_STATUS_ON_UPPER else AppConstants.WIDGET_STATUS_OFF_UPPER

                val titleMetrics = titlePaint.fontMetrics
                val subMetrics = subtextPaint.fontMetrics

                val titleH = titleMetrics.descent - titleMetrics.ascent
                val subH = subMetrics.descent - subMetrics.ascent
                val spacing = 6f * density

                val totalBlockHeight = iconSize + (cardSide * 0.08f) + titleH + spacing + subH
                val blockStartY = cardRect.centerY() - (totalBlockHeight / 2f)

                val iconLeft = (cardRect.centerX() - iconSize / 2f).roundToInt()
                val iconTop = blockStartY.roundToInt()

                if (iconDrawable != null) {
                    iconDrawable.setBounds(iconLeft, iconTop, iconLeft + iconSize, iconTop + iconSize)
                    iconDrawable.draw(canvas)
                }

                val titleY = iconTop + iconSize + (cardSide * 0.08f) - titleMetrics.ascent
                val subY = titleY + titleMetrics.descent + spacing - subMetrics.ascent

                canvas.drawText(titleText, cardRect.centerX(), titleY, titlePaint)
                canvas.drawText(subText, cardRect.centerX(), subY, subtextPaint)
            }
        }

        return bitmap
    }
}
