package me.lxb.writedone.ambient

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 1:1 port of Flutter `lib/features/ambient/ambient_controller.dart`.
 *
 * State machine:
 *   - enter() → Active (no breath)
 *   - Active → delay(1500ms) → Active + breathingEnabled = true
 *   - exit() → Normal
 */
class AmbientController {
    companion object {
        private const val BREATHING_START_DELAY_MS = 1500L
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var timerJob: Job? = null

    private val _state = MutableStateFlow(AmbientState())
    val state: StateFlow<AmbientState> = _state.asStateFlow()

    /** Immediately enter Active state; breathing flag flips on after 1.5s. */
    fun enter() {
        timerJob?.cancel()
        _state.value = AmbientState(status = AmbientStatus.Active)
        timerJob = scope.launch {
            delay(BREATHING_START_DELAY_MS)
            _state.value = AmbientState(
                status = AmbientStatus.Active,
                breathingEnabled = true,
            )
        }
    }

    /** Exit back to Normal, clearing all timers. */
    fun exit() {
        timerJob?.cancel()
        _state.value = AmbientState()
    }

    fun dispose() {
        timerJob?.cancel()
    }
}
