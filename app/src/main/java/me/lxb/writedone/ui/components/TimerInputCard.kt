package me.lxb.writedone.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.first
import me.lxb.writedone.data.repository.SettingsRepository
import me.lxb.writedone.ui.theme.Dimens
import me.lxb.writedone.viewmodel.CompletedViewModel
import me.lxb.writedone.viewmodel.TimerMode
import me.lxb.writedone.viewmodel.TimerStatus
import me.lxb.writedone.viewmodel.TimerViewModel
import java.util.Date

@Composable
fun TimerInputCard(
    timerViewModel: TimerViewModel,
    completedViewModel: CompletedViewModel,
    breathingEnabled: Boolean,
    modifier: Modifier = Modifier,
) {
    val timerState by timerViewModel.state.collectAsState()

    var inputText by remember { mutableStateOf("") }
    var createdAt by remember { mutableStateOf<Date?>(null) }
    var prevState by remember { mutableStateOf(timerState) }

    LaunchedEffect(Unit) {
        inputText = completedViewModel.draftRepo.load()
    }

    LaunchedEffect(inputText) {
        completedViewModel.draftRepo.save(inputText)
    }

    LaunchedEffect(timerState) {
        val prev = prevState
        prevState = timerState

        if (prev.status == TimerStatus.Running && timerState.status == TimerStatus.Idle) {
            val elapsed = prev.elapsedSeconds
            val content = inputText.trim()
            if (content.isNotEmpty()) {
                completedViewModel.addNote(
                    content = content,
                    createdAt = createdAt ?: Date(),
                    durationSeconds = elapsed,
                )
                inputText = ""
            }
            createdAt = null
        } else if (prev.status == TimerStatus.Idle && timerState.status == TimerStatus.Running) {
            createdAt = Date()
        }
    }

    // Async load of pomodoro mode — no main-thread block (was `runBlocking` before).
    val context = LocalContext.current.applicationContext
    val initialPage by produceState(initialValue = 0) {
        val repo = SettingsRepository(context)
        value = if (repo.timerModePomodoro.first()) 1 else 0
    }
    val pagerState = rememberPagerState(
        initialPage = initialPage,
        pageCount = { 2 },
    )

    LaunchedEffect(pagerState.currentPage) {
        val mode = if (pagerState.currentPage == 1) TimerMode.Pomodoro else TimerMode.Normal
        timerViewModel.syncMode(mode)
    }

    Column(modifier = modifier) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            userScrollEnabled = true,
        ) { page ->
            val mode = if (page == 1) TimerMode.Pomodoro else TimerMode.Normal
            TimerComponent(
                state = timerState,
                mode = mode,
                onToggle = { timerViewModel.toggleTimer() },
                modifier = Modifier.fillMaxSize(),
            )
        }

        Spacer(Modifier.height(Dimens.gapLg))

        StickyNoteInput(
            value = inputText,
            onValueChange = { inputText = it },
            createdAt = createdAt,
            durationSeconds = null,
            breathingEnabled = breathingEnabled,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
