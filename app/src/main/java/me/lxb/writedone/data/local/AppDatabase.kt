package me.lxb.writedone.data.local

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import me.lxb.writedone.data.model.CompletedNote

class AppDatabase(context: Context) : SQLiteOpenHelper(
    context, "writedone.db", null, 1
) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE completed_notes (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                content TEXT NOT NULL,
                created_at INTEGER NOT NULL,
                duration_seconds INTEGER NOT NULL
            )
        """.trimIndent())
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {}

    fun insertNote(note: CompletedNote): Long {
        val db = writableDatabase
        val values = android.content.ContentValues().apply {
            put("content", note.content)
            put("created_at", note.createdAt)
            put("duration_seconds", note.durationSeconds)
        }
        return db.insert("completed_notes", null, values)
    }

    fun getNotesByDateRange(startMillis: Long, endMillis: Long): List<CompletedNote> {
        val db = readableDatabase
        val cursor = db.query(
            "completed_notes",
            null,
            "created_at >= ? AND created_at < ?",
            arrayOf(startMillis.toString(), endMillis.toString()),
            null, null,
            "created_at DESC",
        )
        val notes = mutableListOf<CompletedNote>()
        cursor.use { c ->
            while (c.moveToNext()) {
                notes.add(
                    CompletedNote(
                        id = c.getLong(c.getColumnIndexOrThrow("id")),
                        content = c.getString(c.getColumnIndexOrThrow("content")),
                        createdAt = c.getLong(c.getColumnIndexOrThrow("created_at")),
                        durationSeconds = c.getInt(c.getColumnIndexOrThrow("duration_seconds")),
                    )
                )
            }
        }
        return notes
    }
}
