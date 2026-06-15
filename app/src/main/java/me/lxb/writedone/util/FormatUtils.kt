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
            h > 0 -> "${h}小时${m}分钟"
            m > 0 -> "${m}分钟"
            else -> "${s}秒"
        }
    }

    fun dateLabel(today: Date, date: Date): String {
        val calToday = Calendar.getInstance().apply { time = today; set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0) }
        val calDate = Calendar.getInstance().apply { time = date; set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0) }
        val diff = ((calToday.timeInMillis - calDate.timeInMillis) / (1000 * 60 * 60 * 24)).toInt()
        return when (diff) {
            0 -> "今天"
            1 -> "昨天"
            else -> dateFormatM.format(date)
        }
    }

    fun time(dt: Date): String = timeFormat.format(dt)

    fun dateOnly(dt: Date): String = dateOnlyFormat.format(dt)

    private val timeFormat = SimpleDateFormat("HH:mm", Locale.CHINESE)
    private val dateOnlyFormat = SimpleDateFormat("yyyy-MM-dd", Locale.CHINESE)
    private val dateFormatM = SimpleDateFormat("M月d日", Locale.CHINESE)
}
