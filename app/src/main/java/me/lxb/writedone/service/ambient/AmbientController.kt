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

    fun enter() {
        Log.i(TAG, "enter")
        sensorJob?.cancel()
        breathingJob?.cancel()
        _state.value = AmbientState()
        sensorMonitor.start()
        var firstFalse = true
        sensorJob = scope.launch {
            sensorMonitor.isReady.collect { ready ->
                if (ready) {
                    firstFalse = true
                    _state.value = AmbientState(status = AmbientStatus.Active)
                    Log.i(TAG, "status -> Active")
                    delay(BREATHING_START_DELAY_MS)
                    breathingJob?.cancel()
                    breathingJob = launch {
                        _state.value = AmbientState(
                            status = AmbientStatus.Active,
                            breathingEnabled = true,
                        )
                        Log.i(TAG, "breathing -> true")
                    }
                } else {
                    if (firstFalse) {
                        firstFalse = false
                        return@collect
                    }
                    breathingJob?.cancel()
                    _state.value = AmbientState()
                    Log.i(TAG, "status -> Normal")
                }
            }
        }
    }

    fun exit() {
        Log.i(TAG, "exit")
        sensorJob?.cancel()
        breathingJob?.cancel()
        sensorMonitor.stop()
        _state.value = AmbientState()
    }

    fun dispose() {
        exit()
    }
}
