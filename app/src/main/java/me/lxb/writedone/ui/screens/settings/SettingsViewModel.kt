package me.lxb.writedone.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import me.lxb.writedone.domain.usecase.SettingsUseCase
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsUseCase: SettingsUseCase,
) : ViewModel() {

    val autoDimBrightness: StateFlow<Boolean> = settingsUseCase.autoDimBrightness
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    fun setAutoDimBrightness(enabled: Boolean) {
        viewModelScope.launch { settingsUseCase.setAutoDimBrightness(enabled) }
    }
}
