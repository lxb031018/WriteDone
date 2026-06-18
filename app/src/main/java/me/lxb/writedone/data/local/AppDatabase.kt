package me.lxb.writedone.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import me.lxb.writedone.data.model.CompletedNote

@Database(entities = [CompletedNote::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun completedNoteDao(): CompletedNoteDao
}
