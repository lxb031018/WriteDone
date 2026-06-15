package me.lxb.writedone.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

private val Context.draftDataStore by preferencesDataStore(name = "draft_prefs")

class DraftRepository(private val context: Context) {

    companion object {
        private val DRAFT_KEY = stringPreferencesKey("draft_input_text")
    }

    fun load(): String {
        return runBlocking {
            context.draftDataStore.data.first()[DRAFT_KEY] ?: ""
        }
    }

    suspend fun save(text: String) {
        context.draftDataStore.edit { prefs ->
            if (text.isBlank()) {
                prefs.remove(DRAFT_KEY)
            } else {
                prefs[DRAFT_KEY] = text
            }
        }
    }
}
