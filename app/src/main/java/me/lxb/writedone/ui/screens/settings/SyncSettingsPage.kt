package me.lxb.writedone.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import me.lxb.writedone.data.sync.HotspotManager
import me.lxb.writedone.data.sync.Role
import me.lxb.writedone.data.sync.SyncManager

@Composable
fun SyncSettingsPage(
    syncManager: SyncManager,
    hotspotManager: HotspotManager,
    onBack: () -> Unit,
) {
    val syncState by syncManager.state.collectAsState()
    val hotspotState by hotspotManager.state.collectAsState()

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

        StatusCard(syncState, hotspotState)

        Spacer(Modifier.height(12.dp))

        InstructionCard()

        Spacer(Modifier.height(12.dp))

        Button(
            onClick = { syncManager.syncNow() },
            modifier = Modifier.fillMaxWidth(),
            enabled = !syncState.isSyncing,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
            ),
        ) {
            Text(
                text = when {
                    syncState.isSyncing -> stringResource(R.string.sync_in_progress)
                    syncState.role == Role.HOST -> "等待对方连接..."
                    else -> stringResource(R.string.sync_manual)
                },
                fontSize = 14.sp,
            )
        }

        if (syncState.lastSyncResult.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            Text(
                syncState.lastSyncResult,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun StatusCard(
    syncState: me.lxb.writedone.data.sync.SyncState,
    hotspotState: me.lxb.writedone.data.sync.HotspotState,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "同步状态",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
            )

            Spacer(Modifier.height(12.dp))

            StatusRow(
                label = "角色",
                value = when (hotspotState.role) {
                    Role.HOST -> "热点主机 (服务器)"
                    Role.CLIENT -> "连接方"
                    Role.UNKNOWN -> "未检测"
                },
                isOnline = hotspotState.role != Role.UNKNOWN,
            )

            Spacer(Modifier.height(8.dp))

            if (hotspotState.gatewayAddress != null) {
                StatusRow(
                    label = "网关",
                    value = hotspotState.gatewayAddress.hostAddress ?: "",
                    isOnline = true,
                )
                Spacer(Modifier.height(8.dp))
            }

            if (hotspotState.localHotspotIp != null) {
                StatusRow(
                    label = "本机 IP",
                    value = hotspotState.localHotspotIp.hostAddress ?: "",
                    isOnline = true,
                )
                Spacer(Modifier.height(8.dp))
            }

            if (hotspotState.lastError.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    hotspotState.lastError,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.error,
                )
            }

            if (syncState.lastSyncResult.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    syncState.lastSyncResult,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun InstructionCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("使用说明", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            Text(
                "1. 确保两部设备已通过热点连接\n" +
                        "2. 在主机上点击「同步」按钮启动服务器\n" +
                        "3. 在连接方上点击「同步」按钮自动同步",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun StatusRow(label: String, value: String, isOnline: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(
                    if (isOnline) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.error
                ),
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = "$label: $value",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
