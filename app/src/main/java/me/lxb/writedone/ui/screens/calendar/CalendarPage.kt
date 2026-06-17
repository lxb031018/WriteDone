package me.lxb.writedone.ui.screens.calendar

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.lxb.writedone.data.model.CompletedNote
import me.lxb.writedone.data.repository.NoteRepository
import me.lxb.writedone.ui.theme.ZcoolKuaiLeFont as handwritingFont
import me.lxb.writedone.ui.components.CalendarGrid
import me.lxb.writedone.ui.components.CompletedCard
import me.lxb.writedone.ui.theme.AppColors
import me.lxb.writedone.ui.theme.Dimens
import me.lxb.writedone.util.ExportFormatter
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun CalendarPage(
    selectedDate: Date,
    notes: List<CompletedNote>,
    onDateSelected: (Date) -> Unit,
) {
    val context = LocalContext.current
    var reviewMode by remember { mutableStateOf(false) }
    var selectedDates by remember { mutableStateOf(setOf<Long>()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        CalendarGrid(
            selectedDate = selectedDate,
            onDateSelected = onDateSelected,
            reviewMode = reviewMode,
            selectedDates = selectedDates,
            onToggleDate = { date ->
                val ms = calForComparison(date)
                selectedDates = if (ms in selectedDates) selectedDates - ms
                else selectedDates + ms
            },
            onRangeSelected = { start, end ->
                val startMs = calForComparison(start)
                val endMs = calForComparison(end)
                val range = generateSequence(startMs.coerceAtMost(endMs)) {
                    if (it < startMs.coerceAtLeast(endMs)) it + 86400000L else null
                }.toSet()
                selectedDates = selectedDates + range
            },
            modifier = Modifier.padding(horizontal = Dimens.pageH),
        )

        Spacer(Modifier.height(Dimens.gapMd))

        // ── Multi-select action area ──
        if (reviewMode) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = Dimens.pageH)
                        .background(AppColors.border, RoundedCornerShape(Dimens.gap))
                        .clickable {
                            reviewMode = false
                            selectedDates = emptySet()
                        }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "取消",
                        fontSize = 16.sp,
                        color = AppColors.text,
                        fontFamily = handwritingFont,
                    )
                }
                Spacer(Modifier.width(Dimens.gap))
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = Dimens.pageH)
                        .background(
                            if (selectedDates.isNotEmpty()) AppColors.accent else AppColors.border,
                            RoundedCornerShape(Dimens.gap),
                        )
                        .clickable(enabled = selectedDates.isNotEmpty()) {
                            exportSelectedDates(context, selectedDates)
                            reviewMode = false
                            selectedDates = emptySet()
                        }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = if (selectedDates.isNotEmpty()) "复制选中（${selectedDates.size}天）"
                        else "复制选中",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (selectedDates.isNotEmpty()) Color.White else AppColors.textMuted,
                        fontFamily = handwritingFont,
                    )
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimens.pageH)
                    .clickable {
                        reviewMode = true
                        selectedDates = emptySet()
                    }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "复盘",
                    fontSize = 16.sp,
                    color = AppColors.textSecondary,
                    fontFamily = handwritingFont,
                )
            }
        }

        Spacer(Modifier.height(Dimens.gapMd))

        if (notes.isEmpty()) {
            Text(
                text = "这天还没有记录",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Dimens.gapLg),
                fontFamily = handwritingFont,
                fontSize = 14.sp,
                color = AppColors.textMuted,
                textAlign = TextAlign.Center,
            )
        } else {
            notes.forEach { note ->
                CompletedCard(note = note, breathingEnabled = false)
                Spacer(Modifier.height(Dimens.gap))
            }
        }
    }
}

private fun exportSelectedDates(context: Context, selectedDates: Set<Long>) {
    if (selectedDates.isEmpty()) return
    val repo = NoteRepository(context)
    val dateFmt = SimpleDateFormat("yyyy-MM-dd", Locale.CHINESE)
    val notesByDate = mutableMapOf<String, List<CompletedNote>>()

    for (dateMs in selectedDates.sorted()) {
        val endMs = dateMs + 86400000L
        val dayNotes = repo.getByDateRange(dateMs, endMs)
        val dateStr = dateFmt.format(Date(dateMs))
        notesByDate[dateStr] = dayNotes
    }

    val text = ExportFormatter.formatMultipleDates(notesByDate)

    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText("Time Records", text))
    Toast.makeText(context, "已复制到剪贴板", Toast.LENGTH_SHORT).show()
}

private fun calForComparison(date: Date): Long {
    return Calendar.getInstance().apply {
        time = date
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
}
