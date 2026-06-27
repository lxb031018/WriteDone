package me.lxb.writedone.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import me.lxb.writedone.service.notification.NotificationHelper

class TimerForegroundService : Service() {
    companion object {
        const val ACTION_START = "me.lxb.writedone.action.START_TIMER"
        const val ACTION_STOP = "me.lxb.writedone.action.STOP_TIMER"
        const val EXTRA_START_TIME = "extra_start_time"
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val notification = NotificationHelper.createTimerRunningNotification(this)
                startForeground(NotificationHelper.NOTIFICATION_ID_RUNNING, notification)
            }
            ACTION_STOP -> {
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
