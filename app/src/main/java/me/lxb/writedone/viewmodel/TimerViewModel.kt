package me.lxb.writedone.viewmodel

import android.app.Application
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

enum class TimerStatus { Idle, Running }
enum class TimerMode { Normal, Pomodoro }

data class TimerUiState(
    val status: TimerStatus = TimerStatus.Idle,
    val elapsedSeconds: Int = 0,
    val startTimeMillis: Long? = null,
    val mode: TimerMode = TimerMode.Normal,
    val lockScreenEnabled: Boolean = true,
)

class TimerViewModel(application: Application) : AndroidViewModel(application) {
    private val timerStateRepo = TimerStateRepository(application)
    private val settingsRepo = SettingsRepository(application)

    private val _state = MutableStateFlow(TimerUiState())
    val state: StateFlow<TimerUiState> = _state.asStateFlow()

    private var timerJob: Job? = null

    init {
        viewModelScope.launch {
            settingsRepo.lockScreenEnabled.collect { enabled ->
                _state.update { it.copy(lockScreenEnabled = enabled) }
            }
        }
        viewModelScope.launch {
            settingsRepo.timerModePomodoro.collect { pomodoro ->
                _state.update { it.copy(mode = if (pomodoro) TimerMode.Pomodoro else TimerMode.Normal) }
            }
        }
        restoreTimer()
    }

    private fun restoreTimer() {
        val startTime = timerStateRepo.loadStartTime() ?: return
        val elapsed = ((System.currentTimeMillis() - startTime) / 1000).toInt()
        startTimer(startTime, elapsed)
    }

    fun start() {
        val now = System.currentTimeMillis()
        viewModelScope.launch {
            timerStateRepo.saveStartTime(now)
        }
        startTimer(now, 0)
    }

    fun stop() {
        timerJob?.cancel()
        timerJob = null
        viewModelScope.launch {
            timerStateRepo.clear()
        }
        _state.update {
            TimerUiState(mode = it.mode, lockScreenEnabled = it.lockScreenEnabled)
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

    fun setLockScreenEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepo.setLockScreenEnabled(enabled)
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

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}
