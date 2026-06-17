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
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
    isLandscape: Boolean,
    breathingEnabled: Boolean,
    modifier: Modifier = Modifier,
) {
    val timerState by timerViewModel.state.collectAsState()

    var inputText by remember { mutableStateOf("") }
    var createdAt by remember { mutableStateOf<Date?>(null) }
    var prevState by remember { mutableStateOf(timerState) }

    val pagerState = rememberPagerState(
        initialPage = 10000 + if (timerViewModel.state.value.mode == TimerMode.Pomodoro) 1 else 0,
        pageCount = { Int.MAX_VALUE },
    )
    var previousPage by remember { mutableStateOf(pagerState.currentPage) }

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

    // 用户滑动翻页时同步 pager → ViewModel
    LaunchedEffect(pagerState.currentPage) {
        if (pagerState.currentPage != previousPage) {
            val mode = if (pagerState.currentPage % 2 == 0) TimerMode.Normal else TimerMode.Pomodoro
            if (mode != timerViewModel.state.value.mode) {
                timerViewModel.syncMode(mode)
            }
        }
        previousPage = pagerState.currentPage
    }

    LaunchedEffect(timerViewModel.state.value.mode) {
        val targetMod = if (timerViewModel.state.value.mode == TimerMode.Pomodoro) 1 else 0
        if (pagerState.currentPage % 2 != targetMod) {
            pagerState.scrollToPage(pagerState.currentPage + (targetMod - pagerState.currentPage % 2))
        }
    }

    Column(modifier = modifier) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            userScrollEnabled = !isLandscape,
        ) { page ->
            if (timerState.mode == TimerMode.Pomodoro && timerState.breakButtonVisible) {
                TimerCompleteActions(
                    onBreak = { timerViewModel.takeBreak() },
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                TimerComponent(
                    state = timerState,
                    mode = timerViewModel.state.value.mode,
                    onToggle = { timerViewModel.toggleTimer() },
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }

        Spacer(Modifier.height(Dimens.gapLg))

        StickyNoteInput(
            value = inputText,
            onValueChange = { if (!isLandscape) inputText = it },
            createdAt = createdAt,
            durationSeconds = null,
            breathingEnabled = breathingEnabled,
            enabled = !isLandscape,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
