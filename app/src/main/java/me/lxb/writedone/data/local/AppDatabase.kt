package me.lxb.writedone.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import me.lxb.writedone.data.model.CompletedNote

@Database(entities = [CompletedNote::class], version = 5, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun completedNoteDao(): CompletedNoteDao

    companion object {
        val MIGRATION_1_2 = Migration(1, 2) { db ->
            db.execSQL("ALTER TABLE completed_notes ADD COLUMN sync_id TEXT NOT NULL DEFAULT ''")
            db.execSQL("ALTER TABLE completed_notes ADD COLUMN last_modified_at INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE completed_notes ADD COLUMN device_id TEXT NOT NULL DEFAULT ''")
            db.execSQL("UPDATE completed_notes SET last_modified_at = created_at WHERE last_modified_at = 0")
        }

        val MIGRATION_3_4 = Migration(3, 4) { db ->
            db.execSQL("""
                CREATE TABLE completed_notes_new (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    content TEXT NOT NULL,
                    body TEXT NOT NULL DEFAULT '',
                    created_at INTEGER NOT NULL,
                    duration_seconds INTEGER NOT NULL,
                    sync_id TEXT NOT NULL DEFAULT '',
                    last_modified_at INTEGER NOT NULL DEFAULT 0,
                    device_id TEXT NOT NULL DEFAULT ''
                )
            """.trimIndent())
            db.execSQL("""
                INSERT INTO completed_notes_new (id, content, body, created_at, duration_seconds, sync_id, last_modified_at, device_id)
                SELECT id, content, body, created_at, duration_seconds, sync_id, last_modified_at, device_id FROM completed_notes
            """.trimIndent())
            db.execSQL("DROP TABLE completed_notes")
            db.execSQL("ALTER TABLE completed_notes_new RENAME TO completed_notes")
        }

        val MIGRATION_4_5 = Migration(4, 5) { db ->
            db.execSQL("""
                CREATE TABLE completed_notes_new (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    content TEXT NOT NULL,
                    body TEXT NOT NULL DEFAULT '',
                    created_at INTEGER NOT NULL,
                    duration_seconds INTEGER NOT NULL,
                    sync_id TEXT NOT NULL DEFAULT '',
                    last_modified_at INTEGER NOT NULL DEFAULT 0
                )
            """.trimIndent())
            db.execSQL("""
                INSERT INTO completed_notes_new (id, content, body, created_at, duration_seconds, sync_id, last_modified_at)
                SELECT id, content, body, created_at, duration_seconds, sync_id, last_modified_at FROM completed_notes
            """.trimIndent())
            db.execSQL("DROP TABLE completed_notes")
            db.execSQL("ALTER TABLE completed_notes_new RENAME TO completed_notes")
        }
    }
}
