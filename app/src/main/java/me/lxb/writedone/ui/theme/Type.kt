package me.lxb.writedone.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import me.lxb.writedone.R

val HandleeFont = FontFamily(Font(R.font.handlee))
val ZcoolKuaiLeFont = FontFamily(Font(R.font.zcool_kuaile))

val AppTypography = Typography(
    bodyLarge = TextStyle(
        fontFamily = ZcoolKuaiLeFont,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
)
