package me.lxb.writedone.ui.screens.calendar

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import me.lxb.writedone.data.model.CompletedNote
import me.lxb.writedone.ui.theme.ZcoolKuaiLeFont as handwritingFont
import me.lxb.writedone.ui.components.CalendarGrid
import me.lxb.writedone.ui.components.CompletedCard
import me.lxb.writedone.ui.theme.AppColors
import me.lxb.writedone.ui.theme.Dimens
import me.lxb.writedone.ui.theme.LocalAmbientProgress
import java.util.Date

@Composable
fun CalendarOverlay(
    calendarAnim: Animatable<Float, AnimationVector1D>,
    screenWidthPx: Float,
    bgColor: Color,
    selectedDate: Date,
    notes: List<CompletedNote>,
    onDateSelected: (Date) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    val t = LocalAmbientProgress.current
    val emptyColor = lerp(AppColors.textMuted, AppColors.darkTextMuted, t)
    val dividerColor = lerp(AppColors.border, AppColors.darkBorder, t)

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
                HorizontalDivider(color = dividerColor)

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
                        color = emptyColor,
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
    }
}
