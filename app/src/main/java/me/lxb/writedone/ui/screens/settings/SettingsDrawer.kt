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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Switch
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.lxb.writedone.R
import me.lxb.writedone.ui.theme.Dimens
import me.lxb.writedone.ui.theme.ThemeMode

@Composable
fun SettingsDrawer(
    autoDimBrightness: Boolean,
    onToggleAutoDim: (Boolean) -> Unit,
    breathingLampEnabled: Boolean,
    onToggleBreathingLamp: (Boolean) -> Unit,
    autoStartTimerOnLandscapeEnabled: Boolean,
    onToggleAutoStartTimerOnLandscape: (Boolean) -> Unit,
    autoStartTimerOnFlatEnabled: Boolean,
    onToggleAutoStartTimerOnFlat: (Boolean) -> Unit,
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
    modifier: Modifier = Modifier,
) {
    val colorScheme = MaterialTheme.colorScheme

    Column(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .background(colorScheme.background)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState()),
    ) {
        SettingsGuide()
        Spacer(Modifier.height(8.dp))
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
        DrawerToggle(
            text = stringResource(R.string.settings_breathing_lamp),
            checked = breathingLampEnabled,
            onCheckedChange = onToggleBreathingLamp,
        )
        DrawerToggle(
            text = stringResource(R.string.settings_auto_start_timer),
            checked = autoStartTimerOnLandscapeEnabled,
            onCheckedChange = onToggleAutoStartTimerOnLandscape,
        )
        DrawerToggle(
            text = stringResource(R.string.settings_auto_start_timer_on_flat),
            checked = autoStartTimerOnFlatEnabled,
            onCheckedChange = onToggleAutoStartTimerOnFlat,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "Tip：手机横放，俯仰角超过60°自动启用摆件模式和休息通知",
            modifier = Modifier.padding(horizontal = 16.dp),
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(12.dp))
        SectionHeader(stringResource(R.string.settings_theme))
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
        DrawerOption(
            text = stringResource(R.string.settings_theme_system),
            selected = themeMode == ThemeMode.System,
            onClick = { onThemeModeChange(ThemeMode.System) },
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
        Spacer(Modifier.height(12.dp))
        SectionHeader(stringResource(R.string.settings_sync))
        DrawerItem(
            text = stringResource(R.string.settings_sync),
            onClick = onSyncSettings,
        )
    }
}

@Composable
private fun SectionHeader(text: String) {
    val colorScheme = MaterialTheme.colorScheme
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

@Composable
private fun SettingsGuide() {
    val colorScheme = MaterialTheme.colorScheme
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Spacer(Modifier.height(12.dp))
        GuideSection("📌 便利贴", "输入待办事项，计时结束后自动归档至「已完成」\n长按已归档的便利贴即可编辑完整内容")
        GuideSection("⏱️ 计时器", "• 点击计时器：开始 / 结束计时\n• 休息提醒：累积计时达 25 分钟，全屏彩虹弹幕鼓励休息（建议先完成手头上的事就休息大概5分钟）\n• 横屏进入「摆件模式」时自动切换至番茄模式，可配合自动调暗亮度、呼吸灯、自动开始计时")
        GuideSection("🎨 主题", "支持浅色模式、深色模式、跟随系统三种主题\n横屏摆件时自动切换至深色氛围")
        GuideSection("📅 日历", "右滑进入日历页面\n长按进入多选模式，勾选多天后导出 JSON 摘要；粘贴至 AI 助手（如豆包/DeepSeek），聊一聊当日的生活质量")
        GuideSection("🌐 局域网同步", "通过热点在两台设备间同步已完成记录，数据只保存在本地")
        GuideSection("🔔 权限说明", "为保障计时结束时的提醒功能，建议授予设置页面提到的权限。")
        GuideSection("📁 隐私承诺", "WriteDone 仅读取本地数据，不会上传至云端。")
        Spacer(Modifier.height(8.dp))
        HorizontalDivider(color = colorScheme.outline.copy(alpha = 0.3f))
        Spacer(Modifier.height(8.dp))
        Text(
            text = "💡 开发者手记",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = colorScheme.onSurface,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "本人特别在意省电，常规使用几乎不耗电；「摆件模式」功耗略增。\napp非常具有人文关怀，鼓励休息，起身活动，缓解久坐疲劳。",
            fontSize = 13.sp,
            color = colorScheme.onSurfaceVariant,
            lineHeight = 20.sp,
        )
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun GuideSection(header: String, body: String) {
    val colorScheme = MaterialTheme.colorScheme
    Text(
        text = header,
        fontSize = 14.sp,
        fontWeight = FontWeight.SemiBold,
        color = colorScheme.onSurface,
    )
    Spacer(Modifier.height(4.dp))
    Text(
        text = body,
        fontSize = 13.sp,
        color = colorScheme.onSurfaceVariant,
        lineHeight = 20.sp,
    )
    Spacer(Modifier.height(12.dp))
}
