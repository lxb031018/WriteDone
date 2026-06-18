package me.lxb.writedone.service

import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import me.lxb.writedone.service.notification.NotificationHelper

class TimerForegroundService : Service() {
    companion object {
        const val ACTION_START = "me.lxb.writedone.action.START_TIMER"
        const val ACTION_STOP = "me.lxb.writedone.action.STOP_TIMER"
        const val EXTRA_START_TIME = "extra_start_time"
        const val EXTRA_IS_POMODORO = "extra_is_pomodoro"
        const val WORK_SECONDS = 1500L
    }

    private val handler = Handler(Looper.getMainLooper())
    private var breakRunnable: Runnable? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val startTime = intent.getLongExtra(EXTRA_START_TIME, System.currentTimeMillis())
                val isPomodoro = intent.getBooleanExtra(EXTRA_IS_POMODORO, false)

                val notification = NotificationHelper.createTimerRunningNotification(this, isPomodoro)
                startForeground(NotificationHelper.NOTIFICATION_ID_RUNNING, notification)

                if (isPomodoro) {
                    scheduleBreakIfNeeded(startTime)
                }
            }
            ACTION_STOP -> {
                breakRunnable?.let(handler::removeCallbacks)
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
        return START_STICKY
    }

    private fun scheduleBreakIfNeeded(startTime: Long) {
        val elapsed = System.currentTimeMillis() - startTime
        val remaining = WORK_SECONDS * 1000L - elapsed
        if (remaining <= 0) {
            showBreakAndStop()
            return
        }
        val runnable = Runnable { showBreakAndStop() }
        breakRunnable = runnable
        handler.postDelayed(runnable, remaining)
    }

    private fun showBreakAndStop() {
        NotificationHelper.showBreakReminder(this)
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
