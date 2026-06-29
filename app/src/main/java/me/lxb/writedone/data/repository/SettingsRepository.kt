package me.lxb.writedone.data.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import me.lxb.writedone.domain.repository.SettingsRepository

private val Context.settingsDataStore by preferencesDataStore(name = "settings_prefs")

class SettingsRepositoryImpl(private val context: Context) : SettingsRepository {

    companion object {
        private val breathingLampKey = booleanPreferencesKey("breathing_lamp_enabled")
        private val agreementAcceptedKey = booleanPreferencesKey("agreement_accepted")
        private val autoDimBrightnessKey = booleanPreferencesKey("auto_dim_brightness")
        private val themeModeKey = stringPreferencesKey("theme_mode")
        private val syncHostEnabledKey = booleanPreferencesKey("sync_host_enabled")
        private val autoStartTimerOnLandscapeKey = booleanPreferencesKey("auto_start_timer_on_landscape")
    }

    override val breathingLampEnabled: Flow<Boolean> = context.settingsDataStore.data.map { prefs ->
        prefs[breathingLampKey] ?: false
    }

    override suspend fun setBreathingLampEnabled(enabled: Boolean) {
        context.settingsDataStore.edit { prefs ->
            prefs[breathingLampKey] = enabled
        }
    }

    override val autoDimBrightness: Flow<Boolean> = context.settingsDataStore.data.map { prefs ->
        prefs[autoDimBrightnessKey] ?: false
    }

    override suspend fun setAutoDimBrightness(enabled: Boolean) {
        context.settingsDataStore.edit { prefs ->
            prefs[autoDimBrightnessKey] = enabled
        }
    }

    override val themeMode: Flow<String> = context.settingsDataStore.data.map { prefs ->
        prefs[themeModeKey] ?: "Light"
    }

    override suspend fun setThemeMode(mode: String) {
        context.settingsDataStore.edit { prefs ->
            prefs[themeModeKey] = mode
        }
    }

    override suspend fun isAgreementAccepted(): Boolean {
        return context.settingsDataStore.data.first()[agreementAcceptedKey] ?: false
    }

    override suspend fun setAgreementAccepted(accepted: Boolean) {
        context.settingsDataStore.edit { prefs ->
            prefs[agreementAcceptedKey] = accepted
        }
    }

    override val syncHostEnabled: Flow<Boolean> = context.settingsDataStore.data.map { prefs ->
        prefs[syncHostEnabledKey] ?: false
    }

    override suspend fun setSyncHostEnabled(enabled: Boolean) {
        context.settingsDataStore.edit { prefs ->
            prefs[syncHostEnabledKey] = enabled
        }
    }

    override val autoStartTimerOnLandscapeEnabled: Flow<Boolean> = context.settingsDataStore.data.map { prefs ->
        prefs[autoStartTimerOnLandscapeKey] ?: false
    }

    override suspend fun setAutoStartTimerOnLandscapeEnabled(enabled: Boolean) {
        context.settingsDataStore.edit { prefs ->
            prefs[autoStartTimerOnLandscapeKey] = enabled
        }
    }
}
