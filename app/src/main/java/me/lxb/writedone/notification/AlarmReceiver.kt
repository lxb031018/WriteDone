package me.lxb.writedone.notification

import android.app.KeyguardManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import me.lxb.writedone.data.repository.TimerStateRepository

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        NotificationHelper.createChannel(context)
        val repo = TimerStateRepository(context)
        val keyguard = context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        if (keyguard.isDeviceLocked) {
            repo.saveBreakReminderPendingRepeatSync(true)
            NotificationHelper.showBreakReminder(context)
        } else {
            repo.saveBreakReminderSentSync(true)
            NotificationHelper.showBreakReminder(context)
        }
    }
}
