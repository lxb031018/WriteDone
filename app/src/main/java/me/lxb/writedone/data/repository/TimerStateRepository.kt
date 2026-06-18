package me.lxb.writedone.data.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import me.lxb.writedone.domain.repository.TimerStateRepository

private val Context.timerDataStore by preferencesDataStore(name = "timer_prefs")

class TimerStateRepositoryImpl(private val context: Context) : TimerStateRepository {

    companion object {
        private val startTimeKey = longPreferencesKey("timer_start_time")
        private val breakReminderSentKey = booleanPreferencesKey("break_reminder_sent")
        private val breakReminderPendingRepeatKey = booleanPreferencesKey("break_reminder_pending_repeat")
    }

    override suspend fun loadStartTime(): Long? {
        return context.timerDataStore.data.first()[startTimeKey]
    }

    override suspend fun saveStartTime(millis: Long) {
        context.timerDataStore.edit { prefs ->
            prefs[startTimeKey] = millis
        }
    }

    override suspend fun loadBreakReminderSent(): Boolean {
        return context.timerDataStore.data.first()[breakReminderSentKey] ?: false
    }

    override suspend fun saveBreakReminderSent(sent: Boolean) {
        context.timerDataStore.edit { prefs ->
            prefs[breakReminderSentKey] = sent
        }
    }

    override suspend fun loadBreakReminderPendingRepeat(): Boolean {
        return context.timerDataStore.data.first()[breakReminderPendingRepeatKey] ?: false
    }

    override suspend fun saveBreakReminderPendingRepeat(sent: Boolean) {
        context.timerDataStore.edit { prefs ->
            prefs[breakReminderPendingRepeatKey] = sent
        }
    }

    override suspend fun clear() {
        context.timerDataStore.edit { prefs ->
            prefs.remove(startTimeKey)
            prefs.remove(breakReminderSentKey)
            prefs.remove(breakReminderPendingRepeatKey)
        }
    }
}
