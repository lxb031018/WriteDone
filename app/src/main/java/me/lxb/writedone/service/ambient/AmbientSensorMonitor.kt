package me.lxb.writedone.service.ambient

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.sqrt
import kotlin.jvm.Volatile

class AmbientSensorMonitor(
    context: Context,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
) {
    companion object {
        private const val TAG = "AmbientSensor"
        private const val SETTLE_TIME_MS = 2000L
        private const val EVAL_INTERVAL_MS = 500L
        private const val VERTICAL_THRESHOLD = 0.866f
        private const val LEVEL_THRESHOLD = 0.259f
        private const val LOW_PASS_ALPHA = 0.3f
    }

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private val _isReady = MutableStateFlow(false)
    val isReady: StateFlow<Boolean> = _isReady.asStateFlow()

    @Volatile private var gx = 0f
    @Volatile private var gy = 0f
    @Volatile private var gz = 0f
    @Volatile private var hasGravity = false

    private var evalJob: Job? = null

    private val sensorListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            val rawX = event.values[0]
            val rawY = event.values[1]
            val rawZ = event.values[2]
            if (!hasGravity) {
                gx = rawX; gy = rawY; gz = rawZ
                hasGravity = true
            } else {
                gx += LOW_PASS_ALPHA * (rawX - gx)
                gy += LOW_PASS_ALPHA * (rawY - gy)
                gz += LOW_PASS_ALPHA * (rawZ - gz)
            }
        }

        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
    }

    fun start() {
        if (accelerometer == null) {
            Log.w(TAG, "accelerometer is null, aborting")
            return
        }
        sensorManager.registerListener(
            sensorListener, accelerometer, SensorManager.SENSOR_DELAY_NORMAL
        )
        evalJob = scope.launch {
            var stationaryMs = 0L
            while (isActive) {
                delay(EVAL_INTERVAL_MS)
                if (hasGravity && evaluate()) {
                    stationaryMs += EVAL_INTERVAL_MS
                    if (stationaryMs >= SETTLE_TIME_MS && !_isReady.value) {
                        _isReady.value = true
                        Log.i(TAG, "isReady -> true")
                    }
                } else {
                    stationaryMs = 0L
                    if (_isReady.value) {
                        _isReady.value = false
                        Log.i(TAG, "isReady -> false")
                    }
                }
            }
        }
    }

    fun stop() {
        sensorManager.unregisterListener(sensorListener)
        evalJob?.cancel()
        evalJob = null
        hasGravity = false
        _isReady.value = false
    }

    fun dispose() {
        stop()
    }

    private fun evaluate(): Boolean {
        val norm = sqrt(gx * gx + gy * gy + gz * gz)
        if (norm < 0.1f) return false
        return abs(gx) / norm > VERTICAL_THRESHOLD && abs(gy) / norm < LEVEL_THRESHOLD
    }
}
