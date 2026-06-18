package me.lxb.writedone.ui.screens.home

import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.provider.Settings
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
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
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.res.stringResource
import androidx.activity.compose.BackHandler
import me.lxb.writedone.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlin.math.pow
import kotlin.math.sin
import me.lxb.writedone.service.ambient.AmbientController
import me.lxb.writedone.service.ambient.AmbientStatus
import me.lxb.writedone.domain.repository.NoteRepository
import me.lxb.writedone.ui.screens.home.TimerStatus
import me.lxb.writedone.ui.components.CompletedSection
import me.lxb.writedone.util.OemPermissionGuide
import me.lxb.writedone.ui.components.TimerInputCard
import me.lxb.writedone.ui.screens.calendar.CalendarOverlay
import me.lxb.writedone.ui.screens.legal.PrivacyPolicyPage
import me.lxb.writedone.ui.screens.legal.UserAgreementPage
import me.lxb.writedone.ui.screens.settings.SettingsDrawer
import me.lxb.writedone.ui.theme.AppColors
import me.lxb.writedone.ui.theme.Dimens
import me.lxb.writedone.ui.theme.LocalAmbientProgress
import me.lxb.writedone.ui.theme.LocalBreathingAlpha
import me.lxb.writedone.ui.screens.home.CompletedViewModel
import me.lxb.writedone.ui.screens.home.TimerViewModel
import me.lxb.writedone.ui.screens.settings.SettingsViewModel
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
 * Ambient:
 *   - `themeAnim` drives a 1.5s easeInOut crossfade between light and dark themes,
 *     exposed as `LocalAmbientProgress.current` (ambientProgress ∈ [0,1]) for `Color.lerp` calls.
 *   - Breathing alpha runs at ~10fps via `delay(100)` + smoothstep, decoupled from
 *     Choreographer, exposed as `LocalBreathingAlpha.current` for `BreathingWrapper`.
 */
@Composable
fun HomeScreen(
    timerViewModel: TimerViewModel,
    completedViewModel: CompletedViewModel,
    settingsViewModel: SettingsViewModel,
    ambientController: AmbientController,
    noteRepo: NoteRepository? = null,
    ambientProgress: Float = 0f,
    onAmbientProgressChange: (Float) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val completedState by completedViewModel.state.collectAsState()
    val autoDimBrightness by settingsViewModel.autoDimBrightness.collectAsState()
    val themeMode by settingsViewModel.themeMode.collectAsState()

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
    val themeAnim = remember { Animatable(ambientProgress) }
    LaunchedEffect(ambientState.status) {
        themeAnim.animateTo(
            targetValue = if (ambientState.status == AmbientStatus.Active) 1f else 0f,
            animationSpec = tween(durationMillis = 1500, easing = FastOutSlowInEasing),
        )
    }
    LaunchedEffect(themeAnim.value) {
        onAmbientProgressChange(themeAnim.value)
    }

    // Breathing alpha: |sin(t)|³ narrow-peak curve via lookup table at ~10fps.
    val breathingAlphaValue = remember { mutableFloatStateOf(0f) }
    val lookupTable = remember {
        FloatArray(256) { i ->
            val a = (i / 255f) * kotlin.math.PI.toFloat()
            sin(a).pow(3)
        }
    }
    LaunchedEffect(ambientState.breathingEnabled) {
        if (ambientState.breathingEnabled) {
            val periodMs = 4000L
            while (true) {
                delay(100L)
                val t = (System.currentTimeMillis() % periodMs) / periodMs.toFloat()
                breathingAlphaValue.floatValue = lookupTable[(t * 255).toInt()]
            }
        } else {
            breathingAlphaValue.floatValue = 0f
        }
    }
    val breathingAlpha: State<Float>? = if (ambientState.breathingEnabled) breathingAlphaValue else null
    val ambientProgress = themeAnim.value

    // Inline breathing flag (kept for breathingEnabled propagation to children).
    val breathingEnabled = ambientState.breathingEnabled

    // Keep screen on + ambient mode + status bar: subscribe to timer status (Idle↔Running only).
    val view = LocalView.current
    val window = (context as? Activity)?.window
    LaunchedEffect(isLandscape, ambientController, view) {
        var hideJob: Job? = null
        timerViewModel.state
            .map { it.status }
            .distinctUntilChanged()
            .collect { status ->
                val active = status == TimerStatus.Running && isLandscape
                view.keepScreenOn = active
                if (active) ambientController.enter() else ambientController.exit()
                window?.let { win ->
                    val controller = WindowInsetsControllerCompat(win, view)
                    if (active) {
                        controller.hide(WindowInsetsCompat.Type.statusBars())
                        if (autoDimBrightness) {
                            win.attributes = win.attributes.apply { screenBrightness = 0f }
                        }
                        val decorView = win.decorView
                        // Auto-hide status bar 1s after user pulls it down (legacy listener)
                        @Suppress("DEPRECATION")
                        decorView.setOnSystemUiVisibilityChangeListener { visibility ->
                            if (visibility and View.SYSTEM_UI_FLAG_FULLSCREEN == 0) {
                                Handler(Looper.getMainLooper()).removeCallbacksAndMessages(null)
                                decorView.postDelayed({
                                    WindowInsetsControllerCompat(win, view)
                                        .hide(WindowInsetsCompat.Type.statusBars())
                                }, 1000L)
                            }
                        }
                        // Fallback polling for API 30+ where listener may not fire
                        hideJob = launch {
                            while (true) {
                                delay(1000L)
                                val insets = WindowInsetsCompat.toWindowInsetsCompat(
                                    decorView.rootWindowInsets, decorView
                                )
                                if (insets.isVisible(WindowInsetsCompat.Type.statusBars())) {
                                    delay(1000L)
                                    WindowInsetsControllerCompat(win, view)
                                        .hide(WindowInsetsCompat.Type.statusBars())
                                }
                            }
                        }
                    } else {
                        @Suppress("DEPRECATION")
                        win.decorView.setOnSystemUiVisibilityChangeListener(null)
                        Handler(Looper.getMainLooper()).removeCallbacksAndMessages(null)
                        hideJob?.cancel()
                        hideJob = null
                        controller.show(WindowInsetsCompat.Type.statusBars())
                        win.attributes = win.attributes.apply { screenBrightness = -1f }
                    }
                }
            }
    }

    // Y threshold (in px) above which a horizontal drag opens the drawer (legacy).
    // Below this Y, drag is routed by direction: rightward → calendar, leftward → drawer.
    val drawerDragThresholdPx = with(density) { 250.dp.toPx() }
    var dragStartY by remember { mutableFloatStateOf(0f) }
    var lastDragDirection by remember { mutableFloatStateOf(0f) }

    CompositionLocalProvider(
        LocalAmbientProgress provides themeAnim.value,
        LocalBreathingAlpha provides breathingAlpha,
    ) {
        val colorScheme = MaterialTheme.colorScheme

        Box(
            modifier = modifier
                .fillMaxSize()
                .background(colorScheme.background)
                .onSizeChanged { screenWidthPx = it.width.toFloat() }
                .pointerInput(isLandscape) {
                    if (isLandscape) return@pointerInput
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
                                    if (calendarAnim.value > 0f) {
                                        lastDragDirection = 1f
                                        scope.launch {
                                            val delta = dragAmount / screenWidthPx
                                            calendarAnim.snapTo((calendarAnim.value + delta).coerceIn(0f, 1f))
                                        }
                                    } else {
                                        lastDragDirection = -1f
                                        scope.launch {
                                            val delta = -dragAmount / screenWidthPx
                                            drawerAnim.snapTo((drawerAnim.value + delta).coerceIn(0f, 1f))
                                        }
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
                    .offset { IntOffset((screenWidthPx * calendarAnim.value - screenWidthPx * drawerAnim.value).roundToInt(), 0) }
                    .background(Color.Transparent),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(colorScheme.background)
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
                                    headerText = stringResource(R.string.completed_header, completedState.notes.size),
                                    showHeader = true,
                                    breathingEnabled = breathingEnabled,
                                    onNoteBodyChange = { id, body -> completedViewModel.updateNoteBody(id, body) },
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
                                    isLandscape = true,
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
                            isLandscape = false,
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
                            headerText = stringResource(R.string.completed_header, completedState.notes.size),
                            showHeader = true,
                            breathingEnabled = breathingEnabled,
                            onNoteBodyChange = { id, body -> completedViewModel.updateNoteBody(id, body) },
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
                    autoDimBrightness = autoDimBrightness,
                    onToggleAutoDim = { settingsViewModel.setAutoDimBrightness(it) },
                    themeMode = themeMode,
                    onThemeModeChange = { settingsViewModel.setThemeMode(it) },
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
                        OemPermissionGuide.openAutoStartSettings(context)
                    },
                    onBatteryOptimization = {
                        OemPermissionGuide.openBatteryOptimizationSettings(context)
                    },
                    onLockScreenNotification = {
                        OemPermissionGuide.openLockScreenNotificationSettings(context)
                    },
                    modifier = Modifier.align(Alignment.CenterStart),
                )
            }

            // ── Back gesture: close drawer ──
            if (drawerAnim.value > 0f) {
                BackHandler { animateDrawerTo(0f) }
            }

            // ── Back gesture: close calendar overlay ──
            if (calendarAnim.value > 0f) {
                BackHandler { animateCalendarTo(0f) }
            }

            // ── Calendar Scrim ──
            if (calendarAnim.value > 0f) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = calendarAnim.value * 0.5f))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { animateCalendarTo(0f) },
                        ),
                )
            }

            // ── Layer 3: Calendar (slides in from LEFT, pushes content right) ──
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .offset { IntOffset((-screenWidthPx * (1f - calendarAnim.value)).roundToInt(), 0) }
                    .clipToBounds(),
            ) {
                CalendarOverlay(
                    calendarAnim = calendarAnim,
                    screenWidthPx = screenWidthPx,
                    bgColor = colorScheme.background,
                    selectedDate = completedState.selectedDate,
                    notes = completedState.notes,
                    noteRepo = noteRepo ?: error("noteRepo must be provided"),
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
