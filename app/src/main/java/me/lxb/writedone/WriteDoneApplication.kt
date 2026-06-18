package me.lxb.writedone

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import me.lxb.writedone.service.notification.NotificationHelper

@HiltAndroidApp
class WriteDoneApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createChannels(this)
    }
}
