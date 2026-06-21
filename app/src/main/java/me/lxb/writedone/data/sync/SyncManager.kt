package me.lxb.writedone.data.sync

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.lxb.writedone.domain.usecase.NoteUseCase
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.InetAddress
import java.net.Socket
import javax.inject.Inject
import javax.inject.Singleton

data class SyncState(
    val isHostRunning: Boolean = false,
    val isSyncing: Boolean = false,
    val lastSyncResult: String = "",
)

@Singleton
class SyncManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val noteUseCase: NoteUseCase,
    private val pairingRepo: PairingRepository,
) {
    companion object {
        private const val TAG = "SyncManager"
        private const val SOCKET_TIMEOUT = 30000
        private val FALLBACK_GATEWAYS = listOf(
            "192.168.43.1",
            "192.168.1.1",
            "192.168.0.1",
            "192.168.44.1",
        )
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val _state = MutableStateFlow(SyncState())
    val state: StateFlow<SyncState> = _state.asStateFlow()

    private val thisDevice = DeviceInfo.getThis(context.contentResolver)

    fun startHost() {
        if (_state.value.isHostRunning) return
        _state.value = _state.value.copy(isHostRunning = true)
        val intent = Intent(context, SyncHostService::class.java)
        context.startForegroundService(intent)
    }

    fun stopHost() {
        if (!_state.value.isHostRunning) return
        _state.value = _state.value.copy(isHostRunning = false)
        val intent = Intent(context, SyncHostService::class.java)
        context.stopService(intent)
    }

    suspend fun triggerSync(): String = withContext(Dispatchers.IO) {
        _state.value = _state.value.copy(isSyncing = true)
        try {
            val hostIp = resolveHostIp()
            if (hostIp == null) {
                _state.value = _state.value.copy(
                    isSyncing = false,
                    lastSyncResult = "无法检测到热点网关，请连接后再试",
                )
                return@withContext "无法检测到热点网关"
            }

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
                    _state.value = _state.value.copy(
                        isSyncing = false,
                        lastSyncResult = "主机未响应",
                    )
                    return@withContext "主机未响应"
                }
                val ackMsg = parseSyncMessage(ackLine)

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

                _state.value = _state.value.copy(
                    isSyncing = false,
                    lastSyncResult = "同步完成，发送 ${syncNotes.size} 条",
                )
                return@withContext "同步完成"
            } finally {
                try { socket.close() } catch (_: Exception) {}
            }
        } catch (e: Exception) {
            Log.e(TAG, "Sync failed", e)
            _state.value = _state.value.copy(
                isSyncing = false,
                lastSyncResult = "同步失败: ${e.message}",
            )
            return@withContext "同步失败: ${e.message}"
        }
    }

    fun destroy() {
        stopHost()
        scope.cancel()
    }

    private fun resolveHostIp(): String? {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            ?: return fallbackGateway()
        val network = cm.activeNetwork ?: return fallbackGateway()
        val lp = cm.getLinkProperties(network) ?: return fallbackGateway()
        val gateway = lp.routes
            ?.filter { it.isDefaultRoute }
            ?.mapNotNull { it.gateway }
            ?.firstOrNull { it is java.net.Inet4Address }
            ?.hostAddress
        return gateway ?: fallbackGateway()
    }

    private fun fallbackGateway(): String? {
        return FALLBACK_GATEWAYS.firstOrNull()
    }
}
