package me.lxb.writedone.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.remember
import me.lxb.writedone.ui.theme.BreakTexts
import me.lxb.writedone.ui.theme.ZcoolKuaiLeFont as handwritingFont

@Composable
fun TimerCompleteActions(
    onBreak: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val textColor = MaterialTheme.colorScheme.onSurface

    Box(
        modifier = modifier.fillMaxSize().clickable(onClick = onBreak),
        contentAlignment = Alignment.Center,
    ) {
        val breakText = remember { BreakTexts.random() }
        Text(
            text = breakText,
            fontFamily = handwritingFont,
            fontSize = 28.sp,
            lineHeight = 36.sp,
            fontWeight = FontWeight.Normal,
            color = textColor.copy(alpha = 0.2f),
            textAlign = TextAlign.Center,
            modifier = Modifier.offset(x = 2.dp, y = 2.dp),
        )
        Text(
            text = breakText,
            fontFamily = handwritingFont,
            fontSize = 28.sp,
            lineHeight = 36.sp,
            fontWeight = FontWeight.Normal,
            color = textColor,
            textAlign = TextAlign.Center,
        )
    }
}
