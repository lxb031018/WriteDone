package me.lxb.writedone.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import me.lxb.writedone.data.repository.TimerStateRepository

class UnlockNotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_USER_PRESENT) return
        val repo = TimerStateRepository(context)
        if (!repo.loadBreakReminderPendingRepeat()) return
        repo.saveBreakReminderPendingRepeatSync(false)
        NotificationHelper.createChannel(context)
        NotificationHelper.showBreakReminder(context)
    }
}
