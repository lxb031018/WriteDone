package me.lxb.writedone.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

private val Context.timerDataStore by preferencesDataStore(name = "timer_prefs")

class TimerStateRepository(private val context: Context) {

    companion object {
        private val START_TIME_KEY = longPreferencesKey("timer_start_time")
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

    suspend fun clear() {
        context.timerDataStore.edit { prefs ->
            prefs.remove(START_TIME_KEY)
        }
    }
}
