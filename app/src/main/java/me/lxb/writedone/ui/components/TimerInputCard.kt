package me.lxb.writedone.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
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

    var inputText by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        inputText = completedViewModel.loadDraft()
    }

    LaunchedEffect(inputText) {
        completedViewModel.saveDraft(inputText)
    }

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

    StickyNoteInput(
        value = inputText,
        onValueChange = { if (!isLandscape) inputText = it },
        createdAt = timerState.startTimeMillis?.let { Date(it) },
        durationSeconds = null,
        breathingEnabled = breathingEnabled,
        enabled = !isLandscape,
        modifier = modifier.fillMaxWidth(),
    )
}
