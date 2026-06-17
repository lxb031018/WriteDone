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
import java.util.Calendar
import java.util.Date

@Composable
fun CalendarPage(
    selectedDate: Date,
    notes: List<CompletedNote>,
    onDateSelected: (Date) -> Unit,
) {
    val context = LocalContext.current
    var reviewMode by remember { mutableStateOf(false) }
    var reviewStart by remember { mutableStateOf<Date?>(null) }
    var reviewEnd by remember { mutableStateOf<Date?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        CalendarGrid(
            selectedDate = selectedDate,
            onDateSelected = onDateSelected,
            hasNotes = { false },
            reviewMode = reviewMode,
            reviewRangeStart = reviewStart,
            reviewRangeEnd = reviewEnd,
            onReviewDateSelected = { date ->
                if (reviewStart == null || (reviewStart != null && reviewEnd != null)) {
                    reviewStart = date
                    reviewEnd = null
                } else {
                    reviewEnd = date
                }
            },
            modifier = Modifier.padding(horizontal = Dimens.pageH),
        )

        Spacer(Modifier.height(Dimens.gapMd))

        // ── Review button area ──
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
                            reviewStart = null
                            reviewEnd = null
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
                        .background(AppColors.accent, RoundedCornerShape(Dimens.gap))
                        .clickable {
                            val start = reviewStart
                            val end = reviewEnd ?: reviewStart
                            if (start != null && end != null) {
                                exportRange(context, start, end)
                            }
                            reviewMode = false
                            reviewStart = null
                            reviewEnd = null
                        }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "确定",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
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
                        reviewStart = null
                        reviewEnd = null
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

private fun exportRange(context: Context, startDate: Date, endDate: Date) {
    val repo = NoteRepository(context)
    val startCal = Calendar.getInstance().apply { time = startDate }
    startCal.set(Calendar.HOUR_OF_DAY, 0)
    startCal.set(Calendar.MINUTE, 0)
    startCal.set(Calendar.SECOND, 0)
    startCal.set(Calendar.MILLISECOND, 0)
    val startMillis = startCal.timeInMillis

    val endCal = Calendar.getInstance().apply { time = endDate }
    endCal.set(Calendar.HOUR_OF_DAY, 0)
    endCal.set(Calendar.MINUTE, 0)
    endCal.set(Calendar.SECOND, 0)
    endCal.set(Calendar.MILLISECOND, 0)
    endCal.add(Calendar.DAY_OF_MONTH, 1)
    val endMillis = endCal.timeInMillis

    val notes = repo.getByDateRange(startMillis, endMillis)
    val text = ExportFormatter.formatRange(notes, startDate, endDate)

    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText("Time Records", text))
    Toast.makeText(context, "已复制到剪贴板", Toast.LENGTH_SHORT).show()
}
