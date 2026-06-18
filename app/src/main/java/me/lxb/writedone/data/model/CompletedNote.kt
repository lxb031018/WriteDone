package me.lxb.writedone.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

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
)
