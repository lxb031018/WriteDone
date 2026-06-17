package me.lxb.writedone.data.repository

import android.content.Context
import me.lxb.writedone.data.local.AppDatabase
import me.lxb.writedone.data.model.CompletedNote
import java.util.Calendar
import java.util.Date

class NoteRepository(context: Context) {
    private val db = AppDatabase(context)

    fun updateBody(id: Long, body: String) {
        db.updateNoteBody(id, body)
    }

    fun insert(note: CompletedNote): Long {
        return db.insertNote(note)
    }

    fun getByDate(date: Date): List<CompletedNote> {
        val cal = Calendar.getInstance().apply { time = date }
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val startMillis = cal.timeInMillis
        cal.add(Calendar.DAY_OF_MONTH, 1)
        val endMillis = cal.timeInMillis
        return db.getNotesByDateRange(startMillis, endMillis)
    }

    fun getByDateRange(startMillis: Long, endMillis: Long): List<CompletedNote> {
        return db.getNotesByDateRange(startMillis, endMillis)
    }
}
