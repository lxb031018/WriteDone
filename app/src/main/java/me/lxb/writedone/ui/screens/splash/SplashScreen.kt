package me.lxb.writedone.ui.screens.splash

import androidx.compose.foundation.background
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
import androidx.compose.ui.unit.sp
import me.lxb.writedone.ui.theme.AppColors
import kotlin.random.Random

@Composable
fun SplashScreen(modifier: Modifier = Modifier) {
    val bgColor = remember { AppColors.morandiPalette[Random.nextInt(AppColors.morandiPalette.size)] }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(bgColor),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "Ciallo～(∠·ω< )⌒★",
            style = TextStyle(
                fontSize = 28.sp,
                fontWeight = FontWeight.Medium,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFFFF6B6B),
                        Color(0xFFFFE66D),
                        Color(0xFF69DB7C),
                        Color(0xFF74C0FC),
                        Color(0xFFDA77F2),
                    ),
                ),
            ),
        )
    }
}
