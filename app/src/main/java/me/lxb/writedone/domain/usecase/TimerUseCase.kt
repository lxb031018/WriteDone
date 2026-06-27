package me.lxb.writedone.domain.usecase

import me.lxb.writedone.domain.repository.TimerStateRepository
import javax.inject.Inject

class TimerUseCase @Inject constructor(
    private val timerStateRepo: TimerStateRepository,
) {
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
