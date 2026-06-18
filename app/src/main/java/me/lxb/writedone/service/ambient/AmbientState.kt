package me.lxb.writedone.service.ambient

enum class AmbientStatus { Normal, Active }

data class AmbientState(
    val status: AmbientStatus = AmbientStatus.Normal,
    val breathingEnabled: Boolean = false,
)
