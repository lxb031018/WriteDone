package me.lxb.writedone.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf

private val LightColorScheme = lightColorScheme(
    primary = AppColors.accent,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    primaryContainer = AppColors.accentLight,
    secondary = AppColors.textSecondary,
    tertiary = AppColors.green,
    background = AppColors.bg,
    surface = AppColors.card,
    onBackground = AppColors.text,
    onSurface = AppColors.text,
    outline = AppColors.border,
)

private val DarkColorScheme = darkColorScheme(
    primary = AppColors.darkAccent,
    onPrimary = AppColors.darkText,
    primaryContainer = AppColors.darkAccentLight,
    secondary = AppColors.darkTextSecondary,
    tertiary = AppColors.darkGreen,
    background = AppColors.darkBg,
    surface = AppColors.darkCard,
    onBackground = AppColors.darkText,
    onSurface = AppColors.darkText,
    outline = AppColors.darkBorder,
)

@Composable
fun WriteDoneTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content,
    )
}
