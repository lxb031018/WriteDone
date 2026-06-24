package me.lxb.writedone.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import me.lxb.writedone.data.sync.HotspotManager
import me.lxb.writedone.data.sync.Role
import me.lxb.writedone.data.sync.SyncManager
import javax.inject.Inject

data class SyncUiState(
    val isSyncing: Boolean = false,
    val lastSyncResult: String = "",
    val roleDescription: String = "未检测",
    val isOnline: Boolean = false,
    val gatewayIp: String = "",
    val localIp: String = "",
    val lastError: String = "",
    val buttonText: String = "同步",
    val buttonEnabled: Boolean = true,
)

@HiltViewModel
class SyncViewModel @Inject constructor(
    private val syncManager: SyncManager,
    private val hotspotManager: HotspotManager,
) : ViewModel() {

    val uiState: StateFlow<SyncUiState> = combine(
        syncManager.state,
        hotspotManager.state,
    ) { s, h ->
        SyncUiState(
            isSyncing = s.isSyncing,
            lastSyncResult = s.lastSyncResult,
            roleDescription = when (h.role) {
                Role.HOST -> "热点主机 (服务器)"
                Role.CLIENT -> "连接方"
                Role.UNKNOWN -> "未检测"
            },
            isOnline = h.role != Role.UNKNOWN,
            gatewayIp = h.gatewayAddress?.hostAddress ?: "",
            localIp = h.localHotspotIp?.hostAddress ?: "",
            lastError = h.lastError,
            buttonText = when {
                s.isSyncing -> "同步中..."
                s.role == Role.HOST -> "等待对方连接..."
                else -> "同步"
            },
            buttonEnabled = !s.isSyncing,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SyncUiState())

    fun syncNow() {
        viewModelScope.launch {
            syncManager.syncNow()
        }
    }
}
