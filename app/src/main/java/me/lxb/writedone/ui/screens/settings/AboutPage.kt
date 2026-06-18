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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.activity.compose.BackHandler
import androidx.compose.ui.res.stringResource
import me.lxb.writedone.R
import me.lxb.writedone.ui.screens.legal.PrivacyPolicyPage
import me.lxb.writedone.ui.theme.ZcoolKuaiLeFont as handwritingFont
import me.lxb.writedone.ui.screens.legal.UserAgreementPage
import me.lxb.writedone.ui.theme.AppColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutPage(onBack: () -> Unit) {
    val colorScheme = MaterialTheme.colorScheme
    var showUserAgreement by remember { mutableStateOf(false) }
    var showPrivacyPolicy by remember { mutableStateOf(false) }

    Box {
        Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.about_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.nav_back))
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
                color = colorScheme.onSurface,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.about_subtitle),
                fontFamily = handwritingFont,
                fontSize = 16.sp,
                color = colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = "v1.0.0",
                color = colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(32.dp))
            HorizontalDivider(color = colorScheme.outline)
            Spacer(Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.about_inspiration),
                color = colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(24.dp))

            Text(
                text = stringResource(R.string.about_legal_header),
                color = colorScheme.onSurfaceVariant,
                fontSize = 13.sp,
            )
            Spacer(Modifier.height(8.dp))

            DocLinkCard(
                title = stringResource(R.string.about_user_agreement_title),
                summary = stringResource(R.string.about_user_agreement_summary),
                onClick = { showUserAgreement = true },
            )
            Spacer(Modifier.height(8.dp))
            DocLinkCard(
                title = stringResource(R.string.about_privacy_policy_title),
                summary = stringResource(R.string.about_privacy_policy_summary),
                onClick = { showPrivacyPolicy = true },
            )

            Spacer(Modifier.height(24.dp))
            Text(
                text = stringResource(R.string.about_data_header),
                color = colorScheme.onSurfaceVariant,
                fontSize = 13.sp,
            )
            Spacer(Modifier.height(8.dp))

            DataNoteCard()

            Spacer(Modifier.height(24.dp))
            Text(
                text = "© 2026 lxb. All rights reserved.",
                color = colorScheme.onSurfaceVariant,
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
    val colorScheme = MaterialTheme.colorScheme
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
        border = BorderStroke(1.dp, colorScheme.outline),
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
                    color = colorScheme.onSurface,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = summary,
                    fontSize = 12.sp,
                    color = colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun DataNoteCard() {
    val colorScheme = MaterialTheme.colorScheme
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
        border = BorderStroke(1.dp, colorScheme.outline),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = stringResource(R.string.about_data_only_you),
                fontSize = 14.sp,
                color = colorScheme.onSurface,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = stringResource(R.string.about_data_details),
                fontSize = 12.sp,
                color = colorScheme.onSurfaceVariant,
            )
        }
    }
}
