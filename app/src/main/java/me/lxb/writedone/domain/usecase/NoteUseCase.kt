package me.lxb.writedone.domain.usecase

import me.lxb.writedone.data.model.CompletedNote
import me.lxb.writedone.domain.repository.NoteRepository
import java.util.Date
import javax.inject.Inject

class NoteUseCase @Inject constructor(
    private val noteRepo: NoteRepository,
) {
    suspend fun updateNoteBody(id: Long, body: String) = noteRepo.updateBody(id, body)

    suspend fun addNote(content: String, createdAt: Date, durationSeconds: Int): CompletedNote {
        val note = CompletedNote(
            content = content,
            createdAt = createdAt.time,
            durationSeconds = durationSeconds,
        )
        val id = noteRepo.insert(note)
        return note.copy(id = id)
    }

    suspend fun getNotesByDate(date: Date): List<CompletedNote> = noteRepo.getByDate(date)

    suspend fun getNotesByDateRange(startMillis: Long, endMillis: Long): List<CompletedNote> =
        noteRepo.getByDateRange(startMillis, endMillis)

    suspend fun getAllNotes(): List<CompletedNote> = noteRepo.getAll()
}
