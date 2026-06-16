package me.lxb.writedone.ui.theme

import androidx.compose.runtime.State
import androidx.compose.runtime.compositionLocalOf

/** Breathing alpha state (0.15..0.7) during Ambient Mode; null when breathing is off. */
val LocalBreathingAlpha = compositionLocalOf<State<Float>?> { null }
