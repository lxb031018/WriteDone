package me.lxb.writedone.util

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.widget.Toast

object OemPermissionGuide {

    fun openAutoStartSettings(context: Context) {
        val brand = Build.BRAND.lowercase()
        val candidates = mutableListOf<Intent>()

        when {
            brand.contains("xiaomi") || brand.contains("redmi") -> {
                candidates += componentIntent("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity")
            }
            brand.contains("huawei") -> {
                candidates += componentIntent("com.huawei.systemmanager", "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity")
                candidates += componentIntent("com.huawei.systemmanager", "com.huawei.systemmanager.appcontrol.activity.StartupAppControlActivity")
            }
            brand.contains("honor") -> {
                candidates += componentIntent("com.hihonor.systemmanager", "com.hihonor.systemmanager.startupmgr.ui.StartupNormalAppListActivity")
                candidates += componentIntent("com.hihonor.systemmanager", "com.hihonor.systemmanager.appcontrol.activity.StartupAppControlActivity")
                candidates += componentIntent("com.huawei.systemmanager", "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity")
            }
            brand.contains("oppo") || brand.contains("oneplus") || brand.contains("realme") -> {
                candidates += componentIntent("com.coloros.safecenter", "com.coloros.safecenter.permission.startup.StartupAppListActivity")
                candidates += componentIntent("com.coloros.safecenter", "com.coloros.safecenter.startupapp.StartupAppListActivity")
                candidates += componentIntent("com.oplus.safecenter", "com.oplus.safecenter.startupapp.StartupAppListActivity")
                candidates += componentIntent("com.oppo.safe", "com.oppo.safe.permission.startup.StartupAppListActivity")
            }
            brand.contains("vivo") || brand.contains("iqoo") -> {
                candidates += componentIntent("com.vivo.permissionmanager", "com.vivo.permissionmanager.activity.BgStartUpManagerActivity")
                candidates += componentIntent("com.vivo.permissionmanager", "com.vivo.permissionmanager.activity.PurviewTabActivity")
                candidates += componentIntent("com.iqoo.secure", "com.iqoo.secure.ui.phoneoptimize.AddWhiteListActivity")
            }
            brand.contains("lenovo") || brand.contains("zuk") || brand.contains("zui") -> {
                candidates += componentIntent("com.zui.safecenter", "com.zui.safecenter.MainActivity")
                candidates += componentIntent("com.zui.safecenter", "com.zui.safecenter.permission.PermissionAppListActivity")
                candidates += componentIntent("com.lenovo.safecenter", "com.lenovo.safecenter.MainTab.LeSafeMainActivity")
            }
        }

        candidates += componentIntent("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity")
        candidates += componentIntent("com.hihonor.systemmanager", "com.hihonor.systemmanager.startupmgr.ui.StartupNormalAppListActivity")
        candidates += componentIntent("com.huawei.systemmanager", "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity")
        candidates += componentIntent("com.coloros.safecenter", "com.coloros.safecenter.permission.startup.StartupAppListActivity")
        candidates += componentIntent("com.vivo.permissionmanager", "com.vivo.permissionmanager.activity.BgStartUpManagerActivity")
        candidates += componentIntent("com.zui.safecenter", "com.zui.safecenter.MainActivity")
        candidates += componentIntent("com.lenovo.safecenter", "com.lenovo.safecenter.MainTab.LeSafeMainActivity")

        candidates += Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.parse("package:${context.packageName}")
        }
        candidates += Intent(Settings.ACTION_SETTINGS)

        val opened = candidates.any { safeStartActivity(context, it) }
        if (!opened) {
            Toast.makeText(context, "无法直接跳转，请手动开启自启动", Toast.LENGTH_SHORT).show()
        }
    }

    fun openBatteryOptimizationSettings(context: Context) {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        if (powerManager.isIgnoringBatteryOptimizations(context.packageName)) {
            Toast.makeText(context, "已关闭电池优化", Toast.LENGTH_SHORT).show()
            return
        }

        val directIntent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
            data = Uri.parse("package:${context.packageName}")
        }
        if (safeStartActivity(context, directIntent)) return

        val listIntent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
        if (safeStartActivity(context, listIntent)) return

        safeStartActivity(
            context,
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.parse("package:${context.packageName}")
            },
        )
    }

    fun openLockScreenNotificationSettings(context: Context) {
        val channelIntent = Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS).apply {
            putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
            putExtra(Settings.EXTRA_CHANNEL_ID, "write_done_pomodoro")
        }
        if (safeStartActivity(context, channelIntent)) return

        val appIntent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
            putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
        }
        if (safeStartActivity(context, appIntent)) return

        safeStartActivity(
            context,
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.parse("package:${context.packageName}")
            },
        )
    }

    private fun componentIntent(pkg: String, cls: String): Intent {
        return Intent().setComponent(ComponentName(pkg, cls))
    }

    private fun safeStartActivity(context: Context, intent: Intent): Boolean {
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        if (intent.resolveActivity(context.packageManager) == null) return false
        return kotlin.runCatching {
            context.startActivity(intent)
        }.isSuccess
    }
}
