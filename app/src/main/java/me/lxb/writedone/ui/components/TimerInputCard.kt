package me.lxb.writedone.ui.components

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import me.lxb.writedone.ui.theme.Dimens
import me.lxb.writedone.ui.screens.home.CompletedViewModel
import me.lxb.writedone.domain.model.TimerMode
import me.lxb.writedone.ui.screens.home.TimerStatus
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
        if (timerState.mode == TimerMode.Pomodoro &&
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            timerViewModel.toggleTimer()
        }
    }

    var inputText by remember { mutableStateOf("") }

    val pagerState = rememberPagerState(
        initialPage = 10000 + if (timerState.mode == TimerMode.Pomodoro) 1 else 0,
        pageCount = { Int.MAX_VALUE },
    )
    var previousPage by remember { mutableStateOf(pagerState.currentPage) }

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

    // 用户滑动翻页时同步 pager → ViewModel
    LaunchedEffect(pagerState.currentPage) {
        if (pagerState.currentPage != previousPage) {
            val mode = if (pagerState.currentPage % 2 == 0) TimerMode.Normal else TimerMode.Pomodoro
            if (mode != timerState.mode) {
                timerViewModel.setMode(mode)
            }
        }
        previousPage = pagerState.currentPage
    }

    LaunchedEffect(timerState.mode) {
        val targetMod = if (timerState.mode == TimerMode.Pomodoro) 1 else 0
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
            userScrollEnabled = timerState.status == TimerStatus.Idle,
        ) { page ->
            if (timerState.mode == TimerMode.Pomodoro && timerState.breakButtonVisible) {
                TimerCompleteActions(
                    onBreak = { timerViewModel.takeBreak() },
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                TimerComponent(
                    state = timerState,
                    mode = timerState.mode,
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
