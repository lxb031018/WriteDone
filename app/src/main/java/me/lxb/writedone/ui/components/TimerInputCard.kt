package me.lxb.writedone.ui.components

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import me.lxb.writedone.ui.theme.Dimens
import me.lxb.writedone.ui.screens.home.CompletedViewModel
import me.lxb.writedone.ui.screens.home.TimerViewModel
import java.util.Date

@Composable
fun TimerInputCard(
    timerViewModel: TimerViewModel,
    completedViewModel: CompletedViewModel,
    isLandscape: Boolean,
    breathingEnabled: Boolean,
    modifier: Modifier = Modifier,
) {
    val timerState by timerViewModel.state.collectAsState()
    val context = LocalContext.current

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { /* permission result handled implicitly — notification will work if granted */ }

    val onToggle: () -> Unit = {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            timerViewModel.toggleTimer()
        }
    }

    var inputText by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        inputText = completedViewModel.loadDraft()
    }

    LaunchedEffect(inputText) {
        completedViewModel.saveDraft(inputText)
    }

    // Collect stop events from ViewModel to save note with correct elapsed time
    LaunchedEffect(Unit) {
        timerViewModel.stopEvents.collect { (elapsed, startTimeMillis) ->
            val content = inputText.trim()
            if (content.isNotEmpty()) {
                completedViewModel.addNote(
                    content = content,
                    createdAt = Date(startTimeMillis),
                    durationSeconds = elapsed,
                )
                inputText = ""
            }
        }
    }

    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
        ) {
            if (timerState.breakButtonVisible) {
                TimerCompleteActions(
                    onBreak = { timerViewModel.takeBreak() },
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                TimerComponent(
                    state = timerState,
                    onToggle = onToggle,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }

        Spacer(Modifier.height(Dimens.gapLg))

        StickyNoteInput(
            value = inputText,
            onValueChange = { if (!isLandscape) inputText = it },
            createdAt = timerState.startTimeMillis?.let { Date(it) },
            durationSeconds = null,
            breathingEnabled = breathingEnabled,
            enabled = !isLandscape,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
