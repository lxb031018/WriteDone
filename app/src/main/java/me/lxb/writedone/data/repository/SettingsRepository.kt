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
        private val timerModeKey = booleanPreferencesKey("timer_mode_pomodoro")
        private val agreementAcceptedKey = booleanPreferencesKey("agreement_accepted")
        private val autoDimBrightnessKey = booleanPreferencesKey("auto_dim_brightness")
        private val themeModeKey = stringPreferencesKey("theme_mode")
    }

    override val timerModePomodoro: Flow<Boolean> = context.settingsDataStore.data.map { prefs ->
        prefs[timerModeKey] ?: false
    }

    override suspend fun setTimerModePomodoro(pomodoro: Boolean) {
        context.settingsDataStore.edit { prefs ->
            prefs[timerModeKey] = pomodoro
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
        prefs[themeModeKey] ?: "System"
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
}
