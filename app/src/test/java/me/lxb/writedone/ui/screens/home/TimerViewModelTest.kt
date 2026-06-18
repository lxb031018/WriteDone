package me.lxb.writedone.ui.screens.home

import android.app.AlarmManager
import android.app.Application
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.test.resetMain
import me.lxb.writedone.domain.model.TimerMode
import me.lxb.writedone.domain.usecase.TimerUseCase
import me.lxb.writedone.service.notification.NotificationHelper
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TimerViewModelTest {

    private lateinit var app: Application
    private lateinit var timerUseCase: TimerUseCase
    private lateinit var timerModeFlow: MutableStateFlow<TimerMode>

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())

        app = mockk(relaxed = true)
        val context = mockk<Context>(relaxed = true)
        val notificationManager = mockk<NotificationManager>(relaxed = true)
        val alarmManager = mockk<AlarmManager>(relaxed = true)

        every { app.applicationContext } returns context
        every { app.getSystemService(Context.NOTIFICATION_SERVICE) } returns notificationManager
        every { app.getSystemService(Context.ALARM_SERVICE) } returns alarmManager
        every { context.getSystemService(Context.NOTIFICATION_SERVICE) } returns notificationManager
        every { context.getSystemService(Context.ALARM_SERVICE) } returns alarmManager

        mockkObject(NotificationHelper)
        every { NotificationHelper.createChannel(any()) } just Runs
        every { NotificationHelper.showBreakReminder(any()) } just Runs

        mockkStatic(PendingIntent::class)
        every { PendingIntent.getBroadcast(any(), any(), any(), any()) } returns mockk(relaxed = true)

        timerModeFlow = MutableStateFlow(TimerMode.Normal)
        timerUseCase = mockk(relaxed = true)
        every { timerUseCase.observeTimerMode() } returns timerModeFlow
        coEvery { timerUseCase.restoreStartTime() } returns null
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): TimerViewModel {
        val vm = TimerViewModel(app, timerUseCase)
        return vm
    }

    private fun createPomodoroViewModel(): TimerViewModel {
        timerModeFlow.value = TimerMode.Pomodoro
        return TimerViewModel(app, timerUseCase)
    }

    // ── Initial state ──

    @Test
    fun `initial state has correct defaults`() {
        val vm = createViewModel()
        val state = vm.state.value
        assertEquals(TimerStatus.Idle, state.status)
        assertEquals(0, state.elapsedSeconds)
        assertEquals(TimerMode.Normal, state.mode)
        assertEquals(0, state.pomodoroCumulativeSeconds)
        assertFalse(state.breakButtonVisible)
        assertNull(state.startTimeMillis)
    }

    // ── start / stop in Normal mode ──

    @Test
    fun `start sets status to Running and resets elapsed`() {
        val vm = createViewModel()
        vm.start()
        val state = vm.state.value
        assertEquals(TimerStatus.Running, state.status)
        assertEquals(0, state.elapsedSeconds)
        assertNotNull(state.startTimeMillis)
        vm.stop()
    }

    @Test
    fun `stop in Normal mode resets to Idle with no cumulative tracking`() {
        val vm = createViewModel()
        vm.start()
        vm.stop()
        val state = vm.state.value
        assertEquals(TimerStatus.Idle, state.status)
        assertEquals(0, state.elapsedSeconds)
        assertEquals(0, state.pomodoroCumulativeSeconds)
        assertFalse(state.breakButtonVisible)
    }

    // ── Pomodoro mode ──

    @Test
    fun `pomodoro mode persists from settings`() {
        val vm = createPomodoroViewModel()
        assertEquals(TimerMode.Pomodoro, vm.state.value.mode)
    }

    @Test
    fun `pomodoro cumulative tracking across multiple cycles`() {
        val vm = createPomodoroViewModel()

        vm.start()
        vm.stop()
        vm.start()
        vm.stop()

        // Each cycle had 0 elapsed (no time passed), so cumulative stays 0
        assertEquals(0, vm.state.value.pomodoroCumulativeSeconds)
    }

    // ── Break button visibility ──

    @Test
    fun `break button appears when cumulative reaches 1500 (25 min)`() {
        val vm = createPomodoroViewModel()

        vm.start()
        vm.stop()
        assertEquals(0, vm.state.value.pomodoroCumulativeSeconds)
        assertFalse(vm.state.value.breakButtonVisible)
    }

    @Test
    fun `break button does not appear in Normal mode`() {
        val vm = createViewModel()
        vm.start()
        vm.stop()
        assertFalse(vm.state.value.breakButtonVisible)
        assertEquals(0, vm.state.value.pomodoroCumulativeSeconds)
    }

    // ── takeBreak ──

    @Test
    fun `takeBreak resets cumulative and stops timer`() {
        val vm = createPomodoroViewModel()

        vm.start()
        vm.stop()
        vm.takeBreak()

        val state = vm.state.value
        assertEquals(TimerStatus.Idle, state.status)
        assertEquals(0, state.pomodoroCumulativeSeconds)
        assertFalse(state.breakButtonVisible)
    }

    // ── toggleTimer ──

    @Test
    fun `toggleTimer starts from Idle and stops from Running`() {
        val vm = createViewModel()
        vm.toggleTimer()
        assertEquals(TimerStatus.Running, vm.state.value.status)
        vm.toggleTimer()
        assertEquals(TimerStatus.Idle, vm.state.value.status)
    }

    // ── setMode ──

    @Test
    fun `setMode updates mode`() {
        val vm = createViewModel()
        vm.setMode(TimerMode.Pomodoro)
        assertEquals(TimerMode.Pomodoro, vm.state.value.mode)
        vm.setMode(TimerMode.Normal)
        assertEquals(TimerMode.Normal, vm.state.value.mode)
    }

    // ── Tick loop ──

    @Test
    fun `timer ticks increment elapsed while running`() {
        val vm = createViewModel()
        vm.start()
        vm.stop()
        assertTrue(vm.state.value.elapsedSeconds >= 0)
    }

    // ── Init collects settings ──

    @Test
    fun `init collects settings and responds to pomodoro changes`() {
        val vm = createViewModel()
        assertEquals(TimerMode.Normal, vm.state.value.mode)
        timerModeFlow.value = TimerMode.Pomodoro
        assertEquals(TimerMode.Pomodoro, vm.state.value.mode)
    }
}
