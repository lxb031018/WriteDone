package me.lxb.writedone.data.sync

import android.content.ContentResolver
import android.provider.Settings
import java.net.Inet4Address
import java.net.NetworkInterface

data class DeviceInfo(
    val deviceId: String,
    val deviceName: String,
    val ipAddress: String,
) {
    companion object {
        fun getThis(contentResolver: ContentResolver): DeviceInfo {
            val deviceId = Settings.Secure.getString(
                contentResolver, Settings.Secure.ANDROID_ID
            ) ?: ""
            val deviceName = android.os.Build.MODEL
            val ip = getLocalIpAddress()
            return DeviceInfo(
                deviceId = deviceId,
                deviceName = deviceName,
                ipAddress = ip,
            )
        }

        private fun getLocalIpAddress(): String {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            while (interfaces.hasMoreElements()) {
                val networkInterface = interfaces.nextElement()
                if (networkInterface.isLoopback || !networkInterface.isUp) continue
                val addresses = networkInterface.inetAddresses
                while (addresses.hasMoreElements()) {
                    val addr = addresses.nextElement()
                    if (addr is Inet4Address && !addr.isLoopbackAddress) {
                        return addr.hostAddress ?: ""
                    }
                }
            }
            return "127.0.0.1"
        }
    }
}
