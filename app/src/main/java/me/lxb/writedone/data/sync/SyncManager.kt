package me.lxb.writedone.data.sync

import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.lxb.writedone.domain.repository.SettingsRepository
import me.lxb.writedone.domain.usecase.NoteUseCase
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.InetAddress
import java.net.ServerSocket
import java.net.Socket
import java.net.SocketTimeoutException
import javax.inject.Inject
import javax.inject.Singleton

data class SyncState(
    val isSyncing: Boolean = false,
    val lastSyncResult: String = "",
    val role: Role = Role.UNKNOWN,
    val isServerRunning: Boolean = false,
    val gatewayAddress: InetAddress? = null,
)

@Singleton
class SyncManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val noteUseCase: NoteUseCase,
    private val pairingRepo: PairingRepository,
    private val settingsRepo: SettingsRepository,
    private val hotspotManager: HotspotManager,
) {
    companion object {
        private const val TAG = "SyncManager"
        private const val TCP_SYNC_PORT = 48766
        private const val SOCKET_TIMEOUT = 30000
        private const val CONFLICT_THRESHOLD_MS = 5000L
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val _state = MutableStateFlow(SyncState())
    val state: StateFlow<SyncState> = _state.asStateFlow()

    private var serverSocket: ServerSocket? = null
    private var serverScope: CoroutineScope? = null

    fun onResume() {
        // No auto-initialization needed; user triggers sync manually
    }

    fun syncNow() {
        scope.launch {
            if (serverSocket?.isBound == true) {
                serverScope?.cancel()
                try { serverSocket?.close() } catch (_: Exception) {}
                serverSocket = null
                serverScope = null
            }
            _state.value = _state.value.copy(isSyncing = true, lastSyncResult = "")
            val (role, preConnectedSocket) = hotspotManager.detectRole()
            _state.value = _state.value.copy(role = role)

            when (role) {
                Role.HOST -> {
                    _state.value = _state.value.copy(
                        isSyncing = false,
                        lastSyncResult = "本机是热点主机，启动服务器等待连接...",
                        isServerRunning = true,
                    )
                    startServerSocket()
                }
                Role.CLIENT -> {
                    val result = if (preConnectedSocket != null) {
                        Log.d(TAG, "CLIENT role, using pre-connected socket (scan/fast path)")
                        try {
                            performSync(preConnectedSocket)
                        } finally {
                            try { preConnectedSocket.close() } catch (_: Exception) {}
                        }
                    } else {
                        val gateway = hotspotManager.getGatewayAddress()
                        if (gateway == null) {
                            "同步失败: 无法连接主机"
                        } else {
                            Log.d(TAG, "CLIENT role, connecting to ${gateway.hostAddress}:$TCP_SYNC_PORT")
                            doSync(gateway)
                        }
                    }
                    _state.value = _state.value.copy(isSyncing = false, lastSyncResult = result)
                }
                Role.UNKNOWN -> {
                    _state.value = _state.value.copy(
                        lastSyncResult = "同步失败: 无法确定设备角色",
                        isSyncing = false,
                    )
                }
            }
        }
    }

    fun destroy() {
        serverScope?.cancel()
        scope.cancel()
        try { serverSocket?.close() } catch (_: Exception) {}
        hotspotManager.reset()
    }

    // ── Server (Host) ──

    private fun startServerSocket() {
        serverScope?.cancel()
        try { serverSocket?.close() } catch (_: Exception) {}
        serverSocket = null
        serverScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        serverScope?.launch {
            val ss = hotspotManager.startServerSocket() ?: return@launch
            serverSocket = ss
            try {
                while (isActive) {
                    try {
                        val socket = ss.accept()
                        Log.d(TAG, "Client connected from ${socket.inetAddress.hostAddress}")
                        _state.value = _state.value.copy(
                            lastSyncResult = "客户端已连接 (${socket.inetAddress.hostAddress})"
                        )
                        scope.launch { handleServerConnection(socket) }
                    } catch (_: SocketTimeoutException) {
                        continue
                    } catch (e: Exception) {
                        Log.e(TAG, "Accept failed", e)
                    }
                }
            } finally {
                try { ss.close() } catch (_: Exception) {}
                _state.value = _state.value.copy(isServerRunning = false)
            }
        }
    }

    private suspend fun handleServerConnection(socket: Socket) = withContext(Dispatchers.IO) {
        try {
            socket.soTimeout = SOCKET_TIMEOUT
            val reader = BufferedReader(InputStreamReader(socket.getInputStream()))
            val writer = PrintWriter(socket.getOutputStream(), true)

            val line = reader.readLine() ?: return@withContext
            val msg = parseSyncMessage(line)
            if (msg.type != MSG_TYPE_IDENTIFY) return@withContext

            pairingRepo.addPairedDevice(msg.deviceId, msg.deviceName)

            val ack = SyncMessage(
                type = MSG_TYPE_IDENTIFY_ACK,
                deviceId = getDeviceId(),
                deviceName = android.os.Build.MODEL,
                accepted = true,
            )
            writer.println(ack.toJson())

            val syncLine = reader.readLine() ?: return@withContext
            val syncMsg = parseSyncMessage(syncLine)
            if (syncMsg.type != MSG_TYPE_SYNC_SUMMARY) return@withContext

            // 1. Detect conflicts & send HOST's notes to CLIENT (before upsert — preserve HOST's versions)
            val conflictIds = detectConflicts(syncMsg)
            val hostLastSync = pairingRepo.getLastSyncTimestamp()
            val hostNotes = noteUseCase.getModifiedSince(hostLastSync)
            val hostSyncNotes = hostNotes.map { it.toSyncNote() }
            val dataMsg = SyncMessage(
                type = MSG_TYPE_SYNC_DATA,
                deviceId = getDeviceId(),
                notes = hostSyncNotes,
                conflictSyncIds = conflictIds,
            )
            writer.println(dataMsg.toJson())

            // 2. Store CLIENT's incoming notes (mark conflicts)
            val incoming = syncMsg.notes.map {
                val isConflict = conflictIds.contains(it.syncId)
                it.toCompletedNote(isConflict)
            }
            noteUseCase.upsertAll(incoming)

            // 3. Wait for CLIENT SYNC_ACK
            reader.readLine()

            // 4. Update timestamps and UI
            if (incoming.isNotEmpty() || hostSyncNotes.isNotEmpty()) {
                pairingRepo.updateLastSyncTimestamp(System.currentTimeMillis())
            }
            _state.value = _state.value.copy(lastSyncResult = "同步完成")
        } catch (e: Exception) {
            Log.e(TAG, "Server handler error", e)
        } finally {
            try { socket.close() } catch (_: Exception) {}
        }
    }

    // ── Client ──

    private suspend fun detectConflicts(summary: SyncMessage): List<String> {
        val conflictIds = mutableListOf<String>()
        if (summary.lastSyncTimestamp <= 0) return conflictIds

        val localModified = noteUseCase.getModifiedSince(summary.lastSyncTimestamp)
        val localMap = localModified.associateBy { it.syncId }

        for (remoteNote in summary.notes) {
            val localNote = localMap[remoteNote.syncId]
            if (localNote != null) {
                val timeDiff = kotlin.math.abs(localNote.lastModifiedAt - remoteNote.lastModifiedAt)
                if (timeDiff < CONFLICT_THRESHOLD_MS && localNote.lastModifiedAt != remoteNote.lastModifiedAt) {
                    conflictIds.add(remoteNote.syncId)
                }
            }
        }
        return conflictIds
    }

    private suspend fun doSync(hostAddress: InetAddress): String {
        var socket: Socket? = null
        return try {
            Log.d(TAG, "doSync: connecting to ${hostAddress.hostAddress}:$TCP_SYNC_PORT")
            socket = Socket(hostAddress, TCP_SYNC_PORT)
            Log.d(TAG, "doSync: connected")
            performSync(socket)
        } catch (e: Exception) {
            Log.e(TAG, "Sync with host failed", e)
            "同步失败: ${e.message}"
        } finally {
            try { socket?.close() } catch (_: Exception) {}
        }
    }

    private suspend fun performSync(socket: Socket): String {
        return try {
            socket.soTimeout = SOCKET_TIMEOUT
            val writer = PrintWriter(socket.getOutputStream(), true)
            val reader = BufferedReader(InputStreamReader(socket.getInputStream()))

            val dId = getDeviceId()

            val identify = SyncMessage(
                type = MSG_TYPE_IDENTIFY,
                deviceId = dId,
                deviceName = android.os.Build.MODEL,
            )
            writer.println(identify.toJson())

            val ackLine = reader.readLine() ?: return "同步失败: 主机无响应"
            val ackMsg = parseSyncMessage(ackLine)
            if (!ackMsg.accepted) return "同步失败: 主机拒绝"

            pairingRepo.addPairedDevice(ackMsg.deviceId, ackMsg.deviceName)

            val lastSync = 0L
            val localNotes = noteUseCase.getModifiedSince(lastSync)
            val syncNotes = localNotes.map { it.toSyncNote() }

            val summary = SyncMessage(
                type = MSG_TYPE_SYNC_SUMMARY,
                deviceId = dId,
                lastSyncTimestamp = lastSync,
                notes = syncNotes,
            )
            writer.println(summary.toJson())

            val response = reader.readLine()
            var receivedFromHost = false
            if (response != null) {
                val dataMsg = parseSyncMessage(response)
                if (dataMsg.type == MSG_TYPE_SYNC_DATA) {
                    val incoming = dataMsg.notes.map {
                        val isConflict = dataMsg.conflictSyncIds.contains(it.syncId)
                        it.toCompletedNote(isConflict)
                    }
                    noteUseCase.upsertAll(incoming)
                    receivedFromHost = true
                }
            }

            val ack = SyncMessage(
                type = MSG_TYPE_SYNC_ACK,
                deviceId = dId,
            )
            writer.println(ack.toJson())

            if (syncNotes.isNotEmpty() || receivedFromHost) {
                pairingRepo.updateLastSyncTimestamp(System.currentTimeMillis())
            }

            val sent = syncNotes.size
            if (sent > 0) "同步完成，发送 $sent 条"
            else "同步完成"
        } catch (e: Exception) {
            Log.e(TAG, "performSync failed", e)
            "同步失败: ${e.message}"
        }
    }

    private fun getDeviceId(): String {
        return try {
            android.provider.Settings.Secure.getString(
                context.contentResolver,
                android.provider.Settings.Secure.ANDROID_ID
            ) ?: ""
        } catch (_: Exception) { "" }
    }
}
