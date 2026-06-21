package me.lxb.writedone.data.sync

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import me.lxb.writedone.MainActivity
import me.lxb.writedone.R
import me.lxb.writedone.domain.usecase.NoteUseCase
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.ServerSocket
import java.net.SocketTimeoutException
import javax.inject.Inject

@AndroidEntryPoint
class SyncHostService : Service() {
    companion object {
        private const val TAG = "SyncHostService"
        const val CHANNEL_ID = "write_done_sync_host"
        const val NOTIFICATION_ID = 1003
    }

    @Inject lateinit var noteUseCase: NoteUseCase
    @Inject lateinit var pairingRepo: PairingRepository
    @Inject @javax.inject.Named("deviceId") lateinit var deviceId: String

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = createNotification()
        startForeground(NOTIFICATION_ID, notification)
        scope.launch {
            val serverSocket = ServerSocket().apply {
                setReuseAddress(true)
                bind(java.net.InetSocketAddress(TCP_SYNC_PORT))
            }
            try {
                serverSocket.soTimeout = 5000
                while (isActive) {
                    try {
                        val socket = serverSocket.accept()
                        scope.launch { handleConnection(socket) }
                    } catch (_: SocketTimeoutException) {
                        continue
                    } catch (e: Exception) {
                        Log.e(TAG, "Accept failed", e)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Server socket error", e)
            } finally {
                try { serverSocket.close() } catch (_: Exception) {}
            }
        }
        return START_STICKY
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private suspend fun handleConnection(socket: java.net.Socket) {
        try {
            socket.soTimeout = 30000
            val reader = BufferedReader(InputStreamReader(socket.getInputStream()))
            val writer = PrintWriter(socket.getOutputStream(), true)

            val line = reader.readLine() ?: return
            val msg = parseSyncMessage(line)
            if (msg.type != MSG_TYPE_IDENTIFY) return

            val remoteDeviceId = msg.deviceId
            val remoteDeviceName = msg.deviceName

            val ack = SyncMessage(
                type = MSG_TYPE_IDENTIFY_ACK,
                deviceId = deviceId,
                deviceName = android.os.Build.MODEL,
                accepted = true,
            )
            writer.println(ack.toJson())

            pairingRepo.addPairedDevice(remoteDeviceId, remoteDeviceName)

            val syncLine = reader.readLine() ?: return
            val syncMsg = parseSyncMessage(syncLine)
            if (syncMsg.type != MSG_TYPE_SYNC_REQUEST) return

            if (syncMsg.notes.isNotEmpty()) {
                val incoming = syncMsg.notes.map { it.toCompletedNote() }
                noteUseCase.upsertAll(incoming)
            }

            val hostNotes = noteUseCase.getModifiedSince(syncMsg.lastSyncTimestamp)
            val syncNotes = hostNotes.map { it.toSyncNote() }

            val dataMsg = SyncMessage(
                type = MSG_TYPE_SYNC_DATA,
                deviceId = deviceId,
                notes = syncNotes,
            )
            writer.println(dataMsg.toJson())

            reader.readLine()

            pairingRepo.updateLastSyncTimestamp(System.currentTimeMillis())

        } catch (e: Exception) {
            Log.e(TAG, "Connection handler error", e)
        } finally {
            try { socket.close() } catch (_: Exception) {}
        }
    }

    private fun createNotificationChannel() {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(
            CHANNEL_ID,
            getString(R.string.sync_host_channel_name),
            NotificationManager.IMPORTANCE_LOW,
        ).apply {
            setShowBadge(false)
            lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        }
        manager.createNotificationChannel(channel)
    }

    private fun createNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(getString(R.string.sync_host_notification_title))
            .setContentText(getString(R.string.sync_host_notification_text))
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(pendingIntent)
            .build()
    }
}
