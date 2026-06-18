package me.lxb.writedone.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.lxb.writedone.data.repository.TimerStateRepository

class UnlockNotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_USER_PRESENT) return
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val repo = TimerStateRepository(context)
                if (!repo.loadBreakReminderPendingRepeat()) return@launch
                repo.saveBreakReminderPendingRepeat(false)
                NotificationHelper.createChannel(context)
                NotificationHelper.showBreakReminder(context)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
