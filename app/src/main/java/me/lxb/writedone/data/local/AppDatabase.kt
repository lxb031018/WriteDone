package me.lxb.writedone.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import me.lxb.writedone.data.model.CompletedNote

@Database(entities = [CompletedNote::class], version = 3, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun completedNoteDao(): CompletedNoteDao

    companion object {
        val MIGRATION_1_2 = Migration(1, 2) { db ->
            db.execSQL("ALTER TABLE completed_notes ADD COLUMN sync_id TEXT NOT NULL DEFAULT ''")
            db.execSQL("ALTER TABLE completed_notes ADD COLUMN last_modified_at INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE completed_notes ADD COLUMN device_id TEXT NOT NULL DEFAULT ''")
            db.execSQL("UPDATE completed_notes SET last_modified_at = created_at WHERE last_modified_at = 0")
        }

        val MIGRATION_2_3 = Migration(2, 3) { db ->
            db.execSQL("ALTER TABLE completed_notes ADD COLUMN conflict_device_id TEXT NOT NULL DEFAULT ''")
        }
    }
}
