package me.lxb.writedone.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import me.lxb.writedone.data.model.CompletedNote

@Dao
interface CompletedNoteDao {

    @Query("SELECT * FROM completed_notes WHERE created_at >= :startMillis AND created_at < :endMillis ORDER BY created_at DESC")
    suspend fun getByDateRange(startMillis: Long, endMillis: Long): List<CompletedNote>

    @Insert
    suspend fun insert(note: CompletedNote): Long

    @Query("UPDATE completed_notes SET body = :body WHERE id = :id")
    suspend fun updateNoteBody(id: Long, body: String)
}
