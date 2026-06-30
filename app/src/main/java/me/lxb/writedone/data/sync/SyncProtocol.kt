package me.lxb.writedone.data.sync

import me.lxb.writedone.data.model.CompletedNote
import org.json.JSONArray
import org.json.JSONObject

internal const val MSG_TYPE_SYNC_DATA = "SYNC_DATA"

internal data class SyncMessage(
    val type: String,
    val notes: List<SyncNote> = emptyList(),
)

internal data class SyncNote(
    val syncId: String,
    val content: String,
    val createdAt: Long,
    val durationSeconds: Int,
    val lastModifiedAt: Long,
)

internal fun SyncMessage.toJson(): String = JSONObject().apply {
    put("type", type)
    if (notes.isNotEmpty()) {
        put("notes", JSONArray(notes.map { it.toJson() }))
    }
}.toString()

internal fun SyncNote.toJson(): JSONObject = JSONObject().apply {
    put("syncId", syncId)
    put("content", content)
    put("createdAt", createdAt)
    put("durationSeconds", durationSeconds)
    put("lastModifiedAt", lastModifiedAt)
}

internal fun parseSyncMessage(json: String): SyncMessage {
    val obj = JSONObject(json)
    return SyncMessage(
        type = obj.getString("type"),
        notes = obj.optJSONArray("notes")?.let { arr ->
            (0 until arr.length()).map { parseSyncNote(arr.getJSONObject(it)) }
        } ?: emptyList(),
    )
}

internal fun parseSyncNote(obj: JSONObject): SyncNote = SyncNote(
    syncId = obj.getString("syncId"),
    content = obj.getString("content"),
    createdAt = obj.getLong("createdAt"),
    durationSeconds = obj.getInt("durationSeconds"),
    lastModifiedAt = obj.getLong("lastModifiedAt"),
)

internal fun CompletedNote.toSyncNote() = SyncNote(
    syncId = syncId,
    content = content,
    createdAt = createdAt,
    durationSeconds = durationSeconds,
    lastModifiedAt = lastModifiedAt,
)

internal fun SyncNote.toCompletedNote() = CompletedNote(
    syncId = syncId,
    content = content,
    createdAt = createdAt,
    durationSeconds = durationSeconds,
    lastModifiedAt = lastModifiedAt,
)
