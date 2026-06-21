package me.lxb.writedone.data.sync

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.lxb.writedone.domain.repository.SettingsRepository
import me.lxb.writedone.domain.usecase.NoteUseCase
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.Inet4Address
import java.net.Socket
import javax.inject.Inject
import javax.inject.Singleton

data class SyncState(
    val isHostEnabled: Boolean = false,
    val isHostRunning: Boolean = false,
    val isSyncing: Boolean = false,
    val lastSyncResult: String = "",
)

@Singleton
class SyncManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val noteUseCase: NoteUseCase,
    private val pairingRepo: PairingRepository,
    private val settingsRepo: SettingsRepository,
) {
    companion object {
        private const val TAG = "SyncManager"
        private const val SOCKET_TIMEOUT = 30000
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val _state = MutableStateFlow(SyncState())
    val state: StateFlow<SyncState> = _state.asStateFlow()

    private val thisDevice = DeviceInfo.getThis(context.contentResolver)

    fun onResume() {
        scope.launch {
            val enabled = settingsRepo.syncHostEnabled.first()
            _state.value = _state.value.copy(isHostEnabled = enabled)
            if (enabled) {
                startHost()
            } else {
                maybeSyncAsClient()
            }
        }
    }

    fun setHostEnabled(enabled: Boolean) {
        scope.launch {
            settingsRepo.setSyncHostEnabled(enabled)
            _state.value = _state.value.copy(isHostEnabled = enabled)
            if (enabled) startHost() else stopHost()
        }
    }

    fun syncNow() {
        scope.launch { maybeSyncAsClient() }
    }

    fun refreshStatus() {
        scope.launch {
            if (!_state.value.isHostEnabled && resolveGateway() == null) {
                _state.value = _state.value.copy(lastSyncResult = "")
            }
        }
    }

    fun destroy() {
        stopHost()
        scope.cancel()
    }

    suspend fun syncWithHost(hostIp: String): String = withContext(Dispatchers.IO) {
        try {
            val socket = Socket(hostIp, TCP_SYNC_PORT)
            try {
                socket.soTimeout = SOCKET_TIMEOUT
                val writer = PrintWriter(socket.getOutputStream(), true)
                val reader = BufferedReader(InputStreamReader(socket.getInputStream()))

                val identify = SyncMessage(
                    type = MSG_TYPE_IDENTIFY,
                    deviceId = thisDevice.deviceId,
                    deviceName = thisDevice.deviceName,
                )
                writer.println(identify.toJson())

                val ackLine = reader.readLine()
                if (ackLine == null) {
                    return@withContext "同步失败: 主机无响应"
                }
                val ackMsg = parseSyncMessage(ackLine)
                if (!ackMsg.accepted) {
                    return@withContext "同步失败: 主机拒绝"
                }

                pairingRepo.addPairedDevice(ackMsg.deviceId, ackMsg.deviceName)

                val lastSync = pairingRepo.getLastSyncTimestamp()
                val localNotes = noteUseCase.getModifiedSince(lastSync)
                val syncNotes = localNotes.map { it.toSyncNote() }

                val syncRequest = SyncMessage(
                    type = MSG_TYPE_SYNC_REQUEST,
                    deviceId = thisDevice.deviceId,
                    lastSyncTimestamp = lastSync,
                    notes = syncNotes,
                )
                writer.println(syncRequest.toJson())

                val response = reader.readLine()
                if (response != null) {
                    val dataMsg = parseSyncMessage(response)
                    if (dataMsg.type == MSG_TYPE_SYNC_DATA && dataMsg.notes.isNotEmpty()) {
                        val incoming = dataMsg.notes.map { it.toCompletedNote() }
                        noteUseCase.upsertAll(incoming)
                    }
                }

                val ack = SyncMessage(
                    type = MSG_TYPE_SYNC_ACK,
                    deviceId = thisDevice.deviceId,
                )
                writer.println(ack.toJson())

                pairingRepo.updateLastSyncTimestamp(System.currentTimeMillis())

                val sent = syncNotes.size
                if (sent > 0) "同步完成，发送 $sent 条"
                else "同步完成"
            } finally {
                try { socket.close() } catch (_: Exception) {}
            }
        } catch (e: Exception) {
            Log.e(TAG, "Sync with $hostIp failed", e)
            "同步失败: ${e.message}"
        }
    }

    private suspend fun maybeSyncAsClient() {
        val gateway = resolveGateway() ?: run {
            _state.value = _state.value.copy(lastSyncResult = "未连接到热点")
            return
        }
        _state.value = _state.value.copy(isSyncing = true, lastSyncResult = "")
        val result = syncWithHost(gateway)
        _state.value = _state.value.copy(isSyncing = false, lastSyncResult = result)
    }

    private fun resolveGateway(): String? {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            ?: return null
        val network = cm.activeNetwork ?: return null
        val lp = cm.getLinkProperties(network) ?: return null
        return lp.routes
            ?.filter { it.isDefaultRoute }
            ?.mapNotNull { it.gateway }
            ?.firstOrNull { it is Inet4Address }
            ?.hostAddress
    }

    private fun startHost() {
        if (_state.value.isHostRunning) return
        _state.value = _state.value.copy(isHostRunning = true)
        val intent = Intent(context, SyncHostService::class.java)
        context.startForegroundService(intent)
    }

    private fun stopHost() {
        if (!_state.value.isHostRunning) return
        _state.value = _state.value.copy(isHostRunning = false)
        val intent = Intent(context, SyncHostService::class.java)
        context.stopService(intent)
    }
}
