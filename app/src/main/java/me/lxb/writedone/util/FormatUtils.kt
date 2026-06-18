package me.lxb.writedone.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object FormatUtils {

    fun duration(totalSeconds: Int): String {
        val h = totalSeconds / 3600
        val m = (totalSeconds % 3600) / 60
        val s = totalSeconds % 60
        return when {
            h > 0 -> "${h}小时${m}分钟"
            m > 0 -> "${m}分钟"
            else -> "${s}秒"
        }
    }

    fun time(dt: Date): String = timeFormat.format(dt)

    private val timeFormat = SimpleDateFormat("HH:mm", Locale.CHINESE)
}
