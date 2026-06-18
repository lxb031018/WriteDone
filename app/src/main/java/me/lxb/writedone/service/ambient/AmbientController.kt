package me.lxb.writedone.service.ambient

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AmbientController(
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
) {
    companion object {
        private const val BREATHING_START_DELAY_MS = 1500L
    }
    private var timerJob: Job? = null

    private val _state = MutableStateFlow(AmbientState())
    val state: StateFlow<AmbientState> = _state.asStateFlow()

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

    fun exit() {
        timerJob?.cancel()
        _state.value = AmbientState()
    }

    fun dispose() {
        timerJob?.cancel()
    }
}
