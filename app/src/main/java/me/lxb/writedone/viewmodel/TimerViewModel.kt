package me.lxb.writedone.viewmodel

import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.lxb.writedone.data.repository.SettingsRepository
import me.lxb.writedone.data.repository.TimerStateRepository
import me.lxb.writedone.notification.AlarmReceiver
import me.lxb.writedone.notification.NotificationHelper

enum class TimerStatus { Idle, Running }
enum class TimerMode { Normal, Pomodoro }

data class TimerUiState(
    val status: TimerStatus = TimerStatus.Idle,
    val elapsedSeconds: Int = 0,
    val startTimeMillis: Long? = null,
    val mode: TimerMode = TimerMode.Normal,
)

class TimerViewModel(application: Application) : AndroidViewModel(application) {
    private val timerStateRepo = TimerStateRepository(application)
    private val settingsRepo = SettingsRepository(application)

    private val _state = MutableStateFlow(TimerUiState())
    val state: StateFlow<TimerUiState> = _state.asStateFlow()

    private var timerJob: Job? = null

    init {
        viewModelScope.launch {
            settingsRepo.timerModePomodoro.collect { pomodoro ->
                _state.update { it.copy(mode = if (pomodoro) TimerMode.Pomodoro else TimerMode.Normal) }
            }
        }
        restoreTimer()
        NotificationHelper.createChannel(getApplication())
    }

    private fun restoreTimer() {
        val startTime = timerStateRepo.loadStartTime() ?: return
        val elapsed = ((System.currentTimeMillis() - startTime) / 1000).toInt()
        val mode = _state.value.mode
        if (mode == TimerMode.Pomodoro) {
            val workSeconds = 20
            if (elapsed < workSeconds) {
                scheduleBreakAlarm(startTime)
            } else {
                if (!timerStateRepo.loadBreakReminderSent()) {
                    NotificationHelper.createChannel(getApplication())
                    NotificationHelper.showBreakReminder(getApplication())
                    viewModelScope.launch {
                        timerStateRepo.saveBreakReminderSent(true)
                    }
                }
            }
        }
        startTimer(startTime, elapsed)
    }

    fun start() {
        val now = System.currentTimeMillis()
        val mode = _state.value.mode
        viewModelScope.launch {
            timerStateRepo.saveStartTime(now)
            timerStateRepo.saveBreakReminderSent(false)
            timerStateRepo.saveBreakReminderPendingRepeat(false)
        }
        if (mode == TimerMode.Pomodoro) {
            scheduleBreakAlarm(now)
        }
        startTimer(now, 0)
    }

    fun stop() {
        timerJob?.cancel()
        timerJob = null
        cancelBreakAlarm()
        viewModelScope.launch {
            timerStateRepo.clear()
        }
        _state.update {
            TimerUiState(mode = it.mode)
        }
    }

    fun toggleTimer() {
        val current = _state.value
        if (current.status == TimerStatus.Idle) {
            start()
        } else {
            stop()
        }
    }

    fun onResume() {
        val startTime = _state.value.startTimeMillis ?: return
        if (_state.value.status != TimerStatus.Running) return
        val elapsed = ((System.currentTimeMillis() - startTime) / 1000).toInt()
        _state.update { it.copy(elapsedSeconds = elapsed) }
    }

    fun setMode(mode: TimerMode) {
        syncMode(mode)
    }

    fun syncMode(mode: TimerMode) {
        _state.update { it.copy(mode = mode) }
        viewModelScope.launch {
            settingsRepo.setTimerModePomodoro(mode == TimerMode.Pomodoro)
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
            while (true) {
                delay(1000L)
                _state.update { it.copy(elapsedSeconds = it.elapsedSeconds + 1) }
            }
        }
    }

    private fun scheduleBreakAlarm(startTimeMillis: Long) {
        val triggerTime = startTimeMillis + 20 * 1000L
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
