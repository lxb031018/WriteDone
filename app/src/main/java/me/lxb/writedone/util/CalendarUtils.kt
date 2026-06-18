package me.lxb.writedone.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.lxb.writedone.R
import me.lxb.writedone.data.model.CompletedNote
import me.lxb.writedone.domain.repository.NoteRepository
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

fun calForComparison(date: Date): Long {
    return Calendar.getInstance().apply {
        time = date
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
}

suspend fun exportSelectedDates(context: Context, repo: NoteRepository, selectedDates: Set<Long>) {
    if (selectedDates.isEmpty()) return
    val dateFmt = SimpleDateFormat("yyyy-MM-dd", Locale.CHINESE)
    val notesByDate = mutableMapOf<String, List<CompletedNote>>()

    for (dateMs in selectedDates.sorted()) {
        val endMs = dateMs + 86400000L
        val dayNotes = repo.getByDateRange(dateMs, endMs)
        val dateStr = dateFmt.format(Date(dateMs))
        notesByDate[dateStr] = dayNotes
    }

    val text = ExportFormatter.formatMultipleDates(notesByDate)

    withContext(Dispatchers.Main) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("Time Records", text))
        Toast.makeText(context, context.getString(R.string.calendar_copied_toast), Toast.LENGTH_SHORT).show()
    }
}
