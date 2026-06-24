package me.lxb.writedone

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.lxb.writedone.data.sync.SyncManager
import me.lxb.writedone.service.ambient.AmbientController
import me.lxb.writedone.domain.repository.NoteRepository
import me.lxb.writedone.domain.usecase.SettingsUseCase
import me.lxb.writedone.ui.screens.calendar.CalendarPage
import me.lxb.writedone.ui.screens.legal.AgreementDialog
import me.lxb.writedone.ui.screens.legal.PrivacyPolicyPage
import me.lxb.writedone.ui.screens.legal.UserAgreementPage
import me.lxb.writedone.ui.screens.home.HomeScreen
import me.lxb.writedone.ui.screens.settings.AboutPage
import me.lxb.writedone.ui.screens.settings.SyncSettingsPage
import me.lxb.writedone.ui.screens.settings.SyncViewModel
import me.lxb.writedone.ui.theme.ThemeMode
import me.lxb.writedone.ui.theme.WriteDoneTheme
import me.lxb.writedone.ui.screens.home.CompletedViewModel
import me.lxb.writedone.ui.screens.home.TimerViewModel
import me.lxb.writedone.ui.screens.settings.SettingsViewModel
import java.util.Date
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val timerViewModel: TimerViewModel by viewModels()
    private val completedViewModel: CompletedViewModel by viewModels()
    private val settingsViewModel: SettingsViewModel by viewModels()
    private val syncViewModel: SyncViewModel by viewModels()

    @Inject lateinit var settingsUseCase: SettingsUseCase
    @Inject lateinit var noteRepo: NoteRepository
    @Inject lateinit var syncManager: SyncManager

    private val ambientController = AmbientController()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        lifecycle.addObserver(LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    timerViewModel.onResume()
                    syncManager.onResume()
                }
                Lifecycle.Event.ON_PAUSE -> {
                    timerViewModel.onPause()
                }
                else -> {}
            }
        })

        setContent {
            var ambientProgress by remember { mutableFloatStateOf(0f) }
            val themeMode by settingsViewModel.themeMode.collectAsStateWithLifecycle()
            val darkTheme = when (themeMode) {
                ThemeMode.System -> isSystemInDarkTheme()
                ThemeMode.Light -> false
                ThemeMode.Dark -> true
            }
            WriteDoneTheme(darkTheme = darkTheme, ambientProgress = ambientProgress) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    WriteDoneApp(
                        timerViewModel = timerViewModel,
                        completedViewModel = completedViewModel,
                        settingsViewModel = settingsViewModel,
                        settingsRepo = settingsUseCase,
                        noteRepo = noteRepo,
                        ambientController = ambientController,
                        syncManager = syncManager,
                        syncViewModel = syncViewModel,
                        ambientProgress = ambientProgress,
                        onAmbientProgressChange = { ambientProgress = it },
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        ambientController.dispose()
        syncManager.destroy()
        super.onDestroy()
    }
}

enum class Screen { Home, Calendar, About, UserAgreement, PrivacyPolicy, Sync }

@Composable
private fun WriteDoneApp(
    timerViewModel: TimerViewModel,
    completedViewModel: CompletedViewModel,
    settingsViewModel: SettingsViewModel,
    settingsRepo: SettingsUseCase,
    noteRepo: NoteRepository,
    ambientController: AmbientController,
    syncManager: SyncManager,
    syncViewModel: SyncViewModel,
    ambientProgress: Float,
    onAmbientProgressChange: (Float) -> Unit,
) {
    var currentScreen by remember { mutableStateOf(Screen.Home) }
    var showAgreement by remember { mutableStateOf(false) }
    var agreementChecked by remember { mutableStateOf(false) }
    var pendingAgreement by remember { mutableStateOf(false) }
    var calendarDate by remember { mutableStateOf(Date()) }
    val completedState = completedViewModel.state.collectAsStateWithLifecycle().value
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        if (!settingsRepo.isAgreementAccepted()) {
            showAgreement = true
            pendingAgreement = true
        }
        agreementChecked = true
    }

    LaunchedEffect(currentScreen) {
        if (currentScreen == Screen.Home && pendingAgreement) {
            showAgreement = true
        }
    }

    if (!agreementChecked) return

    // Auto-sync 3 seconds after app opens
    LaunchedEffect(agreementChecked) {
        delay(3000)
        if (!pendingAgreement) {
            syncManager.syncNow()
        }
    }

    if (showAgreement) {
        AgreementDialog(
            onAgree = {
                coroutineScope.launch {
                    settingsRepo.setAgreementAccepted(true)
                }
                showAgreement = false
                pendingAgreement = false
            },
            onDisagree = { (context as? Activity)?.finish() },
            onShowUserAgreement = {
                showAgreement = false
                currentScreen = Screen.UserAgreement
            },
            onShowPrivacyPolicy = {
                showAgreement = false
                currentScreen = Screen.PrivacyPolicy
            },
        )
    }

    when (currentScreen) {
        Screen.Home -> {
            HomeScreen(
                timerViewModel = timerViewModel,
                completedViewModel = completedViewModel,
                settingsViewModel = settingsViewModel,
                ambientController = ambientController,
                noteRepo = noteRepo,
                ambientProgress = ambientProgress,
                onAmbientProgressChange = onAmbientProgressChange,
                onSyncSettings = { currentScreen = Screen.Sync },
                syncManager = syncManager,
            )
        }
        Screen.Calendar -> {
            BackHandler { currentScreen = Screen.Home }
            CalendarPage(
                selectedDate = calendarDate,
                notes = completedState.notes,
                noteRepo = noteRepo,
                onDateSelected = { date ->
                    calendarDate = date
                    completedViewModel.selectDate(date)
                },
            )
        }
        Screen.About -> {
            BackHandler { currentScreen = Screen.Home }
            AboutPage(
                onBack = { currentScreen = Screen.Home },
            )
        }
        Screen.UserAgreement -> {
            BackHandler { currentScreen = Screen.Home }
            UserAgreementPage(onBack = {
                currentScreen = Screen.Home
            })
        }
        Screen.PrivacyPolicy -> {
            BackHandler { currentScreen = Screen.Home }
            PrivacyPolicyPage(onBack = {
                currentScreen = Screen.Home
            })
        }
        Screen.Sync -> {
            BackHandler { currentScreen = Screen.Home }
            SyncSettingsPage(
                syncViewModel = syncViewModel,
                onBack = { currentScreen = Screen.Home },
            )
        }
    }
}
