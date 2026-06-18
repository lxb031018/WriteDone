package me.lxb.writedone.notification

import android.app.KeyguardManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.lxb.writedone.data.repository.TimerStateRepository

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                NotificationHelper.createChannel(context)
                val repo = TimerStateRepository(context)
                val keyguard = context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
                if (keyguard.isDeviceLocked) {
                    repo.saveBreakReminderPendingRepeat(true)
                } else {
                    repo.saveBreakReminderSent(true)
                }
                NotificationHelper.showBreakReminder(context)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
