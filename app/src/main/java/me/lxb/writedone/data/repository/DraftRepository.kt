package me.lxb.writedone.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import me.lxb.writedone.domain.repository.DraftRepository

private val Context.draftDataStore by preferencesDataStore(name = "draft_prefs")

class DraftRepositoryImpl(private val context: Context) : DraftRepository {

    companion object {
        private val draftKey = stringPreferencesKey("draft_input_text")
    }

    override suspend fun load(): String {
        return context.draftDataStore.data.first()[draftKey] ?: ""
    }

    override fun loadFlow(): Flow<String> = context.draftDataStore.data.map { prefs ->
        prefs[draftKey] ?: ""
    }

    override suspend fun save(text: String) {
        context.draftDataStore.edit { prefs ->
            if (text.isBlank()) {
                prefs.remove(draftKey)
            } else {
                prefs[draftKey] = text
            }
        }
    }
}
