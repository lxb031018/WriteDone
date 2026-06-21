package me.lxb.writedone.data.repository

import me.lxb.writedone.data.local.CompletedNoteDao
import me.lxb.writedone.data.model.CompletedNote
import me.lxb.writedone.domain.repository.NoteRepository
import java.util.Calendar
import java.util.Date

class NoteRepositoryImpl(private val dao: CompletedNoteDao) : NoteRepository {

    override suspend fun updateBody(id: Long, body: String) {
        dao.updateNoteBody(id, body)
    }

    override suspend fun insert(note: CompletedNote): Long {
        return dao.insert(note)
    }

    override suspend fun getByDate(date: Date): List<CompletedNote> {
        val cal = Calendar.getInstance().apply { time = date }
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val startMillis = cal.timeInMillis
        cal.add(Calendar.DAY_OF_MONTH, 1)
        val endMillis = cal.timeInMillis
        return dao.getByDateRange(startMillis, endMillis)
    }

    override suspend fun getByDateRange(startMillis: Long, endMillis: Long): List<CompletedNote> {
        return dao.getByDateRange(startMillis, endMillis)
    }

    override suspend fun getAll(): List<CompletedNote> = dao.getAll()

    override suspend fun upsert(note: CompletedNote) = dao.upsert(note)

    override suspend fun upsertAll(notes: List<CompletedNote>) = dao.upsertAll(notes)

    override suspend fun getModifiedSince(since: Long): List<CompletedNote> =
        dao.getModifiedSince(since)
}
