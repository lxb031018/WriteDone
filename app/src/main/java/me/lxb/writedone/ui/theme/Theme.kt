package me.lxb.writedone.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp

private val LightColorScheme = lightColorScheme(
    primary = AppColors.accent,
    onPrimary = Color.White,
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

/**
 * 1:1 port of Flutter `_LishiAppState._onThemeAnimTick`/`ThemeData.lerp(light, dark, t)`.
 *
 * At `ambientProgress = 0`, the base colorScheme is used (light or dark per system).
 * At `ambientProgress = 1`, all Material 3 color slots are lerped to dark.
 * Intermediate values produce a smooth crossfade.
 */
private fun ColorScheme.lerpedTo(target: ColorScheme, fraction: Float): ColorScheme {
    if (fraction <= 0f) return this
    if (fraction >= 1f) return target
    return copy(
        primary = lerp(primary, target.primary, fraction),
        onPrimary = lerp(onPrimary, target.onPrimary, fraction),
        primaryContainer = lerp(primaryContainer, target.primaryContainer, fraction),
        secondary = lerp(secondary, target.secondary, fraction),
        onSecondary = lerp(onSecondary, target.onSecondary, fraction),
        secondaryContainer = lerp(secondaryContainer, target.secondaryContainer, fraction),
        tertiary = lerp(tertiary, target.tertiary, fraction),
        onTertiary = lerp(onTertiary, target.onTertiary, fraction),
        tertiaryContainer = lerp(tertiaryContainer, target.tertiaryContainer, fraction),
        background = lerp(background, target.background, fraction),
        onBackground = lerp(onBackground, target.onBackground, fraction),
        surface = lerp(surface, target.surface, fraction),
        onSurface = lerp(onSurface, target.onSurface, fraction),
        surfaceVariant = lerp(surfaceVariant, target.surfaceVariant, fraction),
        onSurfaceVariant = lerp(onSurfaceVariant, target.onSurfaceVariant, fraction),
        outline = lerp(outline, target.outline, fraction),
        outlineVariant = lerp(outlineVariant, target.outlineVariant, fraction),
        inverseSurface = lerp(inverseSurface, target.inverseSurface, fraction),
        inverseOnSurface = lerp(inverseOnSurface, target.inverseOnSurface, fraction),
        inversePrimary = lerp(inversePrimary, target.inversePrimary, fraction),
        error = lerp(error, target.error, fraction),
        onError = lerp(onError, target.onError, fraction),
        errorContainer = lerp(errorContainer, target.errorContainer, fraction),
        onErrorContainer = lerp(onErrorContainer, target.onErrorContainer, fraction),
        scrim = lerp(scrim, target.scrim, fraction),
        surfaceTint = lerp(surfaceTint, target.surfaceTint, fraction),
    )
}

@Composable
fun WriteDoneTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    ambientProgress: Float = 0f,
    content: @Composable () -> Unit,
) {
    val baseScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val colorScheme = baseScheme.lerpedTo(DarkColorScheme, ambientProgress.coerceIn(0f, 1f))

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content,
    )
}
