package me.lxb.writedone.ui.components

import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import me.lxb.writedone.R
import me.lxb.writedone.ui.theme.LocalBreathingAlpha
import me.lxb.writedone.ui.theme.LocalTimerPalette
import me.lxb.writedone.ui.screens.home.TimerUiState

/**
 * Timer display component.
 *
 * Layout strategy:
 *   - Container wraps text height — no hardcoded height or weight.
 *   - `TextAutoSize.StepBased` with `maxFontSize` derived from the available
 *     width, so the text naturally scales on both small phones and tablets
 *     without overflowing.
 *
 * Shadows:
 *   - Two `BasicText` layers stacked: the bottom carries the white highlight,
 *     the top carries the drop shadow + gradient fill.
 */
@Composable
fun TimerComponent(
    state: TimerUiState,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var isPressed by remember { mutableStateOf(false) }
    val view = LocalView.current

    val breathingAlpha = LocalBreathingAlpha.current

    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.618f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium,
        ),
        label = "timerScale",
    )

    val timerFont = remember { FontFamily(Font(R.font.cutive_mono)) }

    val palette = LocalTimerPalette.current
    val textBrush = Brush.linearGradient(
        colors = listOf(palette.light, palette.mid, palette.dark),
    )

    val text = formatHms(state.elapsedSeconds)
    val glowColor = palette.glow
    val baseStyle = TextStyle(
        fontFamily = timerFont,
        brush = textBrush,
        fontSize = 200.sp,
        fontWeight = FontWeight.Normal,
    )
    var containerWidthPx by remember { mutableIntStateOf(0) }
    val density = LocalDensity.current
    val maxFontSize = if (containerWidthPx > 0) {
        with(density) { (containerWidthPx * 0.25f / this.density).sp }
    } else {
        200.sp
    }
    val autoSize = remember(maxFontSize) {
        TextAutoSize.StepBased(
            minFontSize = 12.sp,
            maxFontSize = maxFontSize,
            stepSize = 1.sp,
        )
    }
    val pressScaleModifier = Modifier
        .fillMaxWidth()
        .graphicsLayer {
            scaleX = pressScale
            scaleY = pressScale
        }

    Box(
        modifier = modifier
            .onSizeChanged { containerWidthPx = it.width }
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                    },
                    onTap = {
                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                        onToggle()
                    },
                )
            },
        contentAlignment = Alignment.Center,
    ) {
        // Layer 0: text edge glow (landscape ambient mode)
        if (breathingAlpha != null) {
            BasicText(
                text = text,
                style = TextStyle(
                    fontFamily = timerFont,
                    color = glowColor,
                    fontSize = 200.sp,
                    fontWeight = FontWeight.Normal,
                    shadow = Shadow(
                        color = glowColor,
                        offset = Offset.Zero,
                        blurRadius = 24f,
                    ),
                ),
                autoSize = autoSize,
                softWrap = false,
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        scaleX = pressScale
                        scaleY = pressScale
                        alpha = breathingAlpha.value * 0.6f
                    },
            )
        }

        BreathingWrapper(enabled = true, alpha = breathingAlpha) {
            // Layer 1: white highlight shadow (Flutter `_shadows[1]`)
            BasicText(
                text = text,
                style = baseStyle.copy(
                    shadow = Shadow(
                        offset = Offset(-1.5f, -1.5f),
                        blurRadius = 1f,
                        color = Color(0x99FFFFFF),
                    ),
                ),
                autoSize = autoSize,
                softWrap = false,
                modifier = pressScaleModifier,
            )
            // Layer 2: main drop shadow + gradient fill (Flutter `_shadows[0]`)
            BasicText(
                text = text,
                style = baseStyle.copy(
                    shadow = Shadow(
                        offset = Offset(0f, 6f),
                        blurRadius = 12f,
                        color = Color(0x4D000000),
                    ),
                ),
                autoSize = autoSize,
                softWrap = false,
                modifier = pressScaleModifier,
            )
        }
    }
}

internal fun formatHms(totalSeconds: Int): String {
    val h = totalSeconds / 3600
    val m = (totalSeconds % 3600) / 60
    val s = totalSeconds % 60
    return "%02d:%02d:%02d".format(h, m, s)
}
