package me.lxb.writedone.service.ambient

enum class AmbientStatus { Normal, Active }

enum class AmbientDisplayMode { Breathing, Blackout }

data class AmbientState(
    val status: AmbientStatus = AmbientStatus.Normal,
    val breathingEnabled: Boolean = false,
    val displayMode: AmbientDisplayMode = AmbientDisplayMode.Breathing,
)
