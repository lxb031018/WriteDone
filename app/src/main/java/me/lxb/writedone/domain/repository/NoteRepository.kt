package me.lxb.writedone.domain.repository

import me.lxb.writedone.data.model.CompletedNote
import java.util.Date

interface NoteRepository {
    suspend fun updateBody(id: Long, body: String)
    suspend fun insert(note: CompletedNote): Long
    suspend fun getByDate(date: Date): List<CompletedNote>
    suspend fun getByDateRange(startMillis: Long, endMillis: Long): List<CompletedNote>
    suspend fun getAll(): List<CompletedNote>
    suspend fun upsert(note: CompletedNote)
    suspend fun upsertAll(notes: List<CompletedNote>)
    suspend fun getModifiedSince(since: Long): List<CompletedNote>
    suspend fun resolveConflict(id: Long)
}
