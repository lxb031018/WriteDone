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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import me.lxb.writedone.R
import me.lxb.writedone.data.sync.PairingRepository
import me.lxb.writedone.data.sync.SyncManager

@Composable
fun SyncSettingsPage(
    syncManager: SyncManager,
    pairingRepo: PairingRepository,
    onBack: () -> Unit,
) {
    val state by syncManager.state.collectAsState()
    val pairedDeviceNames by pairingRepo.pairedDeviceNamesFlow().collectAsState(
        initial = emptyMap()
    )
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
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

            HostModeCard(
                isHostRunning = state.isHostRunning,
                onToggle = {
                    if (state.isHostRunning) syncManager.stopHost()
                    else syncManager.startHost()
                },
            )

            Spacer(Modifier.height(16.dp))

            if (!state.isHostRunning) {
                ClientSyncCard(
                    isSyncing = state.isSyncing,
                    lastSyncResult = state.lastSyncResult,
                    onSync = {
                        scope.launch {
                            val result = syncManager.triggerSync()
                            snackbarHostState.showSnackbar(result)
                        }
                    },
                )

                Spacer(Modifier.height(16.dp))
            }

            PairedDevicesCard(
                pairedDeviceNames = pairedDeviceNames,
            )
        }
    }
}

@Composable
private fun HostModeCard(
    isHostRunning: Boolean,
    onToggle: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.sync_host_mode),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = stringResource(R.string.sync_host_mode_desc),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Switch(
                    checked = isHostRunning,
                    onCheckedChange = { onToggle() },
                )
            }

            Spacer(Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(
                            if (isHostRunning) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.error
                        ),
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = if (isHostRunning) stringResource(R.string.sync_host_started)
                    else stringResource(R.string.sync_host_stopped),
                    fontSize = 12.sp,
                    color = if (isHostRunning) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun ClientSyncCard(
    isSyncing: Boolean,
    lastSyncResult: String,
    onSync: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.settings_sync_title),
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
            )

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = onSync,
                enabled = !isSyncing,
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (isSyncing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                    Spacer(Modifier.width(8.dp))
                }
                Text(
                    if (isSyncing) stringResource(R.string.sync_in_progress)
                    else stringResource(R.string.sync_now)
                )
            }

            if (lastSyncResult.isNotEmpty()) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = lastSyncResult,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun PairedDevicesCard(
    pairedDeviceNames: Map<String, String>,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.sync_paired_devices),
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.height(8.dp))

            if (pairedDeviceNames.isEmpty()) {
                Text(
                    text = "暂无已配对的设备",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                pairedDeviceNames.forEach { (_, name) ->
                    Text(
                        text = "· $name",
                        fontSize = 14.sp,
                        modifier = Modifier.padding(vertical = 2.dp),
                    )
                }
            }
        }
    }
}
