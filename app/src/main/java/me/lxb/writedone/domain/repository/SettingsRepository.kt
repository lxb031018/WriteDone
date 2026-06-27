package me.lxb.writedone.domain.repository

import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val breathingLampEnabled: Flow<Boolean>
    suspend fun setBreathingLampEnabled(enabled: Boolean)
    val autoDimBrightness: Flow<Boolean>
    suspend fun setAutoDimBrightness(enabled: Boolean)
    val themeMode: Flow<String>
    suspend fun setThemeMode(mode: String)
    suspend fun isAgreementAccepted(): Boolean
    suspend fun setAgreementAccepted(accepted: Boolean)
    val syncHostEnabled: Flow<Boolean>
    suspend fun setSyncHostEnabled(enabled: Boolean)
}
