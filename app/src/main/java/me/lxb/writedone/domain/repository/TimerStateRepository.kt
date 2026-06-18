package me.lxb.writedone.domain.repository

interface TimerStateRepository {
    suspend fun loadStartTime(): Long?
    suspend fun saveStartTime(millis: Long)
    suspend fun loadBreakReminderSent(): Boolean
    suspend fun saveBreakReminderSent(sent: Boolean)
    suspend fun loadBreakReminderPendingRepeat(): Boolean
    suspend fun saveBreakReminderPendingRepeat(sent: Boolean)
    suspend fun clear()
}
