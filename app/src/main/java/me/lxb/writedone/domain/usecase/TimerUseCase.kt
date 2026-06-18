package me.lxb.writedone.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import me.lxb.writedone.domain.model.TimerMode
import me.lxb.writedone.domain.repository.SettingsRepository
import me.lxb.writedone.domain.repository.TimerStateRepository
import javax.inject.Inject

class TimerUseCase @Inject constructor(
    private val timerStateRepo: TimerStateRepository,
    private val settingsRepo: SettingsRepository,
) {
    fun observeTimerMode(): Flow<TimerMode> =
        settingsRepo.timerModePomodoro.map { if (it) TimerMode.Pomodoro else TimerMode.Normal }

    suspend fun setTimerMode(mode: TimerMode) {
        settingsRepo.setTimerModePomodoro(mode == TimerMode.Pomodoro)
    }

    suspend fun startTimer(): Long {
        val now = System.currentTimeMillis()
        timerStateRepo.saveStartTime(now)
        timerStateRepo.saveBreakReminderSent(false)
        timerStateRepo.saveBreakReminderPendingRepeat(false)
        return now
    }

    suspend fun stopTimer() = timerStateRepo.clear()
    suspend fun takeBreak() = timerStateRepo.clear()
    suspend fun restoreStartTime(): Long? = timerStateRepo.loadStartTime()
    suspend fun getBreakReminderSent(): Boolean = timerStateRepo.loadBreakReminderSent()
    suspend fun setBreakReminderSent(sent: Boolean) = timerStateRepo.saveBreakReminderSent(sent)
    suspend fun getBreakReminderPendingRepeat(): Boolean = timerStateRepo.loadBreakReminderPendingRepeat()
    suspend fun setBreakReminderPendingRepeat(sent: Boolean) = timerStateRepo.saveBreakReminderPendingRepeat(sent)
}
