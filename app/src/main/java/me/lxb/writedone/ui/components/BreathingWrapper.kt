package me.lxb.writedone.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer

/**
 * Ambient breathing effect: passive GPU-only alpha wrapper.
 *
 * Alpha is read inside `graphicsLayer` (draw-phase), so changes only trigger
 * GPU re-draw — never recomposition. The alpha value is driven at ~10fps by a
 * `LaunchedEffect` + `delay(100)` in HomeScreen, decoupled from Choreographer.
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
    Box(modifier = modifier.graphicsLayer { this.alpha = alpha.value }) {
        content()
    }
}
