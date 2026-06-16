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
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.sp
import me.lxb.writedone.R
import me.lxb.writedone.ui.theme.AppColors
import me.lxb.writedone.ui.theme.LocalBreathingAlpha
import me.lxb.writedone.viewmodel.TimerMode
import me.lxb.writedone.viewmodel.TimerUiState

/**
 * 1:1 port of `lib/features/timer/timer_widget.dart`.
 *
 * Layout strategy:
 *   - Flutter: `FittedBox(fit: BoxFit.fitWidth)` performs **layout-time** scaling
 *     so the text never overflows the parent. We reproduce this with
 *     `BasicText(autoSize = TextAutoSize.StepBased(...))`, which is the
 *     layout-time auto-shrink API in Compose Foundation 1.10+.
 *   - The 120.dp height constraint lives on the parent `HorizontalPager` in
 *     [TimerInputCard] (matches Flutter's `SizedBox(height: 120)`).
 *
 * Shadows:
 *   - Flutter source defines two `Shadow`s (drop + highlight). Compose's
 *     `TextStyle.shadow` is single-valued, so we stack two `BasicText`s —
 *     the bottom layer carries the white highlight, the top carries the
 *     drop shadow + gradient fill.
 */
@Composable
fun TimerComponent(
    state: TimerUiState,
    mode: TimerMode,
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

    val handleeFont = remember {
        FontFamily(
            Font(
                googleFont = GoogleFont("Handlee"),
                fontProvider = GoogleFont.Provider(
                    providerAuthority = "com.google.android.gms.fonts",
                    providerPackage = "com.google.android.gms",
                    certificates = R.array.com_google_android_gms_fonts_certs,
                ),
            ),
        )
    }

    val textBrush = if (mode == TimerMode.Pomodoro) {
        Brush.linearGradient(
            colors = listOf(AppColors.pomodoroLight, AppColors.pomodoro, AppColors.pomodoroDark),
        )
    } else {
        Brush.linearGradient(
            colors = listOf(Color(0xFF5C4D3E), Color(0xFF3C3530), Color(0xFF1E1814)),
        )
    }

    val text = formatHms(state.elapsedSeconds)
    val baseStyle = TextStyle(
        fontFamily = handleeFont,
        brush = textBrush,
        fontSize = 200.sp,
        fontWeight = FontWeight.Normal,
    )
    val autoSize = remember {
        TextAutoSize.StepBased(
            minFontSize = 12.sp,
            maxFontSize = 200.sp,
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
            .fillMaxWidth()
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
                modifier = pressScaleModifier,
            )
        }
    }
}

fun formatHms(totalSeconds: Int): String {
    val h = totalSeconds / 3600
    val m = (totalSeconds % 3600) / 60
    val s = totalSeconds % 60
    return "%02d:%02d:%02d".format(h, m, s)
}
