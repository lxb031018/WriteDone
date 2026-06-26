package me.lxb.writedone.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "completed_notes")
data class CompletedNote(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val content: String,
    val body: String = "",
    @ColumnInfo(name = "created_at")
    val createdAt: Long,
    @ColumnInfo(name = "duration_seconds")
    val durationSeconds: Int,
    @ColumnInfo(name = "sync_id")
    val syncId: String = "",
    @ColumnInfo(name = "last_modified_at")
    val lastModifiedAt: Long = createdAt,
) {
    companion object {
        fun generateSyncId(): String = UUID.randomUUID().toString()
    }
}
