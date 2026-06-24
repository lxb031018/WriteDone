package me.lxb.writedone.domain.usecase

import me.lxb.writedone.data.model.CompletedNote
import me.lxb.writedone.domain.repository.NoteRepository
import java.util.Date
import javax.inject.Inject
import javax.inject.Named

class NoteUseCase @Inject constructor(
    private val noteRepo: NoteRepository,
    @Named("deviceId") private val deviceId: String,
) {
    suspend fun updateNoteBody(id: Long, body: String) = noteRepo.updateBody(id, body)

    suspend fun addNote(content: String, createdAt: Date, durationSeconds: Int): CompletedNote {
        val now = System.currentTimeMillis()
        val note = CompletedNote(
            content = content,
            createdAt = createdAt.time,
            durationSeconds = durationSeconds,
            syncId = CompletedNote.generateSyncId(),
            lastModifiedAt = now,
            deviceId = deviceId,
        )
        val id = noteRepo.insert(note)
        return note.copy(id = id)
    }

    suspend fun getNotesByDate(date: Date): List<CompletedNote> = noteRepo.getByDate(date)

    suspend fun getNotesByDateRange(startMillis: Long, endMillis: Long): List<CompletedNote> =
        noteRepo.getByDateRange(startMillis, endMillis)

    suspend fun getAllNotes(): List<CompletedNote> = noteRepo.getAll()

    suspend fun upsert(note: CompletedNote) = noteRepo.upsert(note)

    suspend fun upsertAll(notes: List<CompletedNote>) = noteRepo.upsertAll(notes)

    suspend fun getModifiedSince(since: Long): List<CompletedNote> =
        noteRepo.getModifiedSince(since)

    suspend fun resolveConflict(id: Long) = noteRepo.resolveConflict(id)
}
