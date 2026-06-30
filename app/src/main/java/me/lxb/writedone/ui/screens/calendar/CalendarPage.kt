package me.lxb.writedone.ui.screens.calendar

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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import me.lxb.writedone.R
import me.lxb.writedone.data.model.CompletedNote
import me.lxb.writedone.domain.repository.NoteRepository
import me.lxb.writedone.ui.theme.ZcoolKuaiLeFont as handwritingFont
import me.lxb.writedone.ui.components.CalendarGrid
import me.lxb.writedone.ui.components.CompletedCard
import me.lxb.writedone.ui.theme.AppColors
import me.lxb.writedone.ui.theme.Dimens
import kotlinx.coroutines.launch
import me.lxb.writedone.util.calForComparison
import androidx.compose.material3.MaterialTheme
import me.lxb.writedone.util.copySelectedDatesAsJson
import java.util.Date

@Composable
fun CalendarPage(
    selectedDate: Date,
    notes: List<CompletedNote>,
    noteRepo: NoteRepository,
    onDateSelected: (Date) -> Unit,
    onNoteContentChange: ((Long, String) -> Unit)? = null,
) {
    val colorScheme = MaterialTheme.colorScheme
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
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
            noteRepo = noteRepo,
            reviewMode = reviewMode,
            selectedDates = selectedDates,
            onToggleDate = { date ->
                val ms = calForComparison(date)
                selectedDates = if (ms in selectedDates) selectedDates - ms
                else selectedDates + ms
            },
            onLongPress = { date ->
                val ms = calForComparison(date)
                selectedDates = selectedDates + ms
                reviewMode = true
            },
            modifier = Modifier.padding(horizontal = Dimens.pageH),
        )

        Spacer(Modifier.height(Dimens.gapMd))

        if (reviewMode) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = Dimens.pageH)
                        .background(colorScheme.outline, RoundedCornerShape(Dimens.gap))
                        .clickable {
                            reviewMode = false
                            selectedDates = emptySet()
                        }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stringResource(R.string.calendar_cancel),
                        fontSize = 16.sp,
                        color = colorScheme.onSurface,
                        fontFamily = handwritingFont,
                    )
                }
                Spacer(Modifier.width(Dimens.gap))
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = Dimens.pageH)
                        .background(
                            if (selectedDates.isNotEmpty()) colorScheme.primary else colorScheme.outline,
                            RoundedCornerShape(Dimens.gap),
                        )
                        .clickable(enabled = selectedDates.isNotEmpty()) {
                            scope.launch {
                                copySelectedDatesAsJson(context, noteRepo, selectedDates)
                                reviewMode = false
                                selectedDates = emptySet()
                            }
                        }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = if (selectedDates.isNotEmpty()) stringResource(R.string.calendar_export_selected_count, selectedDates.size)
                        else stringResource(R.string.calendar_export_selected),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (selectedDates.isNotEmpty()) Color.White else colorScheme.onSurfaceVariant,
                        fontFamily = handwritingFont,
                    )
                }
            }
        }

        Spacer(Modifier.height(Dimens.gapMd))

        if (notes.isEmpty()) {
            Text(
                text = stringResource(R.string.calendar_no_records),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Dimens.gapLg),
                fontFamily = handwritingFont,
                fontSize = 14.sp,
                color = colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        } else {
            notes.forEach { note ->
                CompletedCard(
                    modifier = Modifier.padding(horizontal = Dimens.pageH),
                    note = note,
                    breathingEnabled = false,
                    onContentChange = onNoteContentChange,
                )
                Spacer(Modifier.height(Dimens.gap))
            }
        }
    }
}


