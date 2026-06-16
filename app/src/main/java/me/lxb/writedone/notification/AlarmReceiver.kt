package me.lxb.writedone.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.lxb.writedone.data.repository.TimerStateRepository

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        NotificationHelper.createChannel(context)
        NotificationHelper.showBreakReminder(context)
        CoroutineScope(Dispatchers.IO).launch {
            TimerStateRepository(context).saveBreakReminderSent(true)
        }
    }
}
