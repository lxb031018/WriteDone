package me.lxb.writedone.notification

import android.app.AlarmManager
import android.app.KeyguardManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import me.lxb.writedone.data.repository.TimerStateRepository

class UnlockFallbackCheckReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val repo = TimerStateRepository(context)
        if (!repo.loadBreakReminderPendingRepeat()) return
        val keyguard = context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        if (keyguard.isDeviceLocked) {
            scheduleNext(context)
            return
        }
        repo.saveBreakReminderPendingRepeatSync(false)
        NotificationHelper.createChannel(context)
        NotificationHelper.showBreakReminder(context)
    }

    private fun scheduleNext(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, UnlockFallbackCheckReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            100,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        val triggerTime = System.currentTimeMillis() + 30_000L
        if (alarmManager.canScheduleExactAlarms()) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent,
            )
        } else {
            alarmManager.setAlarmClock(
                AlarmManager.AlarmClockInfo(triggerTime, null),
                pendingIntent,
            )
        }
    }
}
