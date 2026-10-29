package com.prism.gleam.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prism.gleam.ui.theme.GleamTheme
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun LiveLuxCounterBadge(
    lux: Float,
    isPoweredOn: Boolean,
    modifier: Modifier = Modifier
) {
    val colors = GleamTheme.colors

    val infiniteTransition = rememberInfiniteTransition(label = "LivePulseTransition")
    val dotAlpha by infiniteTransition.animateFloat(
        initialValue = 0.45f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "LiveDotAlpha"
    )

    val formattedLux = remember(lux) {
        val intLux = lux.roundToInt()
        "%,d lx".format(Locale.US, intLux)
    }

    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(colors.cardBackground)
            .border(
                width = 1.dp,
                color = if (isPoweredOn) colors.primaryAccent.copy(alpha = 0.35f) else colors.trackGroove,
                shape = CircleShape
            )
            .padding(horizontal = 16.dp, vertical = 7.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            // Live pulsing dot indicator
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .clip(CircleShape)
                    .background(
                        if (isPoweredOn) {
                            colors.primaryAccent.copy(alpha = dotAlpha)
                        } else {
                            colors.textMuted.copy(alpha = 0.6f)
                        }
                    )
            )

            Text(
                text = formattedLux,
                color = if (isPoweredOn) colors.textTitle else colors.textSub,
                fontSize = 13.5.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.2.sp
            )
        }
    }
}
