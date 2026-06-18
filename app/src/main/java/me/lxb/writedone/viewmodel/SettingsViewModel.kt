package me.lxb.writedone.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import me.lxb.writedone.data.repository.SettingsRepository
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepo: SettingsRepository,
) : ViewModel() {

    val autoDimBrightness: StateFlow<Boolean> = settingsRepo.autoDimBrightness
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    fun setAutoDimBrightness(enabled: Boolean) {
        viewModelScope.launch { settingsRepo.setAutoDimBrightness(enabled) }
    }
}
