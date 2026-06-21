package me.lxb.writedone

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import me.lxb.writedone.data.sync.SyncManager
import me.lxb.writedone.service.notification.NotificationHelper
import javax.inject.Inject

@HiltAndroidApp
class WriteDoneApplication : Application() {

    @Inject lateinit var syncManager: SyncManager

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createChannels(this)
    }
}
