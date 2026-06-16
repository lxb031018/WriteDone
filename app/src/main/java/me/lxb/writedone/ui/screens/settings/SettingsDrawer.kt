package me.lxb.writedone.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.lxb.writedone.ui.theme.AppColors
import me.lxb.writedone.ui.theme.Dimens

@Composable
fun SettingsDrawer(
    onUserAgreement: () -> Unit,
    onPrivacyPolicy: () -> Unit,
    onNotificationPermission: () -> Unit,
    onExactAlarmPermission: () -> Unit,
    onAutoStartPermission: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .verticalScroll(rememberScrollState()),
    ) {
        Spacer(Modifier.height(24.dp))
        Header("设置")
        Spacer(Modifier.height(16.dp))
        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(),
            color = AppColors.border,
        )
        Spacer(Modifier.height(12.dp))
        SectionHeader("通知")
        DrawerItem(
            text = "通知权限",
            onClick = onNotificationPermission,
        )
        DrawerItem(
            text = "精确闹钟权限",
            onClick = onExactAlarmPermission,
        )
        DrawerItem(
            text = "自启动权限",
            onClick = onAutoStartPermission,
        )
        Spacer(Modifier.height(12.dp))
        SectionHeader("协议与政策")
        DrawerItem(
            text = "用户协议",
            onClick = onUserAgreement,
        )
        DrawerItem(
            text = "隐私政策",
            onClick = onPrivacyPolicy,
        )
    }
}

@Composable
private fun Header(text: String) {
    Text(
        text = text,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        fontSize = 18.sp,
        fontWeight = FontWeight.SemiBold,
        color = AppColors.text,
    )
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = Dimens.gapSm),
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        color = AppColors.textMuted,
    )
}

@Composable
private fun DrawerItem(text: String, onClick: () -> Unit) {
    Text(
        text = text,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = Dimens.gap),
        fontSize = 16.sp,
        color = AppColors.text,
    )
}
