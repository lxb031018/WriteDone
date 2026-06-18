package me.lxb.writedone.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Switch
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.lxb.writedone.R
import me.lxb.writedone.ui.theme.AppColors
import me.lxb.writedone.ui.theme.Dimens
import me.lxb.writedone.ui.theme.LocalAmbientProgress
import me.lxb.writedone.ui.theme.ThemeMode

@Composable
fun SettingsDrawer(
    autoDimBrightness: Boolean,
    onToggleAutoDim: (Boolean) -> Unit,
    themeMode: ThemeMode,
    onThemeModeChange: (ThemeMode) -> Unit,
    onUserAgreement: () -> Unit,
    onPrivacyPolicy: () -> Unit,
    onNotificationPermission: () -> Unit,
    onExactAlarmPermission: () -> Unit,
    onAutoStartPermission: () -> Unit,
    onBatteryOptimization: () -> Unit,
    onLockScreenNotification: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colorScheme = MaterialTheme.colorScheme
    val ambientProgress = LocalAmbientProgress.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .background(colorScheme.background)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState()),
    ) {
        SectionHeader(stringResource(R.string.settings_notifications))
        DrawerItem(
            text = stringResource(R.string.settings_notification_permission),
            onClick = onNotificationPermission,
        )
        DrawerItem(
            text = stringResource(R.string.settings_exact_alarm_permission),
            onClick = onExactAlarmPermission,
        )
        DrawerItem(
            text = stringResource(R.string.settings_auto_start_permission),
            onClick = onAutoStartPermission,
        )
        DrawerItem(
            text = stringResource(R.string.settings_lock_screen_notification),
            onClick = onLockScreenNotification,
        )
        DrawerItem(
            text = stringResource(R.string.settings_battery_optimization),
            onClick = onBatteryOptimization,
        )
        Spacer(Modifier.height(12.dp))
        SectionHeader(stringResource(R.string.settings_battery_saver))
        DrawerToggle(
            text = stringResource(R.string.settings_auto_dim),
            checked = autoDimBrightness,
            onCheckedChange = onToggleAutoDim,
        )
        Spacer(Modifier.height(12.dp))
        SectionHeader(stringResource(R.string.settings_theme))
        DrawerOption(
            text = stringResource(R.string.settings_theme_system),
            selected = themeMode == ThemeMode.System,
            onClick = { onThemeModeChange(ThemeMode.System) },
        )
        DrawerOption(
            text = stringResource(R.string.settings_theme_light),
            selected = themeMode == ThemeMode.Light,
            onClick = { onThemeModeChange(ThemeMode.Light) },
        )
        DrawerOption(
            text = stringResource(R.string.settings_theme_dark),
            selected = themeMode == ThemeMode.Dark,
            onClick = { onThemeModeChange(ThemeMode.Dark) },
        )
        Spacer(Modifier.height(12.dp))
        SectionHeader(stringResource(R.string.settings_legal))
        DrawerItem(
            text = stringResource(R.string.settings_user_agreement),
            onClick = onUserAgreement,
        )
        DrawerItem(
            text = stringResource(R.string.settings_privacy_policy),
            onClick = onPrivacyPolicy,
        )
    }
}

@Composable
private fun SectionHeader(text: String) {
    val colorScheme = MaterialTheme.colorScheme
    val ambientProgress = LocalAmbientProgress.current
    Text(
        text = text,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = Dimens.gapSm),
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        color = colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun DrawerItem(text: String, onClick: () -> Unit) {
    val colorScheme = MaterialTheme.colorScheme
    val ambientProgress = LocalAmbientProgress.current
    Text(
        text = text,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = Dimens.gap),
        fontSize = 16.sp,
        color = colorScheme.onSurface,
    )
}

@Composable
private fun DrawerOption(text: String, selected: Boolean, onClick: () -> Unit) {
    val colorScheme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(start = 16.dp, end = 16.dp, top = Dimens.gapSm, bottom = Dimens.gapSm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(18.dp)
                .background(
                    color = if (selected) colorScheme.primary else colorScheme.outline,
                    shape = CircleShape,
                ),
            contentAlignment = Alignment.Center,
        ) {
            if (selected) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(Color.White, CircleShape),
                )
            }
        }
        Spacer(Modifier.width(12.dp))
        Text(
            text = text,
            fontSize = 16.sp,
            color = colorScheme.onSurface,
        )
    }
}

@Composable
private fun DrawerToggle(
    text: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme
    val ambientProgress = LocalAmbientProgress.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(start = 16.dp, end = 8.dp, top = Dimens.gapSm, bottom = Dimens.gapSm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = text,
            modifier = Modifier.weight(1f),
            fontSize = 16.sp,
            color = colorScheme.onSurface,
        )
        Spacer(Modifier.width(8.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
        )
    }
}
