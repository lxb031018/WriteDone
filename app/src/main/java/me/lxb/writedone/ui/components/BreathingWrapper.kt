package me.lxb.writedone.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer

/**
 * Ambient breathing effect: passive alpha wrapper.
 *
 * Drives its alpha from [alpha] (a `State<Float>` from [me.lxb.writedone.theme.LocalBreathingAlpha])
 * instead of an internal animation, so the home-screen-level `Animatable` controls all wrappers
 * in lockstep (matches Flutter `BreathingWrapper(animation: breathing)`).
 *
 * `graphicsLayer { alpha = ... }` is the Compose analogue of Flutter's `Opacity` + `RepaintBoundary`:
 * composite-layer alpha, no layout pass.
 */
@Composable
fun BreathingWrapper(
    enabled: Boolean,
    alpha: State<Float>? = null,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    if (!enabled || alpha == null) {
        content()
        return
    }
    val a = alpha.value
    Box(modifier = modifier.graphicsLayer { this.alpha = a }) {
        content()
    }
}
