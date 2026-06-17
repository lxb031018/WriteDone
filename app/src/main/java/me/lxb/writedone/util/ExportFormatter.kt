package me.lxb.writedone.util

import me.lxb.writedone.data.model.CompletedNote
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ExportFormatter {
    private val timeFmt = SimpleDateFormat("HH:mm", Locale.CHINESE)
    private val dateFmt = SimpleDateFormat("yyyy-MM-dd", Locale.CHINESE)
    private val dayOfWeekFmt = SimpleDateFormat("EEE", Locale.US)

    fun formatRange(notes: List<CompletedNote>, startDate: Date, endDate: Date): String {
        if (notes.isEmpty()) {
            val label = if (startDate == endDate) dateFmt.format(startDate)
            else "${dateFmt.format(startDate)} ~ ${dateFmt.format(endDate)}"
            return "No records for $label."
        }

        val sorted = notes.sortedBy { it.createdAt }
        val isSingleDay = startDate == endDate ||
            dateFmt.format(startDate) == dateFmt.format(endDate)

        val sb = StringBuilder()
        if (isSingleDay) {
            sb.appendLine("===== Time Records =====")
            sb.appendLine("Date: ${dateFmt.format(startDate)} (${dayOfWeekFmt.format(startDate)})")
            sb.appendLine()
            appendNotes(sb, sorted)
        } else {
            sb.appendLine("===== Time Records =====")
            sb.appendLine("${dateFmt.format(startDate)} (${dayOfWeekFmt.format(startDate)}) ~ ${dateFmt.format(endDate)} (${dayOfWeekFmt.format(endDate)})")
            sb.appendLine()

            val grouped = sorted.groupBy { dateFmt.format(Date(it.createdAt)) }
            val sortedDates = grouped.keys.sortedBy { it }
            var totalAll = 0

            for (dateStr in sortedDates) {
                val dayNotes = grouped[dateStr]!!.sortedBy { it.createdAt }
                val dayDate = Date(dayNotes.first().createdAt)
                sb.appendLine("--- ${dateFmt.format(dayDate)} (${dayOfWeekFmt.format(dayDate)}) ---")
                val dayTotal = appendNotes(sb, dayNotes)
                totalAll += dayTotal
                sb.appendLine()
            }

            sb.appendLine("Total tracked: ${FormatUtils.duration(totalAll)}")
        }

        return sb.toString()
    }

    private fun appendNotes(sb: StringBuilder, notes: List<CompletedNote>): Int {
        var total = 0
        for (note in notes) {
            val startTime = timeFmt.format(Date(note.createdAt))
            val endTime = timeFmt.format(Date(note.createdAt + note.durationSeconds * 1000L))
            sb.appendLine("$startTime - $endTime | ${note.content}")
            total += note.durationSeconds
        }
        sb.appendLine("Total: ${FormatUtils.duration(total)}")
        appendGaps(sb, notes)
        return total
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
