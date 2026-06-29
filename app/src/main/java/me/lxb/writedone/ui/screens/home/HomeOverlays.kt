package me.lxb.writedone.ui.screens.home

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.IntOffset
import kotlin.math.roundToInt
import me.lxb.writedone.data.model.CompletedNote
import me.lxb.writedone.domain.repository.NoteRepository
import me.lxb.writedone.ui.screens.calendar.CalendarOverlay
import me.lxb.writedone.ui.screens.legal.PrivacyPolicyPage
import me.lxb.writedone.ui.screens.legal.UserAgreementPage
import me.lxb.writedone.ui.screens.settings.SettingsDrawer
import me.lxb.writedone.ui.theme.ThemeMode
import java.util.Date

@Composable
fun HomeOverlays(
    drawerAnim: Animatable<Float, AnimationVector1D>,
    calendarAnim: Animatable<Float, AnimationVector1D>,
    screenWidthPx: Float,
    selectedDate: Date,
    notes: List<CompletedNote>,
    noteRepo: NoteRepository,
    onDrawerClose: () -> Unit,
    onCalendarClose: () -> Unit,
    onDateSelected: (Date) -> Unit,
    autoDimBrightness: Boolean,
    onToggleAutoDim: (Boolean) -> Unit,
    breathingLampEnabled: Boolean,
    onToggleBreathingLamp: (Boolean) -> Unit,
    autoStartTimerOnLandscapeEnabled: Boolean,
    onToggleAutoStartTimerOnLandscape: (Boolean) -> Unit,
    themeMode: ThemeMode,
    onThemeModeChange: (ThemeMode) -> Unit,
    onUserAgreement: () -> Unit,
    onPrivacyPolicy: () -> Unit,
    onSyncSettings: () -> Unit,
    onNotificationPermission: () -> Unit,
    onExactAlarmPermission: () -> Unit,
    onAutoStartPermission: () -> Unit,
    onBatteryOptimization: () -> Unit,
    onLockScreenNotification: () -> Unit,
    showUserAgreement: Boolean,
    showPrivacyPolicy: Boolean,
    onCloseUserAgreement: () -> Unit,
    onClosePrivacyPolicy: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colorScheme = MaterialTheme.colorScheme

    // ── Scrim (tap to close) ──
    if (drawerAnim.value > 0f) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = drawerAnim.value * 0.5f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDrawerClose,
                ),
        )
    }

    // ── Drawer ──
    Box(
        modifier = modifier
            .fillMaxSize()
            .offset { IntOffset((screenWidthPx * (1f - drawerAnim.value)).roundToInt(), 0) }
            .clipToBounds(),
    ) {
        SettingsDrawer(
            autoDimBrightness = autoDimBrightness,
            onToggleAutoDim = onToggleAutoDim,
            breathingLampEnabled = breathingLampEnabled,
            onToggleBreathingLamp = onToggleBreathingLamp,
            autoStartTimerOnLandscapeEnabled = autoStartTimerOnLandscapeEnabled,
            onToggleAutoStartTimerOnLandscape = onToggleAutoStartTimerOnLandscape,
            themeMode = themeMode,
            onThemeModeChange = onThemeModeChange,
            onUserAgreement = onUserAgreement,
            onPrivacyPolicy = onPrivacyPolicy,
            onSyncSettings = onSyncSettings,
            onNotificationPermission = onNotificationPermission,
            onExactAlarmPermission = onExactAlarmPermission,
            onAutoStartPermission = onAutoStartPermission,
            onBatteryOptimization = onBatteryOptimization,
            onLockScreenNotification = onLockScreenNotification,
            modifier = Modifier.align(Alignment.CenterStart),
        )
    }

    // ── Back gesture: close drawer ──
    if (drawerAnim.value > 0f) {
        BackHandler(onBack = onDrawerClose)
    }

    // ── Back gesture: close calendar overlay ──
    if (calendarAnim.value > 0f) {
        BackHandler(onBack = onCalendarClose)
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
                    onClick = onCalendarClose,
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
            selectedDate = selectedDate,
            notes = notes,
            noteRepo = noteRepo,
            onDateSelected = onDateSelected,
        )
    }

    // ── Legal page overlays ──
    if (showUserAgreement) {
        BackHandler(onBack = onCloseUserAgreement)
        UserAgreementPage(onBack = onCloseUserAgreement)
    }
    if (showPrivacyPolicy) {
        BackHandler(onBack = onClosePrivacyPolicy)
        PrivacyPolicyPage(onBack = onClosePrivacyPolicy)
    }
}
