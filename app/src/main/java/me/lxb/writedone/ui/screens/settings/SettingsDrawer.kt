package me.lxb.writedone.ui.screens.settings

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.lxb.writedone.R
import me.lxb.writedone.ui.theme.AppColors
import me.lxb.writedone.ui.theme.Dimens
import me.lxb.writedone.viewmodel.SettingsViewModel

@Composable
fun SettingsDrawer(
    settingsViewModel: SettingsViewModel,
    onClose: () -> Unit,
    onAbout: () -> Unit,
    onUserAgreement: () -> Unit,
    onPrivacyPolicy: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val lockScreen by settingsViewModel.lockScreenEnabled.collectAsState()

    val handwritingFont = FontFamily(
        Font(
            googleFont = GoogleFont("ZCOOL KuaiLe"),
            fontProvider = GoogleFont.Provider(
                providerAuthority = "com.google.android.gms.fonts",
                providerPackage = "com.google.android.gms",
                certificates = R.array.com_google_android_gms_fonts_certs,
            ),
        ),
    )

    Column(
        modifier = modifier
            .width(280.dp)
            .fillMaxHeight()
            .padding(Dimens.pageH)
            .verticalScroll(rememberScrollState()),
    ) {
        Spacer(Modifier.height(32.dp))
        Text(
            text = "WriteDone",
            fontFamily = handwritingFont,
            fontSize = 28.sp,
            color = AppColors.text,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "粒时",
            fontFamily = handwritingFont,
            fontSize = 16.sp,
            color = AppColors.textMuted,
        )
        Spacer(Modifier.height(32.dp))
        HorizontalDivider(color = AppColors.border)
        Spacer(Modifier.height(16.dp))

        // Lock screen toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "计时时保持屏幕常亮",
                modifier = Modifier.weight(1f),
                fontFamily = handwritingFont,
                fontSize = 16.sp,
                color = AppColors.text,
            )
            Switch(
                checked = lockScreen,
                onCheckedChange = { settingsViewModel.setLockScreenEnabled(it) },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = AppColors.accent,
                    checkedTrackColor = AppColors.accentLight,
                ),
            )
        }
        Spacer(Modifier.height(16.dp))
        HorizontalDivider(color = AppColors.border)
        Spacer(Modifier.height(16.dp))

        // About
        Text(
            text = "ⓘ 关于",
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onAbout() }
                .padding(vertical = Dimens.gap),
            fontFamily = handwritingFont,
            fontSize = 16.sp,
            color = AppColors.text,
        )
        Spacer(Modifier.height(8.dp))
        HorizontalDivider(color = AppColors.border)
        Spacer(Modifier.height(16.dp))

        // ── 协议与政策 ──
        Text(
            text = "协议与政策",
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = Dimens.gapSm),
            fontFamily = handwritingFont,
            fontSize = 14.sp,
            color = AppColors.textMuted,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "用户协议",
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onUserAgreement() }
                .padding(vertical = Dimens.gap),
            fontFamily = handwritingFont,
            fontSize = 16.sp,
            color = AppColors.text,
        )
        Text(
            text = "隐私政策",
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onPrivacyPolicy() }
                .padding(vertical = Dimens.gap),
            fontFamily = handwritingFont,
            fontSize = 16.sp,
            color = AppColors.text,
        )
    }
}
