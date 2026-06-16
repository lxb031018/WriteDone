package me.lxb.writedone.notification

import android.app.AlarmManager
import android.app.KeyguardManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.runBlocking
import me.lxb.writedone.data.repository.TimerStateRepository

class UnlockNotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_USER_PRESENT) return
        val repo = TimerStateRepository(context)
        if (!repo.loadBreakReminderPendingRepeat()) return
        val keyguard = context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        if (keyguard.isDeviceLocked) return
        repo.saveBreakReminderPendingRepeatSync(false)
        cancelFallback(context)
        NotificationHelper.createChannel(context)
        NotificationHelper.showBreakReminder(context)
    }

    private fun cancelFallback(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, UnlockFallbackCheckReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            100,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
    }
}
