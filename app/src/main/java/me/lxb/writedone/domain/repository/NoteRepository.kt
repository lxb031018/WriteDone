package me.lxb.writedone.domain.repository

import me.lxb.writedone.data.model.CompletedNote
import java.util.Date

interface NoteRepository {
    suspend fun updateBody(id: Long, body: String)
    suspend fun insert(note: CompletedNote): Long
    suspend fun getByDate(date: Date): List<CompletedNote>
    suspend fun getByDateRange(startMillis: Long, endMillis: Long): List<CompletedNote>
}
