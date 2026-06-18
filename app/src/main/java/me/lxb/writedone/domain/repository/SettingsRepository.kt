package me.lxb.writedone.domain.repository

import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val timerModePomodoro: Flow<Boolean>
    suspend fun setTimerModePomodoro(pomodoro: Boolean)
    val autoDimBrightness: Flow<Boolean>
    suspend fun setAutoDimBrightness(enabled: Boolean)
    suspend fun isAgreementAccepted(): Boolean
    suspend fun setAgreementAccepted(accepted: Boolean)
}
