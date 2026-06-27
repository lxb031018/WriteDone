package me.lxb.writedone.ui.theme

import androidx.compose.ui.graphics.Color

data class TimerPalette(
    val light: Color,
    val mid: Color,
    val dark: Color,
    val glow: Color,
)

val rococoPalettes = listOf(
    TimerPalette(
        light = Color(0xFFFFD1DC),
        mid = Color(0xFFF4A3B8),
        dark = Color(0xFFD4748C),
        glow = Color(0xFFF4A3B8),
    ),
    TimerPalette(
        light = Color(0xFFC5E0F0),
        mid = Color(0xFF8EBFD8),
        dark = Color(0xFF6A9EB8),
        glow = Color(0xFF8EBFD8),
    ),
    TimerPalette(
        light = Color(0xFFC8E8D0),
        mid = Color(0xFF96CFA8),
        dark = Color(0xFF6EB088),
        glow = Color(0xFF96CFA8),
    ),
    TimerPalette(
        light = Color(0xFFE0C8F0),
        mid = Color(0xFFC49ED8),
        dark = Color(0xFFA078B8),
        glow = Color(0xFFC49ED8),
    ),
    TimerPalette(
        light = Color(0xFFFFD8C8),
        mid = Color(0xFFF0B898),
        dark = Color(0xFFD09878),
        glow = Color(0xFFF0B898),
    ),
    TimerPalette(
        light = Color(0xFFFFE8C8),
        mid = Color(0xFFF0D098),
        dark = Color(0xFFD0B078),
        glow = Color(0xFFF0D098),
    ),
)
