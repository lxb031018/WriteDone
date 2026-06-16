package me.lxb.writedone.ui.screens.home

import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.provider.Settings
import androidx.compose.ui.platform.LocalContext
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
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
import androidx.compose.ui.platform.LocalView
import androidx.activity.compose.BackHandler
import kotlinx.coroutines.launch
import me.lxb.writedone.ambient.AmbientController
import me.lxb.writedone.ambient.AmbientStatus
import me.lxb.writedone.viewmodel.TimerStatus
import me.lxb.writedone.ui.components.CompletedSection
import me.lxb.writedone.ui.components.TimerInputCard
import me.lxb.writedone.ui.screens.calendar.CalendarOverlay
import me.lxb.writedone.ui.screens.legal.PrivacyPolicyPage
import me.lxb.writedone.ui.screens.legal.UserAgreementPage
import me.lxb.writedone.ui.screens.settings.SettingsDrawer
import me.lxb.writedone.ui.theme.AppColors
import me.lxb.writedone.ui.theme.Dimens
import me.lxb.writedone.ui.theme.LocalAmbientProgress
import me.lxb.writedone.ui.theme.LocalBreathingAlpha
import me.lxb.writedone.viewmodel.CompletedViewModel
import me.lxb.writedone.viewmodel.SettingsViewModel
import me.lxb.writedone.viewmodel.TimerViewModel
import kotlin.math.roundToInt

/**
 * Home screen — 1:1 port of `lib/features/home/home.dart`.
 *
 * Layout:
 *   - Portrait: top-to-bottom (TimerInputCard → CompletedSection).
 *   - Landscape: left-right (Completed | VerticalDivider | TimerInputCard).
 *   - Drawer slides in from the right, pushing the main content left.
 *   - Calendar overlay slides in from the left, on top of everything.
 *
 * Ambient (1:1 port of Flutter `_themeCtrl` + `_breathingCtrl` + `ThemeProgress`):
 *   - `themeAnim` drives a 1.5s easeInOut crossfade between light and dark themes,
 *     exposed as `LocalAmbientProgress.current` (t ∈ [0,1]) for `Color.lerp` calls
 *     in feature widgets.
 *   - `breathingAnim` is a 4s `0.15 ↔ 0.7` easeInOut sine loop, exposed as
 *     `LocalBreathingAlpha.current` for `BreathingWrapper` consumers.
 */
@Composable
fun HomeScreen(
    timerViewModel: TimerViewModel,
    completedViewModel: CompletedViewModel,
    settingsViewModel: SettingsViewModel,
    ambientController: AmbientController,
    modifier: Modifier = Modifier,
) {
    val timerState by timerViewModel.state.collectAsState()
    val completedState by completedViewModel.state.collectAsState()

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    val density = LocalDensity.current
    val context = LocalContext.current

    var screenWidthPx by remember { mutableFloatStateOf(1f) }
    val scope = rememberCoroutineScope()
    val drawerAnim = remember { Animatable(0f) }
    val calendarAnim = remember { Animatable(0f) }

    var showUserAgreement by remember { mutableStateOf(false) }
    var showPrivacyPolicy by remember { mutableStateOf(false) }

    fun animateDrawerTo(target: Float) {
        scope.launch {
            drawerAnim.animateTo(
                targetValue = target,
                animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
            )
        }
    }

    fun animateCalendarTo(target: Float) {
        scope.launch {
            calendarAnim.animateTo(
                targetValue = target,
                animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
            )
        }
    }

    // ── Ambient state machine ──
    val ambientState by ambientController.state.collectAsState()

    // Theme transition: 1.5s easeInOut (Flutter `_themeCtrl`).
    val themeAnim = remember { Animatable(0f) }
    LaunchedEffect(ambientState.status) {
        themeAnim.animateTo(
            targetValue = if (ambientState.status == AmbientStatus.Active) 1f else 0f,
            animationSpec = tween(durationMillis = 1500, easing = FastOutSlowInEasing),
        )
    }

    // Breathing alpha: 4s 0.15↔0.7 easeInOutSine (Flutter `_breathingAnim`).
    val breathingAnim = remember { Animatable(0.15f) }
    LaunchedEffect(ambientState.breathingEnabled) {
        if (ambientState.breathingEnabled) {
            breathingAnim.animateTo(
                targetValue = 0.7f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 4000, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse,
                ),
            )
        } else {
            breathingAnim.stop()
            breathingAnim.snapTo(0.15f)
        }
    }
    val breathingAlpha: State<Float>? = if (ambientState.breathingEnabled) breathingAnim.asState() else null
    val ambientProgress = themeAnim.value

    // Inline breathing flag (kept for breathingEnabled propagation to children).
    val breathingEnabled = ambientState.breathingEnabled

    // Enter ambient (dark mode + breathing) only when timer is running AND in landscape.
    LaunchedEffect(timerState.status, isLandscape) {
        if (timerState.status == TimerStatus.Running && isLandscape) {
            ambientController.enter()
        } else {
            ambientController.exit()
        }
    }

    // Keep screen on when timer is running AND in landscape (matches Flutter behavior).
    val view = LocalView.current
    LaunchedEffect(timerState.status, isLandscape) {
        view.keepScreenOn = timerState.status == TimerStatus.Running && isLandscape
    }

    // Y threshold (in px) above which a horizontal drag opens the drawer (legacy).
    // Below this Y, drag is routed by direction: rightward → calendar, leftward → drawer.
    val drawerDragThresholdPx = with(density) { 250.dp.toPx() }
    var dragStartY by remember { mutableFloatStateOf(0f) }
    var lastDragDirection by remember { mutableFloatStateOf(0f) }

    CompositionLocalProvider(
        LocalAmbientProgress provides ambientProgress,
        LocalBreathingAlpha provides breathingAlpha,
    ) {
        val bgColor = lerp(AppColors.bg, AppColors.darkBg, ambientProgress)
        val landscapeDividerColor = lerp(AppColors.border, AppColors.darkBorder, ambientProgress)

        Box(
            modifier = modifier
                .fillMaxSize()
                .background(bgColor)
                .onSizeChanged { screenWidthPx = it.width.toFloat() }
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragStart = { offset ->
                            dragStartY = offset.y
                            lastDragDirection = 0f
                        },
                        onDragEnd = {
                            if (drawerAnim.value > 0.001f) {
                                val target = if (drawerAnim.value > 0.5f) 1f else 0f
                                animateDrawerTo(target)
                                animateCalendarTo(0f)
                            } else {
                                when {
                                    dragStartY < drawerDragThresholdPx -> {
                                        val target = if (drawerAnim.value > 0.5f) 1f else 0f
                                        animateDrawerTo(target)
                                    }
                                    lastDragDirection > 0f -> {
                                        val target = if (calendarAnim.value > 0.5f) 1f else 0f
                                        animateCalendarTo(target)
                                        animateDrawerTo(0f)
                                    }
                                    lastDragDirection < 0f -> {
                                        val target = if (drawerAnim.value > 0.5f) 1f else 0f
                                        animateDrawerTo(target)
                                        animateCalendarTo(0f)
                                    }
                                    else -> {
                                        animateDrawerTo(0f)
                                        animateCalendarTo(0f)
                                    }
                                }
                            }
                        },
                    ) { _, dragAmount ->
                        if (drawerAnim.value > 0.001f) {
                            if (dragAmount != 0f) {
                                lastDragDirection = if (dragAmount > 0f) 1f else -1f
                            }
                            scope.launch {
                                val delta = -dragAmount / screenWidthPx
                                drawerAnim.snapTo((drawerAnim.value + delta).coerceIn(0f, 1f))
                            }
                        } else {
                            when {
                                dragStartY < drawerDragThresholdPx -> {
                                    if (dragAmount != 0f) {
                                        lastDragDirection = if (dragAmount > 0f) 1f else -1f
                                    }
                                    scope.launch {
                                        val delta = -dragAmount / screenWidthPx
                                        drawerAnim.snapTo((drawerAnim.value + delta).coerceIn(0f, 1f))
                                    }
                                }
                                dragAmount > 0f -> {
                                    lastDragDirection = 1f
                                    scope.launch {
                                        val delta = dragAmount / screenWidthPx
                                        calendarAnim.snapTo((calendarAnim.value + delta).coerceIn(0f, 1f))
                                    }
                                }
                                dragAmount < 0f -> {
                                    lastDragDirection = -1f
                                    scope.launch {
                                        val delta = -dragAmount / screenWidthPx
                                        drawerAnim.snapTo((drawerAnim.value + delta).coerceIn(0f, 1f))
                                    }
                                }
                            }
                        }
                    }
                },
        ) {
            // ── Layer 1: Main Content ──
            // Flutter: offset = -w * t  where w = SCREEN width (so Main is fully off-screen when t=1)
            // t=0(closed): offset = 0, t=1(open): offset = -w (shifted left off-screen)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .offset { IntOffset((-screenWidthPx * drawerAnim.value).roundToInt(), 0) }
                    .background(Color.Transparent),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(bgColor)
                        .statusBarsPadding(),
                ) {
                    if (isLandscape) {
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
                                color = landscapeDividerColor,
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

            // ── Drawer ──
            // Flutter: offset = w * (1 - t)  where w = SCREEN width
            // t=0(closed): offset = w (offscreen right), t=1(open): offset = 0 (visible)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .offset { IntOffset((screenWidthPx * (1f - drawerAnim.value)).roundToInt(), 0) }
                    .clipToBounds(),
            ) {
                SettingsDrawer(
                    onUserAgreement = { showUserAgreement = true },
                    onPrivacyPolicy = { showPrivacyPolicy = true },
                    onNotificationPermission = {
                        Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                            putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                            context.startActivity(this)
                        }
                    },
                    onExactAlarmPermission = {
                        Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                            context.startActivity(this)
                        }
                    },
                    onAutoStartPermission = {
                        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.fromParts("package", context.packageName, null)
                            context.startActivity(this)
                        }
                    },
                    modifier = Modifier.align(Alignment.CenterStart),
                )
            }

            // ── Layer 3: Calendar Overlay (slides in from LEFT) ──
            // Self-contained with its own swipe-to-close gesture handler.
            if (calendarAnim.value > 0f) {
                CalendarOverlay(
                    calendarAnim = calendarAnim,
                    screenWidthPx = screenWidthPx,
                    bgColor = bgColor,
                    selectedDate = completedState.selectedDate,
                    notes = completedState.notes,
                    onDateSelected = { completedViewModel.selectDate(it) },
                )
            }

            // ── Legal page overlays ──
            if (showUserAgreement) {
                BackHandler { showUserAgreement = false }
                UserAgreementPage(onBack = { showUserAgreement = false })
            }
            if (showPrivacyPolicy) {
                BackHandler { showPrivacyPolicy = false }
                PrivacyPolicyPage(onBack = { showPrivacyPolicy = false })
            }
        }
    }
}
