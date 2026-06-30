package me.lxb.writedone.ui.screens.home

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.math.roundToInt
import me.lxb.writedone.R
import me.lxb.writedone.ui.components.CompletedSection
import me.lxb.writedone.ui.components.RainbowBreakOverlay
import me.lxb.writedone.ui.components.TimerComponent
import me.lxb.writedone.ui.components.TimerInputCard
import me.lxb.writedone.ui.theme.Dimens

@Composable
fun HomeContent(
    timerState: TimerUiState,
    completedState: CompletedUiState,
    timerViewModel: TimerViewModel,
    completedViewModel: CompletedViewModel,
    isLandscape: Boolean,
    isAmbientHidden: Boolean,
    breathingEnabled: Boolean,
    onTimerToggle: () -> Unit,
    onPeek: () -> Unit,
    screenWidthPx: Float,
    drawerAnim: Animatable<Float, AnimationVector1D>,
    calendarAnim: Animatable<Float, AnimationVector1D>,
    modifier: Modifier = Modifier,
) {
    val colorScheme = MaterialTheme.colorScheme

    Box(
        modifier = modifier
            .fillMaxSize()
            .offset { IntOffset((screenWidthPx * calendarAnim.value - screenWidthPx * drawerAnim.value).roundToInt(), 0) }
            .background(Color.Transparent),
    ) {
        val smoothRainbow = remember {
            val base = listOf(
                Color(0xFFFF6B6B),
                Color(0xFFFFE66D),
                Color(0xFF69DB7C),
                Color(0xFF74C0FC),
                Color(0xFFDA77F2),
            )
            val steps = 12
            buildList {
                for (i in base.indices)
                    for (s in 0 until steps)
                        add(lerp(base[i], base[(i + 1) % base.size], s.toFloat() / steps))
            }
        }
        var tick by remember { mutableIntStateOf(0) }
        LaunchedEffect(Unit) {
            while (true) {
                tick = (tick + 2) % 60
                delay(100)
            }
        }
        val rainbowBrush = remember(tick) {
            val colors = List(5) { i -> smoothRainbow[(tick + i * 12) % 60] }
            Brush.linearGradient(colors)
        }

        // ── Layer 1: Normal content (always composed, so TimerInputCard stays alive) ──
        val normalBg = when {
            isAmbientHidden -> Color.Black
            else -> colorScheme.background
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(normalBg)
                .then(
                    if (isAmbientHidden && !timerState.breakButtonVisible)
                        Modifier.alpha(0f)
                    else Modifier
                ),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .then(if (!isAmbientHidden) Modifier.statusBarsPadding() else Modifier),
            ) {
                if (isLandscape) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .padding(
                                    start = Dimens.pageH,
                                    top = Dimens.gap,
                                    end = Dimens.gap,
                                    bottom = 0.dp,
                                ),
                        ) {
                            CompletedSection(
                                notes = completedState.todayNotes,
                                headerText = stringResource(R.string.completed_header, completedState.todayNotes.size),
                                showHeader = true,
                                breathingEnabled = breathingEnabled,
                                onNoteContentChange = { id, content -> completedViewModel.updateNoteContent(id, content) },
                                modifier = Modifier.fillMaxSize(),
                            )
                        }
                        VerticalDivider(
                            modifier = Modifier
                                .width(1.dp)
                                .fillMaxHeight(),
                            color = colorScheme.outline,
                            thickness = 1.dp,
                        )
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .padding(
                                    start = Dimens.gap,
                                    top = Dimens.gap,
                                    end = Dimens.pageH,
                                    bottom = Dimens.pageBottom,
                                ),
                        ) {
                            TimerComponent(
                                state = timerState,
                                onToggle = onTimerToggle,
                                modifier = Modifier.fillMaxWidth(),
                            )

                            Spacer(Modifier.height(Dimens.gapLg))

                            TimerInputCard(
                                timerViewModel = timerViewModel,
                                completedViewModel = completedViewModel,
                                isLandscape = true,
                                breathingEnabled = breathingEnabled,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                } else {
                    TimerComponent(
                        state = timerState,
                        onToggle = onTimerToggle,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                start = Dimens.pageH,
                                top = Dimens.gapMd,
                                end = Dimens.pageH,
                            ),
                    )

                    Spacer(Modifier.height(Dimens.gapLg))

                    TimerInputCard(
                        timerViewModel = timerViewModel,
                        completedViewModel = completedViewModel,
                        isLandscape = false,
                        breathingEnabled = breathingEnabled,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = Dimens.pageH),
                    )

                    Spacer(Modifier.height(Dimens.gapLg))

                    CompletedSection(
                        notes = completedState.todayNotes,
                        headerText = stringResource(R.string.completed_header, completedState.todayNotes.size),
                        showHeader = true,
                        breathingEnabled = breathingEnabled,
                        onNoteContentChange = { id, content -> completedViewModel.updateNoteContent(id, content) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = Dimens.pageH)
                            .weight(1f),
                    )
                }
            }
        }

        // ── Layer 2: Ambient blackout (behind break overlay) ──
        if (isAmbientHidden && !timerState.breakButtonVisible) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onPeek,
                    ),
            )
        }

        // ── Layer 3: Break overlay on very top ──
        if (timerState.breakButtonVisible) {
            val bgColor = if (isAmbientHidden) Color.Black else colorScheme.background
            RainbowBreakOverlay(rainbowBrush, { timerViewModel.takeBreak() }, bgColor)
        }
    }
}
