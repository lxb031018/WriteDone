package me.lxb.writedone.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import me.lxb.writedone.ui.theme.BreakTexts
import me.lxb.writedone.ui.theme.ZcoolKuaiLeFont as handwritingFont

@Composable
fun RainbowBreakOverlay(
    brush: Brush,
    onClick: () -> Unit,
    background: Color = Color.Transparent,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(background)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = remember { BreakTexts.random() },
            style = TextStyle(
                fontFamily = handwritingFont,
                fontSize = 48.sp,
                lineHeight = 64.sp,
                fontWeight = FontWeight.Normal,
                brush = brush,
            ),
            textAlign = TextAlign.Center,
        )
    }
}
