package me.lxb.writedone.util

import me.lxb.writedone.data.model.CompletedNote
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ExportFormatter {
    private val timeFmt = SimpleDateFormat("HH:mm", Locale.CHINESE)
    private val dayOfWeekFmt = SimpleDateFormat("EEE", Locale.US)

    fun formatMultipleDates(notesByDate: Map<String, List<CompletedNote>>): String {
        if (notesByDate.isEmpty()) return "没有选中日期"

        val sb = StringBuilder()
        sb.appendLine("===== 选中日期记录 =====")
        sb.appendLine()

        val sortedDates = notesByDate.keys.sortedBy { it }
        var totalAll = 0
        var hasAny = false

        for (dateStr in sortedDates) {
            val dayNotes = notesByDate[dateStr] ?: continue
            if (dayNotes.isEmpty()) continue
            hasAny = true
            val dayDate = Date(dayNotes.first().createdAt)
            sb.appendLine("--- ${dateStr} (${dayOfWeekFmt.format(dayDate)}) ---")
            val dayTotal = appendNotes(sb, dayNotes.sortedBy { it.createdAt })
            totalAll += dayTotal
            sb.appendLine()
        }

        if (!hasAny) return "选中日期均无记录"

        if (totalAll > 0) {
            sb.appendLine("总计时：${FormatUtils.duration(totalAll)}")
        }
        sb.appendLine("共选中：${notesByDate.size}天")
        return sb.toString()
    }

    private fun appendNotes(sb: StringBuilder, notes: List<CompletedNote>): Int {
        var total = 0
        for (note in notes) {
            val startTime = timeFmt.format(Date(note.createdAt))
            val endTime = timeFmt.format(Date(note.createdAt + note.durationSeconds * 1000L))
            sb.append("$startTime - $endTime | ${note.content}")
            if (note.body.isNotBlank()) {
                sb.append(" (${note.body})")
            }
            sb.appendLine()
            total += note.durationSeconds
        }
        sb.appendLine("Total: ${FormatUtils.duration(total)}")
        appendGaps(sb, notes)
        return total
    }

    fun toJsonForAI(notesByDate: Map<String, List<CompletedNote>>): String {
        val timeFmt = SimpleDateFormat("HH:mm", Locale.CHINESE)
        val arr = JSONArray()
        for ((dateStr, dayNotes) in notesByDate.toSortedMap()) {
            val dayObj = JSONObject().apply {
                put("date", dateStr)
                val records = JSONArray()
                for (note in dayNotes.sortedBy { it.createdAt }) {
                    records.put(JSONObject().apply {
                        put("start", timeFmt.format(Date(note.createdAt)))
                        put("end", timeFmt.format(Date(note.createdAt + note.durationSeconds * 1000L)))
                        put("content", note.content)
                        if (note.body.isNotBlank()) {
                            put("body", note.body)
                        }
                    })
                }
                put("records", records)
            }
            arr.put(dayObj)
        }
        return arr.toString(2)
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
