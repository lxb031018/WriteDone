package me.lxb.writedone.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import me.lxb.writedone.ui.theme.AppColors
import me.lxb.writedone.ui.theme.ZcoolKuaiLeFont as handwritingFont
import me.lxb.writedone.ui.theme.Dimens
import java.util.Calendar
import java.util.Date

@Composable
fun CalendarGrid(
    selectedDate: Date,
    onDateSelected: (Date) -> Unit,
    hasNotes: (Date) -> Boolean,
    reviewMode: Boolean = false,
    reviewRangeStart: Date? = null,
    reviewRangeEnd: Date? = null,
    onReviewDateSelected: (Date) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    var displayMonth by remember { mutableStateOf(Calendar.getInstance().apply { time = selectedDate }) }

    fun daysInMonth(cal: Calendar): Int {
        return cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    }

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
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "上月")
            }
            Text(
                text = "${displayMonth.get(Calendar.YEAR)}年${displayMonth.get(Calendar.MONTH) + 1}月",
                modifier = Modifier.weight(1f),
                fontFamily = handwritingFont,
                fontSize = 18.sp,
                textAlign = TextAlign.Center,
                color = AppColors.text,
            )
            IconButton(onClick = {
                displayMonth = Calendar.getInstance().apply {
                    time = displayMonth.time
                    add(Calendar.MONTH, 1)
                }
            }) {
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "下月")
            }
        }

        Row(modifier = Modifier.fillMaxWidth()) {
            listOf("日", "一", "二", "三", "四", "五", "六").forEach { day ->
                Text(
                    text = day,
                    modifier = Modifier.weight(1f),
                    fontFamily = handwritingFont,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    color = AppColors.textMuted,
                )
            }
        }

        val totalDays = daysInMonth(displayMonth)
        val firstDayOffset = (displayMonth.get(Calendar.DAY_OF_WEEK) - Calendar.SUNDAY + 7) % 7
        val rows = (totalDays + firstDayOffset + 6) / 7

        val selectedCal = remember(selectedDate) {
            Calendar.getInstance().apply { time = selectedDate }
        }

        val rangeStartMs = reviewRangeStart?.let {
            calForComparison(it)
        }
        val rangeEndMs = reviewRangeEnd?.let {
            calForComparison(it)
        }

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

                        val hasStart = reviewMode && rangeStartMs != null
                        val hasBoth = hasStart && rangeEndMs != null
                        val inRange = hasBoth &&
                            cellMs >= minOf(rangeStartMs!!, rangeEndMs!!) &&
                            cellMs <= maxOf(rangeStartMs!!, rangeEndMs!!)

                        val isRangeStart = hasBoth && cellMs == rangeStartMs
                        val isRangeEnd = hasBoth && cellMs == rangeEndMs
                        val isStartOnly = hasStart && !hasBoth && cellMs == rangeStartMs

                        val bgModifier = when {
                            inRange && !isRangeStart && !isRangeEnd ->
                                Modifier.background(AppColors.accent.copy(alpha = 0.12f))
                            else -> Modifier
                        }

                        val circleModifier = when {
                            isRangeStart || isRangeEnd || isStartOnly ->
                                Modifier.clip(CircleShape).background(AppColors.accent.copy(alpha = 0.35f))
                            isSelected ->
                                Modifier.clip(CircleShape).background(AppColors.accent.copy(alpha = 0.3f))
                            else -> Modifier
                        }

                        val textColor = when {
                            isRangeStart || isRangeEnd || isStartOnly -> AppColors.accentDeep
                            inRange -> AppColors.accentDeep
                            isSelected -> AppColors.accentDeep
                            else -> if (hasNotes(cellDate)) AppColors.text else AppColors.textMuted
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .padding(2.dp)
                                .then(bgModifier)
                                .then(circleModifier)
                                .clickable {
                                    if (reviewMode) onReviewDateSelected(cellDate)
                                    else onDateSelected(cellDate)
                                },
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = dayNum.toString(),
                                fontSize = 16.sp,
                                color = textColor,
                                fontWeight = if (inRange || isStartOnly || isSelected) FontWeight.Bold else FontWeight.Normal,
                                textAlign = TextAlign.Center,
                            )
                            if (isRangeStart) {
                                Text(
                                    text = "始",
                                    fontSize = 8.sp,
                                    color = AppColors.accentDeep,
                                    modifier = Modifier.align(Alignment.BottomCenter),
                                )
                            }
                            if (isStartOnly) {
                                Text(
                                    text = "始",
                                    fontSize = 8.sp,
                                    color = AppColors.accentDeep,
                                    modifier = Modifier.align(Alignment.BottomCenter),
                                )
                            }
                            if (isRangeEnd) {
                                Text(
                                    text = "终",
                                    fontSize = 8.sp,
                                    color = AppColors.accentDeep,
                                    modifier = Modifier.align(Alignment.BottomCenter),
                                )
                            }
                        }
                    } else {
                        Box(modifier = Modifier.weight(1f).aspectRatio(1f))
                    }
                }
            }
        }
    }
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
