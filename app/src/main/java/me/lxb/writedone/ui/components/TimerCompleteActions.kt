package me.lxb.writedone.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.lxb.writedone.R
import me.lxb.writedone.ui.theme.AppColors
import me.lxb.writedone.ui.theme.LocalAmbientProgress

@Composable
fun TimerCompleteActions(
    onSkip: () -> Unit,
    onBreak: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val t = LocalAmbientProgress.current
    val textColor = lerp(AppColors.text, AppColors.darkText, t)

    val handwritingFont = FontFamily(
        Font(
            googleFont = GoogleFont("ZCOOL KuaiLe"),
            fontProvider = GoogleFont.Provider(
                providerAuthority = "com.google.android.gms.fonts",
                providerPackage = "com.google.android.gms",
                certificates = R.array.com_google_android_gms_fonts_certs,
            ),
        ),
    )

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "跳过",
                fontFamily = handwritingFont,
                fontSize = 18.sp,
                fontWeight = FontWeight.Normal,
                color = textColor.copy(alpha = 0.4f),
                modifier = Modifier.clickable(onClick = onSkip),
            )
            Spacer(Modifier.width(24.dp))
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(20.dp)
                    .background(textColor.copy(alpha = 0.15f)),
            )
            Spacer(Modifier.width(24.dp))
            Text(
                text = "休息一下",
                fontFamily = handwritingFont,
                fontSize = 18.sp,
                fontWeight = FontWeight.Normal,
                color = textColor,
                modifier = Modifier.clickable(onClick = onBreak),
            )
        }
    }
}
