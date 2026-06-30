package me.lxb.writedone.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import me.lxb.writedone.domain.usecase.SettingsUseCase
import me.lxb.writedone.ui.theme.ThemeMode
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsUseCase: SettingsUseCase,
) : ViewModel() {

    val breathingLampEnabled: StateFlow<Boolean> = settingsUseCase.breathingLampEnabled
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    fun setBreathingLampEnabled(enabled: Boolean) {
        viewModelScope.launch { settingsUseCase.setBreathingLampEnabled(enabled) }
    }

    val autoDimBrightness: StateFlow<Boolean> = settingsUseCase.autoDimBrightness
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    fun setAutoDimBrightness(enabled: Boolean) {
        viewModelScope.launch { settingsUseCase.setAutoDimBrightness(enabled) }
    }

    val themeMode: StateFlow<ThemeMode?> = settingsUseCase.themeMode
        .map { name -> try { ThemeMode.valueOf(name) } catch (_: Exception) { ThemeMode.Light } }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { settingsUseCase.setThemeMode(mode.name) }
    }

    val autoStartTimerOnLandscapeEnabled: StateFlow<Boolean> = settingsUseCase.autoStartTimerOnLandscapeEnabled
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    fun setAutoStartTimerOnLandscapeEnabled(enabled: Boolean) {
        viewModelScope.launch { settingsUseCase.setAutoStartTimerOnLandscapeEnabled(enabled) }
    }

    val autoStartTimerOnFlatEnabled: StateFlow<Boolean> = settingsUseCase.autoStartTimerOnFlatEnabled
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    fun setAutoStartTimerOnFlatEnabled(enabled: Boolean) {
        viewModelScope.launch { settingsUseCase.setAutoStartTimerOnFlatEnabled(enabled) }
    }
}
