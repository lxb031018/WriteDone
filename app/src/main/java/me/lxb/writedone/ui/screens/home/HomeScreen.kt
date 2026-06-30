package me.lxb.writedone.ui.screens.home

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
import android.content.pm.PackageManager
import android.os.Build
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
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.core.content.ContextCompat
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalView
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import kotlinx.coroutines.delay
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlin.math.pow
import kotlin.math.sin
import me.lxb.writedone.service.ambient.AmbientController
import me.lxb.writedone.service.ambient.AmbientDisplayMode
import me.lxb.writedone.service.ambient.AmbientStatus
import me.lxb.writedone.service.ambient.FlatSensorMonitor
import me.lxb.writedone.domain.repository.NoteRepository
import me.lxb.writedone.ui.screens.home.TimerStatus
import me.lxb.writedone.util.OemPermissionGuide
import me.lxb.writedone.ui.theme.LocalAmbientProgress
import me.lxb.writedone.ui.theme.LocalBreathingAlpha
import me.lxb.writedone.ui.theme.LocalTimerPalette
import me.lxb.writedone.ui.theme.ThemeMode
import me.lxb.writedone.ui.theme.rococoPalettes
import me.lxb.writedone.data.sync.SyncManager
import me.lxb.writedone.ui.screens.home.CompletedViewModel
import me.lxb.writedone.ui.screens.home.TimerViewModel
import me.lxb.writedone.ui.screens.settings.SettingsViewModel

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
    flatSensorMonitor: FlatSensorMonitor,
    noteRepo: NoteRepository? = null,
    ambientProgress: Float = 0f,
    onAmbientProgressChange: (Float) -> Unit = {},
    onSyncSettings: () -> Unit = {},
    syncManager: SyncManager? = null,
    modifier: Modifier = Modifier,
) {
    val completedState by completedViewModel.state.collectAsState()
    val autoDimBrightness by settingsViewModel.autoDimBrightness.collectAsState()
    val breathingLampEnabled by settingsViewModel.breathingLampEnabled.collectAsState()
    val autoStartTimerOnLandscapeEnabled by settingsViewModel.autoStartTimerOnLandscapeEnabled.collectAsState()
    val autoStartTimerOnFlatEnabled by settingsViewModel.autoStartTimerOnFlatEnabled.collectAsState()
    val themeMode by settingsViewModel.themeMode.collectAsState()
    val timerState by timerViewModel.state.collectAsState()
    val draftText by completedViewModel.draftText.collectAsState()
    val context = LocalContext.current

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { }
    val onTimerToggle: () -> Unit = {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            timerViewModel.toggleTimer()
        }
    }

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    val density = LocalDensity.current

    var screenWidthPx by remember { mutableFloatStateOf(1f) }
    var isPeeking by remember { mutableStateOf(false) }
    var peekJob by remember { mutableStateOf<Job?>(null) }
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
    // Observe sync completion and refresh CompletedSection
    LaunchedEffect(syncManager) {
        if (syncManager == null) return@LaunchedEffect
        syncManager.state.collect { state ->
            if (state.syncCompletedVersion > 0) {
                completedViewModel.refresh()
            }
        }
    }

    val breathingAlpha: State<Float>? = if (ambientState.breathingEnabled) breathingAlphaValue else null
    val breathingEnabled = ambientState.breathingEnabled

    // Ambient mode + status bar: subscribe to timer status (Idle↔Running only).
    val view = LocalView.current
    val window = (context as? Activity)?.window
    LaunchedEffect(isLandscape, ambientController, view) {
        var hideJob: Job? = null
        timerViewModel.state
            .map { it.status }
            .distinctUntilChanged()
            .collect { status ->
                val active = status == TimerStatus.Running && isLandscape
                if (active) ambientController.enter(breathingLampEnabled) else ambientController.exit()
                window?.let { win ->
                    val controller = WindowInsetsControllerCompat(win, view)
                    if (active) {
                        controller.hide(WindowInsetsCompat.Type.systemBars())
                        val decorView = win.decorView
                        // Auto-hide status bar 1s after user pulls it down (legacy listener)
                        @Suppress("DEPRECATION")
                        decorView.setOnSystemUiVisibilityChangeListener { visibility ->
                            if (visibility and View.SYSTEM_UI_FLAG_FULLSCREEN == 0) {
                                Handler(Looper.getMainLooper()).removeCallbacksAndMessages(null)
                                decorView.postDelayed({
                                    WindowInsetsControllerCompat(win, view)
                                        .hide(WindowInsetsCompat.Type.systemBars())
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
                                        .hide(WindowInsetsCompat.Type.systemBars())
                                }
                            }
                        }
                    } else {
                        @Suppress("DEPRECATION")
                        win.decorView.setOnSystemUiVisibilityChangeListener(null)
                        Handler(Looper.getMainLooper()).removeCallbacksAndMessages(null)
                        hideJob?.cancel()
                        hideJob = null
                        controller.show(WindowInsetsCompat.Type.systemBars())
                    }
                }
            }
    }

    // Auto-start timer when entering landscape with toggle enabled
    LaunchedEffect(isLandscape) {
        if (isLandscape && autoStartTimerOnLandscapeEnabled) {
            if (timerViewModel.state.value.status == TimerStatus.Idle) {
                timerViewModel.start()
            }
        }
    }

    // Flat-detect auto-start: when user types something, start sensor;
    // if phone placed flat while text exists, auto-start timer (no pomodoro).
    var wasFlat by remember { mutableStateOf(false) }
    LaunchedEffect(draftText, isLandscape, autoStartTimerOnFlatEnabled) {
        val shouldMonitor = draftText.isNotEmpty() && !isLandscape && autoStartTimerOnFlatEnabled
        if (shouldMonitor) {
            flatSensorMonitor.start()
        } else {
            flatSensorMonitor.stop()
            wasFlat = false
        }
    }
    LaunchedEffect(Unit) {
        flatSensorMonitor.isFlat.collect { flat ->
            if (flat && !wasFlat) {
                wasFlat = true
                val currentTimerStatus = timerViewModel.state.value.status
                val currentDraftText = completedViewModel.draftText.value
                if (currentTimerStatus == TimerStatus.Idle && currentDraftText.isNotEmpty() && !isLandscape) {
                    timerViewModel.start()
                }
            }
            if (!flat) {
                wasFlat = false
            }
        }
    }

    // Stop flat sensor when timer starts running (auto-started or manual).
    LaunchedEffect(timerState.status) {
        if (timerState.status == TimerStatus.Running) {
            flatSensorMonitor.stop()
            wasFlat = false
        }
    }

    // Keep screen on only when ambient mode is active.
    LaunchedEffect(ambientState.status) {
        view.keepScreenOn = ambientState.status == AmbientStatus.Active
    }

    // Notify ViewModel when ambient mode activates/deactivates
    LaunchedEffect(ambientState.status) {
        timerViewModel.setAmbientMode(ambientState.status == AmbientStatus.Active)
        if (ambientState.status != AmbientStatus.Active) {
            peekJob?.cancel()
            isPeeking = false
        }
    }

    // Notify controller when breathing lamp setting changes
    LaunchedEffect(breathingLampEnabled) {
        ambientController.updateDisplayMode(breathingLampEnabled)
    }

    // Brightness control: dim screen when autoDim is enabled.
    // During peek, restore system brightness temporarily.
    LaunchedEffect(ambientState.status, autoDimBrightness, isPeeking) {
        val active = ambientState.status == AmbientStatus.Active
        if (active && !isPeeking && autoDimBrightness) {
            window?.attributes = window?.attributes?.apply { screenBrightness = 0f }
        } else if (!active || isPeeking) {
            window?.attributes = window?.attributes?.apply { screenBrightness = -1f }
        }
    }

    // Y threshold (in px) above which a horizontal drag opens the drawer (legacy).
    // Below this Y, drag is routed by direction: rightward → calendar, leftward → drawer.
    val drawerDragThresholdPx = with(density) { 250.dp.toPx() }
    var dragStartY by remember { mutableFloatStateOf(0f) }
    var lastDragDirection by remember { mutableFloatStateOf(0f) }

    val timerPalette = rococoPalettes[timerState.paletteIndex]

    CompositionLocalProvider(
        LocalAmbientProgress provides themeAnim.value,
        LocalBreathingAlpha provides breathingAlpha,
        LocalTimerPalette provides timerPalette,
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
            HomeContent(
                timerState = timerState,
                completedState = completedState,
                timerViewModel = timerViewModel,
                completedViewModel = completedViewModel,
                isLandscape = isLandscape,
                isAmbientHidden = ambientState.status == AmbientStatus.Active
                    && ambientState.displayMode == AmbientDisplayMode.Blackout && !isPeeking,
                breathingEnabled = breathingEnabled,
                onTimerToggle = onTimerToggle,
                onPeek = {
                    isPeeking = true
                    peekJob?.cancel()
                    peekJob = scope.launch {
                        delay(3000L)
                        isPeeking = false
                    }
                },
                screenWidthPx = screenWidthPx,
                drawerAnim = drawerAnim,
                calendarAnim = calendarAnim,
            )

            // ── Drawer & Calendar Overlays ──
            HomeOverlays(
                drawerAnim = drawerAnim,
                calendarAnim = calendarAnim,
                screenWidthPx = screenWidthPx,
                selectedDate = completedState.selectedDate,
                notes = completedState.notes,
                noteRepo = noteRepo ?: error("noteRepo must be provided"),
                onDrawerClose = { animateDrawerTo(0f) },
                onCalendarClose = { animateCalendarTo(0f) },
                onDateSelected = { completedViewModel.selectDate(it) },
                onNoteContentChange = { id, content -> completedViewModel.updateNoteContent(id, content) },
                autoDimBrightness = autoDimBrightness,
                onToggleAutoDim = { settingsViewModel.setAutoDimBrightness(it) },
                breathingLampEnabled = breathingLampEnabled,
                onToggleBreathingLamp = { settingsViewModel.setBreathingLampEnabled(it) },
                autoStartTimerOnLandscapeEnabled = autoStartTimerOnLandscapeEnabled,
                onToggleAutoStartTimerOnLandscape = { settingsViewModel.setAutoStartTimerOnLandscapeEnabled(it) },
                autoStartTimerOnFlatEnabled = autoStartTimerOnFlatEnabled,
                onToggleAutoStartTimerOnFlat = { settingsViewModel.setAutoStartTimerOnFlatEnabled(it) },
                themeMode = themeMode ?: ThemeMode.System,
                onThemeModeChange = { settingsViewModel.setThemeMode(it) },
                onUserAgreement = { showUserAgreement = true },
                onPrivacyPolicy = { showPrivacyPolicy = true },
                onSyncSettings = onSyncSettings,
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
                onAutoStartPermission = { OemPermissionGuide.openAutoStartSettings(context) },
                onBatteryOptimization = { OemPermissionGuide.openBatteryOptimizationSettings(context) },
                onLockScreenNotification = { OemPermissionGuide.openLockScreenNotificationSettings(context) },
                showUserAgreement = showUserAgreement,
                showPrivacyPolicy = showPrivacyPolicy,
                onCloseUserAgreement = { showUserAgreement = false },
                onClosePrivacyPolicy = { showPrivacyPolicy = false },
            )

        }
    }
}
