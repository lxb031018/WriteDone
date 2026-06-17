package me.lxb.writedone.util

import me.lxb.writedone.data.model.CompletedNote
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ExportFormatter {
    private val timeFmt = SimpleDateFormat("HH:mm", Locale.CHINESE)
    private val dateFmt = SimpleDateFormat("yyyy-MM-dd", Locale.CHINESE)
    private val dayOfWeekFmt = SimpleDateFormat("EEE", Locale.US)

    fun formatDaily(notes: List<CompletedNote>, date: Date): String {
        val sorted = notes.sortedBy { it.createdAt }
        if (sorted.isEmpty()) {
            return "No records for ${dateFmt.format(date)}."
        }

        val sb = StringBuilder()
        sb.appendLine("===== Time Records =====")
        sb.appendLine("Date: ${dateFmt.format(date)} (${dayOfWeekFmt.format(date)})")
        sb.appendLine()

        var totalSeconds = 0
        for (note in sorted) {
            val startTime = timeFmt.format(Date(note.createdAt))
            val endTime = timeFmt.format(Date(note.createdAt + note.durationSeconds * 1000L))
            sb.appendLine("$startTime - $endTime | ${note.content}")
            totalSeconds += note.durationSeconds
        }
        sb.appendLine()
        sb.appendLine("Total tracked: ${FormatUtils.duration(totalSeconds)}")

        appendGaps(sb, sorted)

        return sb.toString()
    }

    fun formatWeekly(notes: List<CompletedNote>, weekStart: Date, weekEnd: Date): String {
        if (notes.isEmpty()) return "No records for this week."

        val sb = StringBuilder()
        sb.appendLine("===== Time Records (Weekly) =====")
        sb.appendLine("${dateFmt.format(weekStart)} (${dayOfWeekFmt.format(weekStart)}) ~ ${dateFmt.format(weekEnd)} (${dayOfWeekFmt.format(weekEnd)})")
        sb.appendLine()

        val grouped = notes.groupBy { dateFmt.format(Date(it.createdAt)) }
        val sortedDates = grouped.keys.sorted()
        var weeklyTotal = 0

        for (dateStr in sortedDates) {
            val dayNotes = grouped[dateStr]!!.sortedBy { it.createdAt }
            val dayDate = Date(dayNotes.first().createdAt)
            sb.appendLine("--- ${dateFmt.format(dayDate)} (${dayOfWeekFmt.format(dayDate)}) ---")
            var dayTotal = 0
            for (note in dayNotes) {
                val startTime = timeFmt.format(Date(note.createdAt))
                val endTime = timeFmt.format(Date(note.createdAt + note.durationSeconds * 1000L))
                sb.appendLine("$startTime - $endTime | ${note.content}")
                dayTotal += note.durationSeconds
            }
            sb.appendLine("Day total: ${FormatUtils.duration(dayTotal)}")
            appendGaps(sb, dayNotes)
            sb.appendLine()
            weeklyTotal += dayTotal
        }

        sb.appendLine("Weekly total: ${FormatUtils.duration(weeklyTotal)}")

        return sb.toString()
    }

    private fun appendGaps(sb: StringBuilder, sortedNotes: List<CompletedNote>) {
        val gaps = mutableListOf<Triple<Long, Long, Long>>()
        for (i in 0 until sortedNotes.size - 1) {
            val currentEnd = sortedNotes[i].createdAt + sortedNotes[i].durationSeconds * 1000L
            val nextStart = sortedNotes[i + 1].createdAt
            if (nextStart > currentEnd) {
                gaps.add(Triple(currentEnd, nextStart, (nextStart - currentEnd) / 1000))
            }
        }
        if (gaps.isNotEmpty()) {
            sb.appendLine("Gaps:")
            for ((gapStart, gapEnd, gapSec) in gaps) {
                val start = timeFmt.format(Date(gapStart))
                val end = timeFmt.format(Date(gapEnd))
                sb.appendLine("  $start - $end (${gapSec / 60}min)")
            }
        }
    }

}
