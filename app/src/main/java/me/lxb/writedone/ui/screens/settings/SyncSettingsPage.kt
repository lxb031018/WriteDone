package me.lxb.writedone.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.lxb.writedone.R
import me.lxb.writedone.data.sync.SyncManager

@Composable
fun SyncSettingsPage(
    syncManager: SyncManager,
    onBack: () -> Unit,
) {
    val state by syncManager.state.collectAsState()

    LaunchedEffect(Unit) {
        syncManager.refreshStatus()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        Text(
            text = "← ${stringResource(R.string.settings_sync)}",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.clickable(onClick = onBack),
        )

        Spacer(Modifier.height(16.dp))

        HostToggleCard(
            isHostEnabled = state.isHostEnabled,
            onCheckedChange = { syncManager.setHostEnabled(it) },
        )

        Spacer(Modifier.height(12.dp))

        StatusCard(state)

        if (!state.isHostEnabled) {
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = { syncManager.syncNow() },
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isSyncing,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                ),
            ) {
                Text(
                    text = if (state.isSyncing) stringResource(R.string.sync_in_progress)
                    else stringResource(R.string.sync_manual),
                    fontSize = 14.sp,
                )
            }
        }
    }
}

@Composable
private fun HostToggleCard(
    isHostEnabled: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onCheckedChange(!isHostEnabled) }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.sync_host_toggle),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.sync_host_description),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.width(12.dp))
            Switch(
                checked = isHostEnabled,
                onCheckedChange = onCheckedChange,
            )
        }
    }
}

@Composable
private fun StatusCard(state: me.lxb.writedone.data.sync.SyncState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.sync_status),
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
            )

            Spacer(Modifier.height(12.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                val isOnline = state.lastSyncResult.isNotEmpty()
                    && !state.lastSyncResult.startsWith("同步失败")
                    && state.lastSyncResult != "未连接到热点"
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                state.isHostRunning -> MaterialTheme.colorScheme.primary
                                isOnline -> MaterialTheme.colorScheme.secondary
                                else -> MaterialTheme.colorScheme.error
                            }
                        ),
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = when {
                        state.isHostRunning -> stringResource(R.string.sync_role_host)
                        state.isSyncing -> stringResource(R.string.sync_in_progress)
                        isOnline -> stringResource(R.string.sync_role_client)
                        else -> stringResource(R.string.sync_role_isolated)
                    },
                    fontSize = 14.sp,
                )
            }

            if (state.lastSyncResult.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = state.lastSyncResult,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
