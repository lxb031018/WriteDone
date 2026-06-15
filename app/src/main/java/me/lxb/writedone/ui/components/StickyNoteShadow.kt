package me.lxb.writedone.ui.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * 1:1 port of Flutter `sticky_note.dart` 3-layer boxShadow.
 *
 *   Layer 1 (ambient halo):   bgColor @ α=0.35, blur=24, offset (0, 8)
 *   Layer 2 (drop shadow):    0x26000000,            blur=14, offset (2, 4)
 *   Layer 3 (contact):        0x0A000000,            blur=4,  offset (0, 1)
 *
 * Compose `Modifier.shadow` accepts only one ambient + one spot per call,
 * so we stack three to approximate the three Flutter layers.
 */
fun Modifier.stickyNoteShadow(bgColor: Color): Modifier {
    val shape = RoundedCornerShape(4.dp)
    return this
        // Layer 3 (innermost contact)
        .shadow(
            elevation = 1.dp,
            shape = shape,
            clip = false,
            ambientColor = Color(0x0A000000),
            spotColor = Color(0x0A000000),
        )
        // Layer 2 (directional drop)
        .shadow(
            elevation = 4.dp,
            shape = shape,
            clip = false,
            ambientColor = Color(0x26000000),
            spotColor = Color(0x26000000),
        )
        // Layer 1 (colored ambient halo)
        .shadow(
            elevation = 8.dp,
            shape = shape,
            clip = false,
            ambientColor = bgColor.copy(alpha = 0.35f),
            spotColor = Color(0x26000000),
        )
}
