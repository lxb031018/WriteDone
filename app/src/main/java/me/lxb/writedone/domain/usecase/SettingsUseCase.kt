package me.lxb.writedone.domain.usecase

import kotlinx.coroutines.flow.Flow
import me.lxb.writedone.domain.repository.SettingsRepository
import javax.inject.Inject

class SettingsUseCase @Inject constructor(
    private val settingsRepo: SettingsRepository,
) {
    val breathingLampEnabled: Flow<Boolean> = settingsRepo.breathingLampEnabled

    suspend fun setBreathingLampEnabled(enabled: Boolean) = settingsRepo.setBreathingLampEnabled(enabled)

    val autoDimBrightness: Flow<Boolean> = settingsRepo.autoDimBrightness

    suspend fun setAutoDimBrightness(enabled: Boolean) = settingsRepo.setAutoDimBrightness(enabled)

    val themeMode: Flow<String> = settingsRepo.themeMode

    suspend fun setThemeMode(mode: String) = settingsRepo.setThemeMode(mode)

    suspend fun isAgreementAccepted(): Boolean = settingsRepo.isAgreementAccepted()
    suspend fun setAgreementAccepted(accepted: Boolean) = settingsRepo.setAgreementAccepted(accepted)

    val syncHostEnabled: Flow<Boolean> = settingsRepo.syncHostEnabled

    suspend fun setSyncHostEnabled(enabled: Boolean) = settingsRepo.setSyncHostEnabled(enabled)
}
