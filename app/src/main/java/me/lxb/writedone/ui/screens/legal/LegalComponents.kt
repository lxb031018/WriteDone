package me.lxb.writedone.ui.screens.legal

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import me.lxb.writedone.ui.theme.AppColors

val boldSpan = SpanStyle(fontWeight = FontWeight.Bold)
val italicSpan = SpanStyle(fontStyle = FontStyle.Italic)
val codeSpan = SpanStyle(
    fontFamily = FontFamily.Monospace,
    color = AppColors.accentDeep,
    background = AppColors.card,
)

private val baseStyle = TextStyle(
    fontSize = 14.sp,
    lineHeight = 1.7.em,
    color = AppColors.text,
)

@Composable
fun LP(text: String) {
    Text(
        text = text,
        style = baseStyle,
        modifier = Modifier.padding(vertical = 4.dp),
    )
}

@Composable
fun LPRich(content: AnnotatedString) {
    Text(
        text = content,
        style = baseStyle,
        modifier = Modifier.padding(vertical = 4.dp),
    )
}

@Composable
fun LH2(text: String) {
    Text(
        text = text,
        style = TextStyle(
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = AppColors.text,
        ),
        modifier = Modifier.padding(top = 18.dp, bottom = 8.dp),
    )
}

@Composable
fun LH3(text: String) {
    Text(
        text = text,
        style = TextStyle(
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = AppColors.text,
        ),
        modifier = Modifier.padding(top = 12.dp, bottom = 4.dp),
    )
}

@Composable
fun LBullet(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 4.dp, top = 2.dp, bottom = 2.dp),
        horizontalArrangement = Arrangement.Start,
    ) {
        Text(
            text = "\u2022",
            style = baseStyle,
            modifier = Modifier.padding(end = 8.dp),
        )
        Text(text = text, style = baseStyle)
    }
}

@Composable
fun LBulletRich(content: AnnotatedString) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 4.dp, top = 2.dp, bottom = 2.dp),
        horizontalArrangement = Arrangement.Start,
    ) {
        Text(
            text = "\u2022",
            style = baseStyle,
            modifier = Modifier.padding(end = 8.dp),
        )
        Text(text = content, style = baseStyle)
    }
}

@Composable
fun LRule() {
    Spacer(Modifier.height(12.dp))
    HorizontalDivider(thickness = 1.dp, color = AppColors.border)
    Spacer(Modifier.height(12.dp))
}

@Composable
fun LContactLine(label: String, value: String) {
    Row(
        modifier = Modifier.padding(start = 4.dp, top = 4.dp, bottom = 4.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Text(
            text = "$label:",
            style = baseStyle.copy(color = AppColors.textMuted),
            modifier = Modifier.padding(end = 8.dp),
        )
        Text(
            text = value,
            style = baseStyle.copy(color = AppColors.accentDeep),
        )
    }
}
