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
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.lxb.writedone.domain.usecase.NoteUseCase
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.Inet4Address
import java.net.Socket
import javax.inject.Inject
import javax.inject.Singleton

sealed class SyncRole {
    data object Host : SyncRole()
    data class Client(val gatewayIp: String) : SyncRole()
    data object Isolated : SyncRole()
}

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

    fun autoDetectRoleAndStart() {
        scope.launch {
            when (val role = detectRole()) {
                is SyncRole.Host -> {
                    Log.d(TAG, "Role: Host")
                    startHost()
                }
                is SyncRole.Client -> {
                    Log.d(TAG, "Role: Client, gateway=${role.gatewayIp}")
                    triggerSync(role.gatewayIp)
                    SyncWorker.schedule(context)
                }
                is SyncRole.Isolated -> {
                    Log.d(TAG, "Role: Isolated")
                    _state.value = _state.value.copy(lastSyncResult = "未连接到热点")
                }
            }
        }
    }

    fun destroy() {
        stopHost()
        SyncWorker.cancel(context)
        scope.cancel()
    }

    suspend fun syncWithHost(hostIp: String): String = withContext(Dispatchers.IO) {
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
        } catch (e: Exception) {
            Log.e(TAG, "Sync with $hostIp failed", e)
            "同步失败: ${e.message}"
        } finally {
            try { socket.close() } catch (_: Exception) {}
        }
    }

    private fun detectRole(): SyncRole {
        val myIp = thisDevice.ipAddress
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        val network = cm?.activeNetwork
        val lp = network?.let { cm.getLinkProperties(it) }
        val gateway = lp?.routes
            ?.filter { it.isDefaultRoute }
            ?.mapNotNull { it.gateway }
            ?.firstOrNull { it is Inet4Address }
            ?.hostAddress
            ?: return SyncRole.Isolated

        return if (myIp == gateway) SyncRole.Host
        else SyncRole.Client(gateway)
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

    private suspend fun triggerSync(gatewayIp: String) {
        _state.value = _state.value.copy(isSyncing = true, lastSyncResult = "")
        val result = syncWithHost(gatewayIp)
        _state.value = _state.value.copy(isSyncing = false, lastSyncResult = result)
    }
}
