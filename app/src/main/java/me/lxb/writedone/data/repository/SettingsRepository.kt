package me.lxb.writedone.data.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

private val Context.settingsDataStore by preferencesDataStore(name = "settings_prefs")

class SettingsRepository(private val context: Context) {

    companion object {
        private val TIMER_MODE_KEY = booleanPreferencesKey("timer_mode_pomodoro")
        private val AGREEMENT_ACCEPTED_KEY = booleanPreferencesKey("agreement_accepted")
    }

    val timerModePomodoro: Flow<Boolean> = context.settingsDataStore.data.map { prefs ->
        prefs[TIMER_MODE_KEY] ?: false
    }

    suspend fun setTimerModePomodoro(pomodoro: Boolean) {
        context.settingsDataStore.edit { prefs ->
            prefs[TIMER_MODE_KEY] = pomodoro
        }
    }

    fun isAgreementAccepted(): Boolean {
        return runBlocking {
            context.settingsDataStore.data.first()[AGREEMENT_ACCEPTED_KEY] ?: false
        }
    }

    suspend fun setAgreementAccepted(accepted: Boolean) {
        context.settingsDataStore.edit { prefs ->
            prefs[AGREEMENT_ACCEPTED_KEY] = accepted
        }
    }
}
