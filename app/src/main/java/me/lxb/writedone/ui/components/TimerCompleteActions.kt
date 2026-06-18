package me.lxb.writedone.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import me.lxb.writedone.R
import me.lxb.writedone.ui.theme.AppColors
import me.lxb.writedone.ui.theme.ZcoolKuaiLeFont as handwritingFont
import me.lxb.writedone.ui.theme.LocalAmbientProgress

@Composable
fun TimerCompleteActions(
    onBreak: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val ambientProgress = LocalAmbientProgress.current
    val textColor = lerp(AppColors.text, AppColors.darkText, ambientProgress)

    Box(
        modifier = modifier.fillMaxSize().clickable(onClick = onBreak),
        contentAlignment = Alignment.Center,
    ) {
        val breakText = stringResource(R.string.timer_complete_break)
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
