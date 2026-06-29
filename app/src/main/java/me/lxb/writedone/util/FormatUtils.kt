package me.lxb.writedone.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object FormatUtils {

    fun duration(totalSeconds: Int): String {
        val h = totalSeconds / 3600
        val m = (totalSeconds % 3600) / 60
        val s = totalSeconds % 60
        return when {
            h > 0 -> "${h}h${m}min"
            m > 0 -> "${m}min"
            else -> "${s}s"
        }
    }

    fun time(dt: Date): String = timeFormat.format(dt)

    fun formatDateWithDay(dt: Date): String {
        val cal = Calendar.getInstance().apply { time = dt }
        return String.format("%02d/%02d 周%s", cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH), DAY_NAMES[cal.get(Calendar.DAY_OF_WEEK) - 1])
    }

    private val timeFormat = SimpleDateFormat("HH:mm", Locale.CHINESE)
    private val DAY_NAMES = arrayOf("日", "一", "二", "三", "四", "五", "六")
}
