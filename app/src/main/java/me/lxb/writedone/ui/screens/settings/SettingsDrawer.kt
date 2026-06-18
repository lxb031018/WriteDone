package me.lxb.writedone.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.runtime.Composable
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

@Composable
fun SettingsDrawer(
    autoDimBrightness: Boolean,
    onToggleAutoDim: (Boolean) -> Unit,
    onUserAgreement: () -> Unit,
    onPrivacyPolicy: () -> Unit,
    onNotificationPermission: () -> Unit,
    onExactAlarmPermission: () -> Unit,
    onAutoStartPermission: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val t = LocalAmbientProgress.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .background(lerp(AppColors.bg, AppColors.darkBg, t))
            .verticalScroll(rememberScrollState()),
    ) {
        Spacer(Modifier.height(24.dp))
        Header(stringResource(R.string.settings_title))
        Spacer(Modifier.height(16.dp))
        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(),
            color = lerp(AppColors.border, AppColors.darkBorder, t),
        )
        Spacer(Modifier.height(12.dp))
        SectionHeader(stringResource(R.string.settings_display))
        DrawerToggle(
            text = stringResource(R.string.settings_auto_dim),
            checked = autoDimBrightness,
            onCheckedChange = onToggleAutoDim,
        )
        Spacer(Modifier.height(12.dp))
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
private fun Header(text: String) {
    val t = LocalAmbientProgress.current
    Text(
        text = text,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        fontSize = 18.sp,
        fontWeight = FontWeight.SemiBold,
        color = lerp(AppColors.text, AppColors.darkText, t),
    )
}

@Composable
private fun SectionHeader(text: String) {
    val t = LocalAmbientProgress.current
    Text(
        text = text,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = Dimens.gapSm),
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        color = lerp(AppColors.textMuted, AppColors.darkTextMuted, t),
    )
}

@Composable
private fun DrawerItem(text: String, onClick: () -> Unit) {
    val t = LocalAmbientProgress.current
    Text(
        text = text,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = Dimens.gap),
        fontSize = 16.sp,
        color = lerp(AppColors.text, AppColors.darkText, t),
    )
}

@Composable
private fun DrawerToggle(
    text: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    val t = LocalAmbientProgress.current
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
            color = lerp(AppColors.text, AppColors.darkText, t),
        )
        Spacer(Modifier.width(8.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
        )
    }
}
