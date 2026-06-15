package me.lxb.writedone.ui.screens.home

import android.content.res.Configuration
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import me.lxb.writedone.ui.components.CompletedSection
import me.lxb.writedone.ui.components.TimerInputCard
import me.lxb.writedone.ui.screens.settings.SettingsDrawer
import me.lxb.writedone.ui.theme.AppColors
import me.lxb.writedone.ui.theme.Dimens
import me.lxb.writedone.viewmodel.CompletedViewModel
import me.lxb.writedone.viewmodel.SettingsViewModel
import me.lxb.writedone.viewmodel.TimerStatus
import me.lxb.writedone.viewmodel.TimerViewModel
import kotlin.math.roundToInt

/**
 * Home screen — 1:1 port of `lib/features/home/home.dart`.
 *
 * Layout:
 *   - Portrait: top-to-bottom (TimerInputCard → CompletedSection).
 *   - Landscape: left-right (Completed | VerticalDivider | TimerInputCard).
 *   - Drawer slides in from the left; both layers driven by a single
 *     `detectHorizontalDragGestures` on the root Box and an `Animatable`
 *     that snaps with `tween(300, FastOutSlowInEasing)`.
 *
 *   - Flutter: offset_drawer = w * (1 - t), offset_main = -w * t.
 */
@Composable
fun HomeScreen(
    timerViewModel: TimerViewModel,
    completedViewModel: CompletedViewModel,
    settingsViewModel: SettingsViewModel,
    onNavigateToCalendar: () -> Unit,
    onNavigateToAbout: () -> Unit,
    onNavigateToUserAgreement: () -> Unit,
    onNavigateToPrivacyPolicy: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val timerState by timerViewModel.state.collectAsState()
    val completedState by completedViewModel.state.collectAsState()

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    val density = LocalDensity.current
    val drawerWidthDp = 280.dp
    val drawerWidthPx = with(density) { drawerWidthDp.toPx() }

    var screenWidthPx by remember { mutableFloatStateOf(1f) }
    val scope = rememberCoroutineScope()
    val drawerAnim = remember { Animatable(0f) }

    // Ambient breathing: after 15s of timer running
    val breathingEnabled =
        timerState.status == TimerStatus.Running && timerState.elapsedSeconds >= 15

    fun animateDrawerTo(target: Float) {
        scope.launch {
            drawerAnim.animateTo(
                targetValue = target,
                animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
            )
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .onSizeChanged { screenWidthPx = it.width.toFloat() }
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        val target = if (drawerAnim.value > 0.5f) 1f else 0f
                        animateDrawerTo(target)
                    },
                ) { _, dragAmount ->
                    scope.launch {
                        val delta = -dragAmount / screenWidthPx
                        drawerAnim.snapTo((drawerAnim.value + delta).coerceIn(0f, 1f))
                    }
                }
            },
    ) {
        // ── Layer 1: Drawer ──
        // Flutter: offset = w * (1 - t)
        // t=0(closed): offset = w (offscreen right), t=1(open): offset = 0 (visible)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset { IntOffset((drawerWidthPx * (1f - drawerAnim.value)).roundToInt(), 0) }
                .clipToBounds(),
        ) {
            SettingsDrawer(
                settingsViewModel = settingsViewModel,
                onClose = { animateDrawerTo(0f) },
                onCalendar = onNavigateToCalendar,
                onAbout = onNavigateToAbout,
                onUserAgreement = onNavigateToUserAgreement,
                onPrivacyPolicy = onNavigateToPrivacyPolicy,
                modifier = Modifier.align(Alignment.CenterStart),
            )
        }

        // ── Scrim (tap to close) ──
        if (drawerAnim.value > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = drawerAnim.value * 0.5f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { animateDrawerTo(0f) },
                    ),
            )
        }

        // ── Layer 2: Main Content ──
        // Flutter: offset = -w * t
        // t=0(closed): offset = 0, t=1(open): offset = -w (shifted left)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset { IntOffset((-drawerWidthPx * drawerAnim.value).roundToInt(), 0) }
                .background(Color.Transparent),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(AppColors.bg)
                    .statusBarsPadding(),
            ) {
                if (isLandscape) {
                    // Flutter home.dart _buildLandscape
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                    ) {
                        // Left column: Completed list
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
                                notes = completedState.notes,
                                headerText = "已完成 — ${completedState.notes.size}",
                                showHeader = true,
                                breathingEnabled = breathingEnabled,
                                modifier = Modifier.fillMaxSize(),
                            )
                        }
                        VerticalDivider(
                            modifier = Modifier
                                .width(1.dp)
                                .fillMaxHeight(),
                            color = lerp(AppColors.border, AppColors.darkBorder, 0f),
                            thickness = 1.dp,
                        )
                        // Right column: Timer + Input
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .verticalScroll(rememberScrollState())
                                .padding(
                                    start = Dimens.gap,
                                    top = Dimens.gap,
                                    end = Dimens.pageH,
                                    bottom = Dimens.pageBottom,
                                ),
                        ) {
                            Spacer(Modifier.height(Dimens.gapMd))
                            TimerInputCard(
                                timerViewModel = timerViewModel,
                                completedViewModel = completedViewModel,
                                breathingEnabled = breathingEnabled,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                } else {
                    // Portrait
                    TimerInputCard(
                        timerViewModel = timerViewModel,
                        completedViewModel = completedViewModel,
                        breathingEnabled = breathingEnabled,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                start = Dimens.pageH,
                                top = Dimens.gapMd,
                                end = Dimens.pageH,
                            ),
                    )

                    Spacer(Modifier.height(Dimens.gapLg))

                    CompletedSection(
                        notes = completedState.notes,
                        headerText = "已完成 — ${completedState.notes.size}",
                        showHeader = true,
                        breathingEnabled = breathingEnabled,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = Dimens.pageH)
                            .weight(1f),
                    )
                }
            }
        }
    }
}
