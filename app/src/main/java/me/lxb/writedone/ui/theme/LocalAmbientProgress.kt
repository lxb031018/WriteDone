package me.lxb.writedone.ui.theme

import androidx.compose.runtime.compositionLocalOf

/** Theme transition progress t ∈ [0, 1]. 0 = light, 1 = dark. */
val LocalAmbientProgress = compositionLocalOf { 0f }
