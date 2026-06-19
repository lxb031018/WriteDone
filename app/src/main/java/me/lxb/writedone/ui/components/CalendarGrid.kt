package me.lxb.writedone.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.lxb.writedone.R
import me.lxb.writedone.domain.repository.NoteRepository
import me.lxb.writedone.ui.theme.AppColors
import me.lxb.writedone.ui.theme.LocalDarkTheme
import me.lxb.writedone.ui.theme.ZcoolKuaiLeFont as handwritingFont
import me.lxb.writedone.util.calForComparison
import java.util.Calendar
import java.util.Date

@Composable
fun CalendarGrid(
    selectedDate: Date,
    onDateSelected: (Date) -> Unit,
    noteRepo: NoteRepository,
    reviewMode: Boolean = false,
    selectedDates: Set<Long> = emptySet(),
    onToggleDate: ((Date) -> Unit)? = null,
    onLongPress: ((Date) -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val colorScheme = MaterialTheme.colorScheme
    val isDark = LocalDarkTheme.current
    var displayMonth by remember { mutableStateOf(Calendar.getInstance().apply { time = selectedDate }) }
    var noteDays by remember { mutableStateOf(setOf<Long>()) }
    val todayMs = remember {
        Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    val year = displayMonth.get(Calendar.YEAR)
    val month = displayMonth.get(Calendar.MONTH)
    LaunchedEffect(year, month) {
        val cal = Calendar.getInstance()
        cal.set(year, month, 1, 0, 0, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val startMs = cal.timeInMillis
        cal.add(Calendar.MONTH, 1)
        val endMs = cal.timeInMillis

        val notes = withContext(Dispatchers.IO) {
            noteRepo.getByDateRange(startMs, endMs)
        }
        val cal2 = Calendar.getInstance()
        noteDays = notes.mapTo(mutableSetOf()) {
            cal2.time = Date(it.createdAt)
            cal2.set(Calendar.HOUR_OF_DAY, 0)
            cal2.set(Calendar.MINUTE, 0)
            cal2.set(Calendar.SECOND, 0)
            cal2.set(Calendar.MILLISECOND, 0)
            cal2.timeInMillis
        }
    }

    fun daysInMonth(cal: Calendar): Int {
        return cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    }

    val totalDays = daysInMonth(displayMonth)
    val firstDayOffset = (displayMonth.get(Calendar.DAY_OF_WEEK) - Calendar.SUNDAY + 7) % 7
    val rows = (totalDays + firstDayOffset + 6) / 7



    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = {
                displayMonth = Calendar.getInstance().apply {
                    time = displayMonth.time
                    add(Calendar.MONTH, -1)
                }
            }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.calendar_prev_month))
            }
            val isCurrentMonth = displayMonth.get(Calendar.YEAR) == Calendar.getInstance().get(Calendar.YEAR) &&
                displayMonth.get(Calendar.MONTH) == Calendar.getInstance().get(Calendar.MONTH)
            Text(
                text = stringResource(R.string.calendar_month_header, displayMonth.get(Calendar.YEAR), displayMonth.get(Calendar.MONTH) + 1),
                modifier = Modifier.weight(1f),
                fontFamily = handwritingFont,
                fontSize = 18.sp,
                textAlign = TextAlign.Center,
                color = if (isCurrentMonth) colorScheme.onSurface else colorScheme.onSurfaceVariant,
            )
            IconButton(onClick = {
                displayMonth = Calendar.getInstance().apply {
                    time = displayMonth.time
                    add(Calendar.MONTH, 1)
                }
            }) {
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = stringResource(R.string.calendar_next_month))
            }
        }

        Row(modifier = Modifier.fillMaxWidth()) {
            listOf(
                stringResource(R.string.calendar_day_sun),
                stringResource(R.string.calendar_day_mon),
                stringResource(R.string.calendar_day_tue),
                stringResource(R.string.calendar_day_wed),
                stringResource(R.string.calendar_day_thu),
                stringResource(R.string.calendar_day_fri),
                stringResource(R.string.calendar_day_sat),
            ).forEach { day ->
                Text(
                    text = day,
                    modifier = Modifier.weight(1f),
                    fontFamily = handwritingFont,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    color = colorScheme.onSurfaceVariant,
                )
            }
        }

        val selectedCal = remember(selectedDate) {
            Calendar.getInstance().apply { time = selectedDate }
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
        ) {
            for (row in 0 until rows) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    for (col in 0 until 7) {
                        val dayNum = row * 7 + col - firstDayOffset + 1
                        if (dayNum in 1..totalDays) {
                            val cellDate = Calendar.getInstance().apply {
                                set(Calendar.YEAR, displayMonth.get(Calendar.YEAR))
                                set(Calendar.MONTH, displayMonth.get(Calendar.MONTH))
                                set(Calendar.DAY_OF_MONTH, dayNum)
                                set(Calendar.HOUR_OF_DAY, 0)
                                set(Calendar.MINUTE, 0)
                                set(Calendar.SECOND, 0)
                                set(Calendar.MILLISECOND, 0)
                            }.time
                            val cellMs = cellDate.time

                            val isSelected = !reviewMode &&
                                selectedCal.get(Calendar.YEAR) == displayMonth.get(Calendar.YEAR) &&
                                selectedCal.get(Calendar.MONTH) == displayMonth.get(Calendar.MONTH) &&
                                selectedCal.get(Calendar.DAY_OF_MONTH) == dayNum

                            val isSelectedOrPreview = cellMs in selectedDates
                            val isToday = cellMs == todayMs

                            val circleModifier = if (isSelectedOrPreview)
                                Modifier.clip(CircleShape).background(colorScheme.primary.copy(alpha = 0.3f))
                            else Modifier
                            val todayModifier = if (isToday)
                                Modifier.border(1.5.dp, colorScheme.primary, CircleShape)
                            else Modifier

                            val accentDeepColor = if (isDark) AppColors.darkAccentDeep else AppColors.accentDeep
                            val textColor = when {
                                isSelectedOrPreview -> accentDeepColor
                                isSelected -> accentDeepColor
                                isToday -> colorScheme.primary
                                cellMs in noteDays -> colorScheme.onSurface
                                else -> colorScheme.onSurfaceVariant
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .padding(2.dp)
                                    .then(circleModifier)
                                    .then(todayModifier)
                                    .then(
                                        if (reviewMode) {
                                            Modifier.combinedClickable(
                                                onClick = {
                                                    if (cellMs in noteDays) {
                                                        onToggleDate?.invoke(cellDate)
                                                    }
                                                }
                                            )
                                        } else {
                                            Modifier.combinedClickable(
                                                onClick = { onDateSelected(cellDate) },
                                                onLongClick = {
                                                    if (cellMs in noteDays) {
                                                        onLongPress?.invoke(cellDate)
                                                    }
                                                }
                                            )
                                        }
                                    ),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = dayNum.toString(),
                                    fontSize = 16.sp,
                                    color = textColor,
                                    fontWeight = if (isSelectedOrPreview || isSelected) FontWeight.Bold else FontWeight.Normal,
                                    textAlign = TextAlign.Center,
                                )
                            }
                        } else {
                            Box(modifier = Modifier.weight(1f).aspectRatio(1f))
                        }
                    }
                }
            }
        }
    }
}


