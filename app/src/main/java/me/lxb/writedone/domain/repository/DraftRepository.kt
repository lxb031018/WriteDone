package me.lxb.writedone.domain.repository

import kotlinx.coroutines.flow.Flow

interface DraftRepository {
    suspend fun load(): String
    fun loadFlow(): Flow<String>
    suspend fun save(text: String)
}
