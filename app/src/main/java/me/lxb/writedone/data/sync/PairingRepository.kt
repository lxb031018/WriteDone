package me.lxb.writedone.data.sync

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import org.json.JSONArray
import org.json.JSONObject

private val Context.syncStore by preferencesDataStore("sync_prefs")

private val PAIRED_DEVICES_KEY = stringPreferencesKey("paired_devices")
private val LAST_SYNC_KEY = stringPreferencesKey("last_sync_timestamp")

class PairingRepository(private val context: Context) {

    private data class PairedDevice(
        val deviceId: String,
        val deviceName: String,
        val lastSeenIp: String = "",
    )

    suspend fun addPairedDevice(deviceId: String, deviceName: String) {
        val devices = getPairedDevices().toMutableList()
        if (devices.none { it.deviceId == deviceId }) {
            devices.add(PairedDevice(deviceId, deviceName))
            savePairedDevices(devices)
        }
    }

    suspend fun getLastSyncTimestamp(): Long {
        return context.syncStore.data.first()[LAST_SYNC_KEY]?.toLongOrNull() ?: 0L
    }

    suspend fun updateLastSyncTimestamp(timestamp: Long) {
        context.syncStore.edit { prefs ->
            prefs[LAST_SYNC_KEY] = timestamp.toString()
        }
    }

    private suspend fun getPairedDevices(): List<PairedDevice> {
        val json = context.syncStore.data.first()[PAIRED_DEVICES_KEY] ?: return emptyList()
        return try {
            val arr = JSONArray(json)
            (0 until arr.length()).map { i ->
                val obj = arr.getJSONObject(i)
                PairedDevice(
                    deviceId = obj.getString("deviceId"),
                    deviceName = obj.optString("deviceName", ""),
                    lastSeenIp = obj.optString("lastSeenIp", ""),
                )
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private suspend fun savePairedDevices(devices: List<PairedDevice>) {
        val arr = JSONArray(devices.map { d ->
            JSONObject().apply {
                put("deviceId", d.deviceId)
                put("deviceName", d.deviceName)
            }
        })
        context.syncStore.edit { prefs ->
            prefs[PAIRED_DEVICES_KEY] = arr.toString()
        }
    }

}
