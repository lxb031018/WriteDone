package me.lxb.writedone.data.model

data class CompletedNote(
    val id: Long = 0,
    val content: String,
    val body: String = "",
    val createdAt: Long,
    val durationSeconds: Int,
)
