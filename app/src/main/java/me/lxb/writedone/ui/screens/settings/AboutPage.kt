package me.lxb.writedone.ui.screens.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.activity.compose.BackHandler
import me.lxb.writedone.R
import me.lxb.writedone.ui.screens.legal.PrivacyPolicyPage
import me.lxb.writedone.ui.screens.legal.UserAgreementPage
import me.lxb.writedone.ui.theme.AppColors
import me.lxb.writedone.ui.theme.Dimens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutPage(onBack: () -> Unit) {
    var showUserAgreement by remember { mutableStateOf(false) }
    var showPrivacyPolicy by remember { mutableStateOf(false) }

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

    Box {
        Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("关于") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            Spacer(Modifier.height(32.dp))
            Text(
                text = "WriteDone",
                fontFamily = handwritingFont,
                fontSize = 32.sp,
                color = AppColors.text,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "每做完一件具体的事情，就提交一下吧。",
                fontFamily = handwritingFont,
                fontSize = 16.sp,
                color = AppColors.textMuted,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = "v1.0.0",
                color = AppColors.textMuted,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(32.dp))
            HorizontalDivider(color = AppColors.border)
            Spacer(Modifier.height(16.dp))
            Text(
                text = "灵感来自 git —— 在完全由自己掌控的时间里，把每天主动做的、有意义的事，像 git commit 一样记录下来。",
                color = AppColors.textMuted,
            )
            Spacer(Modifier.height(24.dp))

            Text(
                text = "📜 协议与政策",
                color = AppColors.textMuted,
                fontSize = 13.sp,
            )
            Spacer(Modifier.height(8.dp))

            DocLinkCard(
                title = "《粒时用户协议》",
                summary = "约定你与开发者之间就使用本应用的权利和义务。",
                onClick = { showUserAgreement = true },
            )
            Spacer(Modifier.height(8.dp))
            DocLinkCard(
                title = "《粒时隐私政策》",
                summary = "说明本应用如何处理你的信息。本应用不收集任何信息。",
                onClick = { showPrivacyPolicy = true },
            )

            Spacer(Modifier.height(24.dp))
            Text(
                text = "🔒 数据说明",
                color = AppColors.textMuted,
                fontSize = 13.sp,
            )
            Spacer(Modifier.height(8.dp))

            DataNoteCard()

            Spacer(Modifier.height(24.dp))
            Text(
                text = "© 2026 lxb. All rights reserved.",
                color = AppColors.textMuted,
                fontSize = 12.sp,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(16.dp))
        }
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

@Composable
private fun DocLinkCard(title: String, summary: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.card),
        border = BorderStroke(1.dp, AppColors.border),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    color = AppColors.text,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = summary,
                    fontSize = 12.sp,
                    color = AppColors.textMuted,
                )
            }
        }
    }
}

@Composable
private fun DataNoteCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.card),
        border = BorderStroke(1.dp, AppColors.border),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "你的数据只属于你",
                fontSize = 14.sp,
                color = AppColors.text,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "· 不联网 · 不收集任何个人信息\n" +
                        "· 未集成任何第三方 SDK\n" +
                        "· 所有提交记录只保存在本机\n" +
                        "· 卸载 App 数据即丢失, 请自行备份",
                fontSize = 12.sp,
                color = AppColors.textMuted,
            )
        }
    }
}
