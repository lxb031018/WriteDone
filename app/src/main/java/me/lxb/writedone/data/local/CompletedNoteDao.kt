package me.lxb.writedone.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import me.lxb.writedone.data.model.CompletedNote

@Dao
interface CompletedNoteDao {

    @Query("SELECT * FROM completed_notes WHERE created_at >= :startMillis AND created_at < :endMillis ORDER BY created_at DESC")
    suspend fun getByDateRange(startMillis: Long, endMillis: Long): List<CompletedNote>

    @Insert
    suspend fun insert(note: CompletedNote): Long

    @Query("UPDATE completed_notes SET content = :content, last_modified_at = :lastModifiedAt WHERE id = :id")
    suspend fun updateNoteContent(id: Long, content: String, lastModifiedAt: Long)

    @Query("SELECT * FROM completed_notes ORDER BY created_at DESC")
    suspend fun getAll(): List<CompletedNote>

    @Query("""
        UPDATE completed_notes
        SET content = :content, created_at = :createdAt,
            duration_seconds = :durationSeconds, last_modified_at = :lastModifiedAt
        WHERE sync_id = :syncId AND last_modified_at < :lastModifiedAt
    """)
    suspend fun updateIfNewer(
        syncId: String, content: String,
        createdAt: Long, durationSeconds: Int,
        lastModifiedAt: Long,
    )

    @Query("SELECT * FROM completed_notes WHERE sync_id = :syncId LIMIT 1")
    suspend fun getBySyncId(syncId: String): CompletedNote?

    @Transaction
    suspend fun upsertAll(notes: List<CompletedNote>) {
        for (note in notes) {
            val existing = getBySyncId(note.syncId)
            if (existing == null) {
                insert(note)
            } else if (note.lastModifiedAt >= existing.lastModifiedAt) {
                updateIfNewer(
                    syncId = note.syncId,
                    content = note.content,
                    createdAt = note.createdAt,
                    durationSeconds = note.durationSeconds,
                    lastModifiedAt = note.lastModifiedAt,
                )
            }
        }
    }

    @Query("SELECT * FROM completed_notes WHERE sync_id != '' AND last_modified_at > :since ORDER BY last_modified_at ASC")
    suspend fun getModifiedSince(since: Long): List<CompletedNote>
}
