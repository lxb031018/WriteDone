package me.lxb.writedone.ui.screens.calendar

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.lxb.writedone.data.model.CompletedNote
import me.lxb.writedone.ui.theme.ZcoolKuaiLeFont as handwritingFont
import me.lxb.writedone.ui.components.CalendarGrid
import me.lxb.writedone.ui.components.CompletedCard
import me.lxb.writedone.ui.theme.AppColors
import me.lxb.writedone.ui.theme.Dimens
import java.util.Date

@Composable
fun CalendarPage(
    selectedDate: Date,
    notes: List<CompletedNote>,
    onDateSelected: (Date) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        CalendarGrid(
            selectedDate = selectedDate,
            onDateSelected = onDateSelected,
            hasNotes = { false },
            modifier = Modifier.padding(horizontal = Dimens.pageH),
        )

        Spacer(Modifier.height(Dimens.gap))

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
