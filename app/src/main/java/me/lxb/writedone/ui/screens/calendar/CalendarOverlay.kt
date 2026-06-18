package me.lxb.writedone.ui.screens.calendar

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
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
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.launch
import me.lxb.writedone.R
import me.lxb.writedone.data.model.CompletedNote
import me.lxb.writedone.domain.repository.NoteRepository
import me.lxb.writedone.ui.theme.ZcoolKuaiLeFont as handwritingFont
import me.lxb.writedone.ui.components.CalendarGrid
import me.lxb.writedone.ui.components.CompletedCard
import me.lxb.writedone.ui.theme.AppColors
import me.lxb.writedone.ui.theme.Dimens
import me.lxb.writedone.ui.theme.LocalAmbientProgress
import me.lxb.writedone.util.calForComparison
import me.lxb.writedone.util.exportSelectedDates
import java.util.Date

@Composable
fun CalendarOverlay(
    calendarAnim: Animatable<Float, AnimationVector1D>,
    screenWidthPx: Float,
    bgColor: Color,
    selectedDate: Date,
    notes: List<CompletedNote>,
    noteRepo: NoteRepository,
    onDateSelected: (Date) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val ambientProgress = LocalAmbientProgress.current
    val emptyColor = lerp(AppColors.textMuted, AppColors.darkTextMuted, ambientProgress)
    val reviewTextColor = lerp(AppColors.textSecondary, AppColors.darkTextSecondary, ambientProgress)
    val cancelBg = lerp(AppColors.border, AppColors.darkBorder, ambientProgress)
    val confirmBg = lerp(AppColors.accent, AppColors.darkAccent, ambientProgress)
    val disableBg = lerp(AppColors.border, AppColors.darkBorder, ambientProgress)
    val disableText = lerp(AppColors.textMuted, AppColors.darkTextMuted, ambientProgress)

    var reviewMode by remember { mutableStateOf(false) }
    var selectedDates by remember { mutableStateOf(setOf<Long>()) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(bgColor)
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        val target = if (calendarAnim.value > 0.5f) 1f else 0f
                        scope.launch {
                            calendarAnim.animateTo(
                                targetValue = target,
                                animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
                            )
                        }
                    },
                ) { _, dragAmount ->
                    scope.launch {
                        val delta = dragAmount / screenWidthPx
                        calendarAnim.snapTo((calendarAnim.value + delta).coerceIn(0f, 1f))
                    }
                }
            },
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
        ) {
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

                if (reviewMode) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = Dimens.pageH)
                                .background(cancelBg, RoundedCornerShape(Dimens.gap))
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
                                color = lerp(AppColors.text, AppColors.darkText, ambientProgress),
                                fontFamily = handwritingFont,
                            )
                        }
                        Spacer(Modifier.width(Dimens.gap))
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = Dimens.pageH)
                                .background(
                                    if (selectedDates.isNotEmpty()) confirmBg else disableBg,
                                    RoundedCornerShape(Dimens.gap),
                                )
                                .clickable(enabled = selectedDates.isNotEmpty()) {
                                    scope.launch {
                                        exportSelectedDates(context, noteRepo, selectedDates)
                                    }
                                    reviewMode = false
                                    selectedDates = emptySet()
                                }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = if (selectedDates.isNotEmpty()) stringResource(R.string.calendar_copy_selected_count, selectedDates.size)
                                else stringResource(R.string.calendar_copy_selected),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (selectedDates.isNotEmpty()) Color.White else disableText,
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
                            text = stringResource(R.string.calendar_review),
                            fontSize = 16.sp,
                            color = reviewTextColor,
                            fontFamily = handwritingFont,
                        )
                    }
                }

                Spacer(Modifier.height(Dimens.gap))

                if (notes.isEmpty()) {
                    Text(
                        text = stringResource(R.string.calendar_no_records),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Dimens.gapLg),
                        fontFamily = handwritingFont,
                        fontSize = 14.sp,
                        color = emptyColor,
                        textAlign = TextAlign.Center,
                    )
                } else {
                    notes.forEach { note ->
                        CompletedCard(
                            modifier = Modifier.padding(horizontal = Dimens.pageH),
                            note = note,
                            breathingEnabled = false,
                        )
                        Spacer(Modifier.height(Dimens.gap))
                    }
                }
            }
        }
    }
}


