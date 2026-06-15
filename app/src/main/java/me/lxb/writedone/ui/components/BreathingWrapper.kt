package me.lxb.writedone.ui.components

import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer

/**
 * Ambient breathing effect: 4s ease-in-out-sine, alpha 0.15 ↔ 0.7.
 *
 * 1:1 port of Flutter source's `shared/widgets/breathing_wrapper.dart` which wraps
 * children in an `Opacity(opacity: animation.value, child: ...)`. We use
 * `graphicsLayer { alpha = ... }` for the same composition-layer semantics
 * (no layout pass on each frame, only draw).
 */
@Composable
fun BreathingWrapper(
    enabled: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    if (!enabled) {
        content()
        return
    }
    val transition = rememberInfiniteTransition(label = "breathing")
    val alpha by transition.animateFloat(
        initialValue = 0.15f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "breath",
    )
    Box(modifier = modifier.graphicsLayer { this.alpha = alpha }) {
        content()
    }
}
