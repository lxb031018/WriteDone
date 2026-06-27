package me.lxb.writedone.ui.screens.home

import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.lxb.writedone.domain.usecase.TimerUseCase
import me.lxb.writedone.service.TimerForegroundService
import me.lxb.writedone.service.notification.AlarmReceiver
import me.lxb.writedone.service.notification.NotificationHelper
import javax.inject.Inject

enum class TimerStatus { Idle, Running }

data class StopEvent(
    val elapsedSeconds: Int,
    val startTimeMillis: Long,
)

data class TimerUiState(
    val status: TimerStatus = TimerStatus.Idle,
    val elapsedSeconds: Int = 0,
    val startTimeMillis: Long? = null,
    val cumulativeSeconds: Int = 0,
    val breakButtonVisible: Boolean = false,
)

@HiltViewModel
class TimerViewModel @Inject constructor(
    application: Application,
    private val timerUseCase: TimerUseCase,
) : AndroidViewModel(application) {

    private val _state = MutableStateFlow(TimerUiState())
    val state: StateFlow<TimerUiState> = _state.asStateFlow()

    private val _stopEvents = MutableSharedFlow<StopEvent>(extraBufferCapacity = 1)
    val stopEvents: SharedFlow<StopEvent> = _stopEvents.asSharedFlow()

    companion object {
        private const val WORK_SECONDS = 1500
    }

    private var timerJob: Job? = null
    private var pomodoroSessionActive = false
    private var compositingSuspended = false

    init {
        viewModelScope.launch {
            restoreTimer()
        }
    }

    private suspend fun restoreTimer() {
        val startTime = timerUseCase.restoreStartTime() ?: return
        val elapsed = ((System.currentTimeMillis() - startTime) / 1000).toInt()
        startForegroundService(startTime)
        if (elapsed < WORK_SECONDS) {
            scheduleBreakAlarm(startTime)
        } else {
            if (!timerUseCase.getBreakReminderSent()) {
                NotificationHelper.showBreakReminder(getApplication())
                timerUseCase.setBreakReminderSent(true)
            }
        }
        startTimer(startTime, elapsed)
    }

    fun setCompositingSuspended(suspended: Boolean) {
        compositingSuspended = suspended
        if (suspended && _state.value.status == TimerStatus.Running) {
            timerJob?.cancel()
            timerJob = null
        } else if (!suspended && _state.value.status == TimerStatus.Running) {
            val startTime = _state.value.startTimeMillis ?: return
            val elapsed = ((System.currentTimeMillis() - startTime) / 1000).toInt()
            startTimer(startTime, elapsed)
        }
    }

    fun setAmbientMode(active: Boolean) {
        if (active && _state.value.status == TimerStatus.Running && !pomodoroSessionActive) {
            pomodoroSessionActive = true
            val startTime = _state.value.startTimeMillis ?: return
            scheduleBreakAlarm(startTime)
            val elapsed = ((System.currentTimeMillis() - startTime) / 1000).toInt()
            _state.update {
                it.copy(
                    breakButtonVisible = it.cumulativeSeconds + elapsed >= WORK_SECONDS,
                )
            }
        }
    }

    fun start() {
        val now = System.currentTimeMillis()
        pomodoroSessionActive = false
        viewModelScope.launch {
            timerUseCase.startTimer()
        }
        startForegroundService(now)
        startTimer(now, 0)
    }

    fun stop() {
        val current = _state.value
        _stopEvents.tryEmit(StopEvent(
            elapsedSeconds = current.elapsedSeconds,
            startTimeMillis = current.startTimeMillis ?: System.currentTimeMillis(),
        ))
        timerJob?.cancel()
        timerJob = null
        stopForegroundService()
        cancelBreakAlarm()
        viewModelScope.launch {
            timerUseCase.stopTimer()
        }
        if (pomodoroSessionActive) {
            pomodoroSessionActive = false
            val newCumulative = current.cumulativeSeconds + current.elapsedSeconds
            _state.update {
                TimerUiState(
                    cumulativeSeconds = newCumulative,
                    breakButtonVisible = newCumulative >= WORK_SECONDS,
                )
            }
        } else {
            _state.update { TimerUiState() }
        }
    }

    fun takeBreak() {
        val current = _state.value
        _stopEvents.tryEmit(StopEvent(
            elapsedSeconds = current.elapsedSeconds,
            startTimeMillis = current.startTimeMillis ?: System.currentTimeMillis(),
        ))
        timerJob?.cancel()
        timerJob = null
        stopForegroundService()
        cancelBreakAlarm()
        pomodoroSessionActive = false
        viewModelScope.launch {
            timerUseCase.takeBreak()
        }
        _state.update { TimerUiState() }
    }

    fun toggleTimer() {
        val current = _state.value
        if (current.status == TimerStatus.Idle) {
            start()
        } else {
            stop()
        }
    }

    fun onPause() {
        timerJob?.cancel()
        timerJob = null
    }

    fun onResume() {
        val startTime = _state.value.startTimeMillis ?: return
        if (_state.value.status != TimerStatus.Running) return
        val elapsed = ((System.currentTimeMillis() - startTime) / 1000).toInt()
        if (pomodoroSessionActive && elapsed >= WORK_SECONDS) {
            viewModelScope.launch {
                if (!timerUseCase.getBreakReminderSent()) {
                    NotificationHelper.showBreakReminder(getApplication())
                    timerUseCase.setBreakReminderSent(true)
                }
            }
        }
        startTimer(startTime, elapsed)
    }

    private fun startTimer(startTimeMillis: Long, initialElapsed: Int) {
        timerJob?.cancel()
        _state.update {
            it.copy(
                status = TimerStatus.Running,
                elapsedSeconds = initialElapsed,
                startTimeMillis = startTimeMillis,
            )
        }
        timerJob = viewModelScope.launch {
            var breakNotified = false
            while (true) {
                delay(1000L)
                _state.update { current ->
                    val startMs = current.startTimeMillis ?: System.currentTimeMillis()
                    val newElapsed = ((System.currentTimeMillis() - startMs) / 1000).toInt()
                    val total = current.cumulativeSeconds + newElapsed
                    current.copy(
                        elapsedSeconds = newElapsed,
                        breakButtonVisible = pomodoroSessionActive && total >= WORK_SECONDS,
                    )
                }
                if (_state.value.breakButtonVisible && !breakNotified) {
                    breakNotified = true
                    NotificationHelper.showBreakReminder(getApplication())
                    timerUseCase.setBreakReminderSent(true)
                }
            }
        }
    }

    private fun startForegroundService(startTimeMillis: Long) {
        val intent = Intent(getApplication(), TimerForegroundService::class.java).apply {
            action = TimerForegroundService.ACTION_START
            putExtra(TimerForegroundService.EXTRA_START_TIME, startTimeMillis)
        }
        ContextCompat.startForegroundService(getApplication(), intent)
    }

    private fun stopForegroundService() {
        val intent = Intent(getApplication(), TimerForegroundService::class.java).apply {
            action = TimerForegroundService.ACTION_STOP
        }
        getApplication<Application>().startService(intent)
    }

    private fun scheduleBreakAlarm(startTimeMillis: Long) {
        val triggerTime = startTimeMillis + WORK_SECONDS * 1000L
        val alarmManager = getApplication<Application>().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(getApplication(), AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            getApplication(),
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && alarmManager.canScheduleExactAlarms()) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent,
            )
        } else {
            alarmManager.setAlarmClock(
                AlarmManager.AlarmClockInfo(triggerTime, null),
                pendingIntent,
            )
        }
    }

    private fun cancelBreakAlarm() {
        val alarmManager = getApplication<Application>().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(getApplication(), AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            getApplication(),
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        stopForegroundService()
    }
}
