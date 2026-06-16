package me.lxb.writedone

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import me.lxb.writedone.ambient.AmbientController
import me.lxb.writedone.data.repository.SettingsRepository
import me.lxb.writedone.ui.screens.calendar.CalendarPage
import me.lxb.writedone.ui.screens.legal.AgreementDialog
import me.lxb.writedone.ui.screens.legal.PrivacyPolicyPage
import me.lxb.writedone.ui.screens.legal.UserAgreementPage
import me.lxb.writedone.ui.screens.home.HomeScreen
import me.lxb.writedone.ui.screens.settings.AboutPage
import me.lxb.writedone.ui.theme.AppColors
import me.lxb.writedone.ui.theme.LocalAmbientProgress
import me.lxb.writedone.ui.theme.WriteDoneTheme
import me.lxb.writedone.viewmodel.CompletedViewModel
import me.lxb.writedone.viewmodel.SettingsViewModel
import me.lxb.writedone.viewmodel.TimerViewModel
import java.util.Date

class MainActivity : ComponentActivity() {
    private lateinit var timerViewModel: TimerViewModel
    private lateinit var completedViewModel: CompletedViewModel
    private lateinit var settingsViewModel: SettingsViewModel
    private lateinit var settingsRepo: SettingsRepository
    private lateinit var ambientController: AmbientController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        timerViewModel = ViewModelProvider(this)[TimerViewModel::class.java]
        completedViewModel = ViewModelProvider(this)[CompletedViewModel::class.java]
        settingsViewModel = ViewModelProvider(this)[SettingsViewModel::class.java]
        settingsRepo = SettingsRepository(applicationContext)
        ambientController = AmbientController()

        setContent {
            val ambientProgress = androidx.compose.runtime.remember {
                mutableFloatStateOf(0f)
            }.floatValue
            // We can't read LocalAmbientProgress inside WriteDoneTheme (it provides it),
            // so we read from a side-state holder. Keep ambientController as the
            // single source of truth — HomeScreen drives the actual animation
            // and sets LocalAmbientProgress for children.
            androidx.compose.runtime.SideEffect {
                // Placeholder for future: could read ambientController.state.value
                // directly if we need theme crossfade outside HomeScreen.
            }
            WriteDoneTheme(ambientProgress = ambientProgress) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = AppColors.bg,
                ) {
                    WriteDoneApp(
                        timerViewModel = timerViewModel,
                        completedViewModel = completedViewModel,
                        settingsViewModel = settingsViewModel,
                        settingsRepo = settingsRepo,
                        ambientController = ambientController,
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        ambientController.dispose()
        super.onDestroy()
    }
}

enum class Screen { Home, Calendar, About, UserAgreement, PrivacyPolicy }

@Composable
private fun WriteDoneApp(
    timerViewModel: TimerViewModel,
    completedViewModel: CompletedViewModel,
    settingsViewModel: SettingsViewModel,
    settingsRepo: SettingsRepository,
    ambientController: AmbientController,
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
            )
        }
        Screen.Calendar -> {
            CalendarPage(
                selectedDate = calendarDate,
                notes = completedState.notes,
                onDateSelected = { date ->
                    calendarDate = date
                    completedViewModel.selectDate(date)
                },
            )
        }
        Screen.About -> {
            AboutPage(
                onBack = { currentScreen = Screen.Home },
            )
        }
        Screen.UserAgreement -> {
            UserAgreementPage(onBack = {
                currentScreen = Screen.Home
            })
        }
        Screen.PrivacyPolicy -> {
            PrivacyPolicyPage(onBack = {
                currentScreen = Screen.Home
            })
        }
    }
}
