package me.lxb.writedone.ui.animation

import androidx.compose.animation.core.Easing
import kotlin.math.PI
import kotlin.math.pow
import kotlin.math.sin

object SinCubicEasing : Easing {
    override fun transform(fraction: Float): Float {
        val t = fraction * (PI.toFloat() / 2f)
        return sin(t).pow(3)
    }
}
