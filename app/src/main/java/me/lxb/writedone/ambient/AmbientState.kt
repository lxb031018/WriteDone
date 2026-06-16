package me.lxb.writedone.ambient

enum class AmbientStatus { Normal, Active }

/**
 * 1:1 port of Flutter `lib/features/ambient/ambient_state.dart`.
 *
 * State machine:
 *   - idle → enter() → Active (no breath yet)
 *   - Active → delay(1500ms) → Active + breathingEnabled = true
 *   - Active → exit() → Normal
 */
data class AmbientState(
    val status: AmbientStatus = AmbientStatus.Normal,
    val breathingEnabled: Boolean = false,
)
