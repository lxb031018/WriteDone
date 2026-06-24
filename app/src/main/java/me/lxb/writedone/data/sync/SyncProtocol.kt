package me.lxb.writedone.data.sync

import me.lxb.writedone.data.model.CompletedNote
import org.json.JSONArray
import org.json.JSONObject

internal const val MSG_TYPE_IDENTIFY = "IDENTIFY"
internal const val MSG_TYPE_IDENTIFY_ACK = "IDENTIFY_ACK"
internal const val MSG_TYPE_SYNC_SUMMARY = "SYNC_SUMMARY"
internal const val MSG_TYPE_SYNC_REQUEST = "SYNC_REQUEST"
internal const val MSG_TYPE_SYNC_DATA = "SYNC_DATA"
internal const val MSG_TYPE_SYNC_ACK = "SYNC_ACK"

internal data class SyncMessage(
    val type: String,
    val deviceId: String,
    val deviceName: String = "",
    val lastSyncTimestamp: Long = 0,
    val notes: List<SyncNote> = emptyList(),
    val accepted: Boolean = false,
    val conflictSyncIds: List<String> = emptyList(),
)

internal data class SyncNote(
    val syncId: String,
    val content: String,
    val body: String,
    val createdAt: Long,
    val durationSeconds: Int,
    val lastModifiedAt: Long,
    val deviceId: String,
)

internal fun SyncMessage.toJson(): String = JSONObject().apply {
    put("type", type)
    put("deviceId", deviceId)
    if (deviceName.isNotEmpty()) put("deviceName", deviceName)
    if (lastSyncTimestamp > 0) put("lastSyncTimestamp", lastSyncTimestamp)
    if (notes.isNotEmpty()) {
        put("notes", JSONArray(notes.map { it.toJson() }))
    }
    if (accepted) put("accepted", true)
    if (conflictSyncIds.isNotEmpty()) {
        put("conflictSyncIds", JSONArray(conflictSyncIds))
    }
}.toString()

internal fun SyncNote.toJson(): JSONObject = JSONObject().apply {
    put("syncId", syncId)
    put("content", content)
    put("body", body)
    put("createdAt", createdAt)
    put("durationSeconds", durationSeconds)
    put("lastModifiedAt", lastModifiedAt)
    put("deviceId", deviceId)
}

internal fun parseSyncMessage(json: String): SyncMessage {
    val obj = JSONObject(json)
    return SyncMessage(
        type = obj.getString("type"),
        deviceId = obj.getString("deviceId"),
        deviceName = obj.optString("deviceName", ""),
        lastSyncTimestamp = obj.optLong("lastSyncTimestamp", 0),
        notes = obj.optJSONArray("notes")?.let { arr ->
            (0 until arr.length()).map { parseSyncNote(arr.getJSONObject(it)) }
        } ?: emptyList(),
        accepted = obj.optBoolean("accepted", false),
        conflictSyncIds = obj.optJSONArray("conflictSyncIds")?.let { arr ->
            (0 until arr.length()).map { arr.getString(it) }
        } ?: emptyList(),
    )
}

internal fun parseSyncNote(obj: JSONObject): SyncNote = SyncNote(
    syncId = obj.getString("syncId"),
    content = obj.getString("content"),
    body = obj.optString("body", ""),
    createdAt = obj.getLong("createdAt"),
    durationSeconds = obj.getInt("durationSeconds"),
    lastModifiedAt = obj.getLong("lastModifiedAt"),
    deviceId = obj.optString("deviceId", ""),
)

internal fun CompletedNote.toSyncNote() = SyncNote(
    syncId = syncId,
    content = content,
    body = body,
    createdAt = createdAt,
    durationSeconds = durationSeconds,
    lastModifiedAt = lastModifiedAt,
    deviceId = deviceId,
)

internal fun SyncNote.toCompletedNote(isConflict: Boolean = false) = CompletedNote(
    syncId = syncId,
    content = content,
    body = body,
    createdAt = createdAt,
    durationSeconds = durationSeconds,
    lastModifiedAt = lastModifiedAt,
    deviceId = deviceId,
    conflictDeviceId = if (isConflict) deviceId else "",
)
