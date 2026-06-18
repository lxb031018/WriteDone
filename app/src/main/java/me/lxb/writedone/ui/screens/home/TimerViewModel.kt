package me.lxb.writedone.ui.screens.home

import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
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
import me.lxb.writedone.domain.model.TimerMode
import me.lxb.writedone.domain.usecase.TimerUseCase
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
    val mode: TimerMode = TimerMode.Normal,
    val pomodoroCumulativeSeconds: Int = 0,
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

    init {
        viewModelScope.launch {
            timerUseCase.observeTimerMode().collect { mode ->
                _state.update { it.copy(mode = mode) }
            }
        }
        viewModelScope.launch {
            restoreTimer()
        }
    }

    private suspend fun restoreTimer() {
        val startTime = timerUseCase.restoreStartTime() ?: return
        val elapsed = ((System.currentTimeMillis() - startTime) / 1000).toInt()
        val mode = _state.value.mode
        if (mode == TimerMode.Pomodoro) {
            if (elapsed < WORK_SECONDS) {
                scheduleBreakAlarm(startTime)
            } else {
                if (!timerUseCase.getBreakReminderSent()) {
                    NotificationHelper.showBreakReminder(getApplication())
                    timerUseCase.setBreakReminderSent(true)
                }
            }
        }
        startTimer(startTime, elapsed)
    }

    fun start() {
        val now = System.currentTimeMillis()
        val mode = _state.value.mode
        viewModelScope.launch {
            timerUseCase.startTimer()
        }
        if (mode == TimerMode.Pomodoro) {
            scheduleBreakAlarm(now)
        }
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
        cancelBreakAlarm()
        viewModelScope.launch {
            timerUseCase.stopTimer()
        }
        if (current.mode == TimerMode.Pomodoro) {
            val newCumulative = current.pomodoroCumulativeSeconds + current.elapsedSeconds
            _state.update {
                TimerUiState(
                    mode = it.mode,
                    pomodoroCumulativeSeconds = newCumulative,
                    breakButtonVisible = newCumulative >= WORK_SECONDS,
                )
            }
        } else {
            _state.update { TimerUiState(mode = it.mode) }
        }
    }

    fun takeBreak() {
        timerJob?.cancel()
        timerJob = null
        cancelBreakAlarm()
        viewModelScope.launch {
            timerUseCase.takeBreak()
        }
        _state.update { TimerUiState(mode = it.mode) }
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
        if (_state.value.mode == TimerMode.Pomodoro && elapsed >= WORK_SECONDS) {
            viewModelScope.launch {
                if (!timerUseCase.getBreakReminderSent()) {
                    NotificationHelper.showBreakReminder(getApplication())
                    timerUseCase.setBreakReminderSent(true)
                }
            }
        }
        startTimer(startTime, elapsed)
    }

    fun setMode(mode: TimerMode) {
        _state.update { it.copy(mode = mode) }
        viewModelScope.launch {
            timerUseCase.setTimerMode(mode)
        }
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
                    val total = current.pomodoroCumulativeSeconds + newElapsed
                    current.copy(
                        elapsedSeconds = newElapsed,
                        breakButtonVisible = current.mode == TimerMode.Pomodoro && total >= WORK_SECONDS,
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
    }
}
