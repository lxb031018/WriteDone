package me.lxb.writedone.data.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

private val Context.timerDataStore by preferencesDataStore(name = "timer_prefs")

class TimerStateRepository(private val context: Context) {

    companion object {
        private val START_TIME_KEY = longPreferencesKey("timer_start_time")
        private val BREAK_REMINDER_SENT_KEY = booleanPreferencesKey("break_reminder_sent")
        private val BREAK_REMINDER_PENDING_REPEAT_KEY = booleanPreferencesKey("break_reminder_pending_repeat")
    }

    fun loadStartTime(): Long? {
        return runBlocking {
            context.timerDataStore.data.first()[START_TIME_KEY]
        }
    }

    suspend fun saveStartTime(millis: Long) {
        context.timerDataStore.edit { prefs ->
            prefs[START_TIME_KEY] = millis
        }
    }

    fun loadBreakReminderSent(): Boolean {
        return runBlocking {
            context.timerDataStore.data.first()[BREAK_REMINDER_SENT_KEY] ?: false
        }
    }

    suspend fun saveBreakReminderSent(sent: Boolean) {
        context.timerDataStore.edit { prefs ->
            prefs[BREAK_REMINDER_SENT_KEY] = sent
        }
    }

    fun loadBreakReminderPendingRepeat(): Boolean {
        return runBlocking {
            context.timerDataStore.data.first()[BREAK_REMINDER_PENDING_REPEAT_KEY] ?: false
        }
    }

    fun saveBreakReminderPendingRepeatSync(sent: Boolean) {
        runBlocking {
            context.timerDataStore.edit { prefs ->
                prefs[BREAK_REMINDER_PENDING_REPEAT_KEY] = sent
            }
        }
    }

    fun saveBreakReminderSentSync(sent: Boolean) {
        runBlocking {
            context.timerDataStore.edit { prefs ->
                prefs[BREAK_REMINDER_SENT_KEY] = sent
            }
        }
    }

    suspend fun saveBreakReminderPendingRepeat(sent: Boolean) {
        context.timerDataStore.edit { prefs ->
            prefs[BREAK_REMINDER_PENDING_REPEAT_KEY] = sent
        }
    }

    suspend fun clear() {
        context.timerDataStore.edit { prefs ->
            prefs.remove(START_TIME_KEY)
            prefs.remove(BREAK_REMINDER_SENT_KEY)
            prefs.remove(BREAK_REMINDER_PENDING_REPEAT_KEY)
        }
    }
}
