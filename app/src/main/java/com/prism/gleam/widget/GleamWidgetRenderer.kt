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
    SIZE_2X2,
    SIZE_4X4
}

object GleamWidgetRenderer {

    /**
     * Builds a squircle / smoothed rounded rectangle path based on corner radius and smoothing percentage.
     * - smoothing = 0: standard circular arc
     * - smoothing = 50: standard G2 squircle curvature
     * - smoothing = 100: hyper-elliptical continuous curvature
     */
    fun createSquirclePath(rect: RectF, radius: Float, smoothingPercent: Int): Path {
        val path = Path()
        val maxR = min(rect.width(), rect.height()) / 2f
        val r = radius.coerceIn(0f, maxR)

        if (r <= 0f) {
            path.addRect(rect, Path.Direction.CW)
            return path
        }

        val smoothing = (smoothingPercent.coerceIn(0, 100) / 100f)

        if (smoothing <= 0.05f) {
            path.addRoundRect(rect, r, r, Path.Direction.CW)
            return path
        }

        // Smooth cubic Bézier continuous curvature transition
        val p = smoothing * 0.55f // Bézier tension parameter
        val offset = r * (1f + p)
        val l = rect.left
        val t = rect.top
        val rgt = rect.right
        val b = rect.bottom

        path.moveTo(l + offset, t)

        // Top edge to top-right corner
        path.lineTo(rgt - offset, t)
        path.cubicTo(
            rgt - offset + (r * p), t,
            rgt, t + offset - (r * p),
            rgt, t + offset
        )

        // Right edge to bottom-right corner
        path.lineTo(rgt, b - offset)
        path.cubicTo(
            rgt, b - offset + (r * p),
            rgt - offset + (r * p), b,
            rgt - offset, b
        )

        // Bottom edge to bottom-left corner
        path.lineTo(l + offset, b)
        path.cubicTo(
            l + offset - (r * p), b,
            l, b - offset + (r * p),
            l, b - offset
        )

        // Left edge to top-left corner
        path.lineTo(l, t + offset)
        path.cubicTo(
            l, t + offset - (r * p),
            l + offset - (r * p), t,
            l + offset, t
        )

        path.close()
        return path
    }

    /**
     * Renders a pixel-perfect, responsive widget bitmap matching Figma designs.
     */
    fun renderWidgetBitmap(
        context: Context,
        widgetSize: WidgetSize,
        config: WidgetConfig,
        isPoweredOn: Boolean,
        widthPx: Int,
        heightPx: Int
    ): Bitmap {
        val w = widthPx.coerceAtLeast(120)
        val h = heightPx.coerceAtLeast(120)
        val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val density = context.resources.displayMetrics.density
        val isDark = config.theme.isDark(context)
        val alphaFraction = (config.opacityPercent.coerceIn(0, 100) / 100f)
        val alphaInt = (alphaFraction * 255).roundToInt().coerceIn(0, 255)

        // Outer margin so shadows/launcher grid won't touch the very edge
        val margin = 3f * density
        val cardRect = RectF(margin, margin, w - margin, h - margin)

        // Determine corner radius:
        // Capsule (70dp) on 2x2 should produce full semicircular pill ends
        val baseRadiusPx = config.radiusDp * density
        val effectiveRadiusPx = if (config.radiusDp >= 65) {
            min(cardRect.width(), cardRect.height()) / 2f
        } else {
            baseRadiusPx.coerceAtMost(min(cardRect.width(), cardRect.height()) / 2f)
        }

        // 1. Draw Background Path (Plain or Minimal Gradient)
        val backgroundPath = createSquirclePath(cardRect, effectiveRadiusPx, config.smoothingPercent)
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
                // Centered Faceted Sun
                if (iconDrawable != null) {
                    val iconSize = (min(cardRect.width(), cardRect.height()) * 0.58f).roundToInt()
                    val iconLeft = (cardRect.centerX() - iconSize / 2f).roundToInt()
                    val iconTop = (cardRect.centerY() - iconSize / 2f).roundToInt()
                    iconDrawable.setBounds(iconLeft, iconTop, iconLeft + iconSize, iconTop + iconSize)
                    iconDrawable.draw(canvas)
                }
            }

            WidgetSize.SIZE_2X2 -> {
                // Horizontal Pill/Card: Icon on Left + Two Lines of Text on Right
                val cardH = cardRect.height()
                val iconSize = (cardH * 0.54f).roundToInt()

                val horizontalPadding = cardH * 0.22f
                val iconLeft = (cardRect.left + horizontalPadding).roundToInt()
                val iconTop = (cardRect.centerY() - iconSize / 2f).roundToInt()

                if (iconDrawable != null) {
                    iconDrawable.setBounds(iconLeft, iconTop, iconLeft + iconSize, iconTop + iconSize)
                    iconDrawable.draw(canvas)
                }

                val textLeft = iconLeft + iconSize + (cardH * 0.14f)
                val textRightAvailable = cardRect.right - horizontalPadding
                val textBlockCenterY = cardRect.centerY()

                // Typography sizes scale proportionally
                val titleSize = (cardH * 0.22f).coerceAtLeast(14f * density)
                val subtextSize = (cardH * 0.145f).coerceAtLeast(10f * density)

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

                // Measure text baseline
                val titleMetrics = titlePaint.fontMetrics
                val subMetrics = subtextPaint.fontMetrics

                val titleHeight = titleMetrics.descent - titleMetrics.ascent
                val subHeight = subMetrics.descent - subMetrics.ascent
                val spacing = 2f * density

                val totalTextHeight = titleHeight + spacing + subHeight
                val titleY = textBlockCenterY - (totalTextHeight / 2f) - titleMetrics.ascent
                val subY = titleY + titleMetrics.descent + spacing - subMetrics.ascent

                canvas.drawText(titleText, textLeft, titleY, titlePaint)
                canvas.drawText(subText, textLeft, subY, subtextPaint)
            }

            WidgetSize.SIZE_4X4 -> {
                // Centered Square Layout: Big Icon + Upper Title + Upper Subtext
                val cardMinDim = min(cardRect.width(), cardRect.height())
                val iconSize = (cardMinDim * 0.44f).roundToInt()

                val titleSize = (cardMinDim * 0.10f).coerceAtLeast(15f * density)
                val subtextSize = (cardMinDim * 0.08f).coerceAtLeast(12f * density)

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

                val totalBlockHeight = iconSize + (cardMinDim * 0.08f) + titleH + spacing + subH
                val blockStartY = cardRect.centerY() - (totalBlockHeight / 2f)

                val iconLeft = (cardRect.centerX() - iconSize / 2f).roundToInt()
                val iconTop = blockStartY.roundToInt()

                if (iconDrawable != null) {
                    iconDrawable.setBounds(iconLeft, iconTop, iconLeft + iconSize, iconTop + iconSize)
                    iconDrawable.draw(canvas)
                }

                val titleY = iconTop + iconSize + (cardMinDim * 0.08f) - titleMetrics.ascent
                val subY = titleY + titleMetrics.descent + spacing - subMetrics.ascent

                canvas.drawText(titleText, cardRect.centerX(), titleY, titlePaint)
                canvas.drawText(subText, cardRect.centerX(), subY, subtextPaint)
            }
        }

        return bitmap
    }
}
