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

    val autoDimBrightness: StateFlow<Boolean> = settingsUseCase.autoDimBrightness
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    fun setAutoDimBrightness(enabled: Boolean) {
        viewModelScope.launch { settingsUseCase.setAutoDimBrightness(enabled) }
    }

    val themeMode: StateFlow<ThemeMode> = settingsUseCase.themeMode
        .map { name -> try { ThemeMode.valueOf(name) } catch (_: Exception) { ThemeMode.System } }
        .stateIn(viewModelScope, SharingStarted.Eagerly, ThemeMode.System)

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { settingsUseCase.setThemeMode(mode.name) }
    }
}
