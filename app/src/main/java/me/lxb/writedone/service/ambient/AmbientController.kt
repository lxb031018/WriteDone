package me.lxb.writedone.service.ambient

import android.content.Context
import android.util.Log
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
    context: Context,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
) {
    companion object {
        private const val TAG = "AmbientCtrl"
        private const val BREATHING_START_DELAY_MS = 1500L
    }

    private val sensorMonitor = AmbientSensorMonitor(context, scope)
    private var sensorJob: Job? = null
    private var breathingJob: Job? = null

    private val _state = MutableStateFlow(AmbientState())
    val state: StateFlow<AmbientState> = _state.asStateFlow()

    fun enter(breathingLampEnabled: Boolean) {
        Log.i(TAG, "enter($breathingLampEnabled)")
        val mode = if (breathingLampEnabled) AmbientDisplayMode.Breathing else AmbientDisplayMode.Blackout
        sensorJob?.cancel()
        breathingJob?.cancel()
        _state.value = AmbientState(displayMode = mode)
        sensorMonitor.start()
        var firstFalse = true
        sensorJob = scope.launch {
            sensorMonitor.isReady.collect { ready ->
                if (ready) {
                    firstFalse = true
                    _state.value = AmbientState(
                        status = AmbientStatus.Active,
                        displayMode = mode,
                    )
                    Log.i(TAG, "status -> Active, mode=$mode")
                    if (mode == AmbientDisplayMode.Breathing) {
                        delay(BREATHING_START_DELAY_MS)
                        breathingJob?.cancel()
                        breathingJob = launch {
                            _state.value = _state.value.copy(breathingEnabled = true)
                            Log.i(TAG, "breathing -> true")
                        }
                    }
                } else {
                    if (firstFalse) {
                        firstFalse = false
                        return@collect
                    }
                    breathingJob?.cancel()
                    _state.value = AmbientState(displayMode = _state.value.displayMode)
                    Log.i(TAG, "status -> Normal")
                }
            }
        }
    }

    fun updateDisplayMode(breathingLampEnabled: Boolean) {
        if (_state.value.status != AmbientStatus.Active) return
        val mode = if (breathingLampEnabled) AmbientDisplayMode.Breathing else AmbientDisplayMode.Blackout
        if (mode == AmbientDisplayMode.Blackout) {
            breathingJob?.cancel()
            _state.value = AmbientState(
                status = AmbientStatus.Active,
                displayMode = AmbientDisplayMode.Blackout,
            )
            Log.i(TAG, "mode -> Blackout")
        } else {
            _state.value = AmbientState(
                status = AmbientStatus.Active,
                displayMode = AmbientDisplayMode.Breathing,
            )
            Log.i(TAG, "mode -> Breathing")
            breathingJob?.cancel()
            breathingJob = scope.launch {
                delay(BREATHING_START_DELAY_MS)
                _state.value = _state.value.copy(breathingEnabled = true)
                Log.i(TAG, "breathing -> true")
            }
        }
    }

    fun exit() {
        Log.i(TAG, "exit")
        sensorJob?.cancel()
        breathingJob?.cancel()
        sensorMonitor.stop()
        _state.value = AmbientState(displayMode = _state.value.displayMode)
    }

    fun dispose() {
        exit()
    }
}
