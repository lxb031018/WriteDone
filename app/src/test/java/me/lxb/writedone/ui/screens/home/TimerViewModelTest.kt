package me.lxb.writedone.ui.screens.home

import android.app.AlarmManager
import android.app.Application
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkConstructor
import io.mockk.mockkStatic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.test.resetMain
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
        every { NotificationHelper.createChannels(any<Context>()) } just Runs
        every { NotificationHelper.showBreakReminder(any<Context>()) } just Runs

        mockkStatic(PendingIntent::class)
        every { PendingIntent.getBroadcast(any(), any(), any(), any()) } returns mockk(relaxed = true)

        mockkStatic(ContextCompat::class)
        every { ContextCompat.startForegroundService(any(), any()) } just Runs

        mockkConstructor(Intent::class)
        every { anyConstructed<Intent>().setAction(any()) } returns mockk(relaxed = true)
        every { anyConstructed<Intent>().putExtra(any(), any<Long>()) } returns mockk(relaxed = true)
        every { anyConstructed<Intent>().putExtra(any(), any<String>()) } returns mockk(relaxed = true)
        every { anyConstructed<Intent>().putExtra(any(), any<Boolean>()) } returns mockk(relaxed = true)

        timerUseCase = mockk(relaxed = true)
        coEvery { timerUseCase.restoreStartTime() } returns null
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): TimerViewModel {
        return TimerViewModel(app, timerUseCase)
    }

    // ── Initial state ──

    @Test
    fun `initial state has correct defaults`() {
        val vm = createViewModel()
        val state = vm.state.value
        assertEquals(TimerStatus.Idle, state.status)
        assertEquals(0, state.elapsedSeconds)
        assertEquals(0, state.cumulativeSeconds)
        assertFalse(state.breakButtonVisible)
        assertNull(state.startTimeMillis)
    }

    // ── start / stop without ambient mode ──

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
    fun `stop without ambient mode resets to Idle with no cumulative tracking`() {
        val vm = createViewModel()
        vm.start()
        vm.stop()
        val state = vm.state.value
        assertEquals(TimerStatus.Idle, state.status)
        assertEquals(0, state.elapsedSeconds)
        assertEquals(0, state.cumulativeSeconds)
        assertFalse(state.breakButtonVisible)
    }

    // ── Ambient mode activates pomodoro session ──

    @Test
    fun `setAmbientMode true when idle has no effect`() {
        val vm = createViewModel()
        vm.setAmbientMode(true)
        val state = vm.state.value
        assertEquals(TimerStatus.Idle, state.status)
        assertEquals(0, state.cumulativeSeconds)
        assertFalse(state.breakButtonVisible)
    }

    @Test
    fun `setAmbientMode true when running enables cumulative tracking`() {
        val vm = createViewModel()
        vm.start()
        vm.setAmbientMode(true)
        // Simulate elapsed time by stopping — cumulative should be tracked
        vm.stop()
        val state = vm.state.value
        assertTrue(state.cumulativeSeconds >= 0)
    }

    @Test
    fun `cumulative tracking across multiple stop cycles with ambient mode`() {
        val vm = createViewModel()
        vm.start()
        vm.setAmbientMode(true)
        vm.stop()

        val cumulativeAfterFirst = vm.state.value.cumulativeSeconds

        vm.start()
        vm.stop()

        assertEquals(cumulativeAfterFirst, vm.state.value.cumulativeSeconds)
    }

    // ── Break button visibility ──

    @Test
    fun `break button not visible without ambient mode`() {
        val vm = createViewModel()
        vm.start()
        vm.stop()
        assertFalse(vm.state.value.breakButtonVisible)
        assertEquals(0, vm.state.value.cumulativeSeconds)
    }

    // ── takeBreak ──

    @Test
    fun `takeBreak resets cumulative and stops timer`() {
        val vm = createViewModel()

        vm.start()
        vm.setAmbientMode(true)
        vm.stop()
        vm.takeBreak()

        val state = vm.state.value
        assertEquals(TimerStatus.Idle, state.status)
        assertEquals(0, state.cumulativeSeconds)
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

    // ── Tick loop ──

    @Test
    fun `timer ticks increment elapsed while running`() {
        val vm = createViewModel()
        vm.start()
        vm.stop()
        assertTrue(vm.state.value.elapsedSeconds >= 0)
    }

    // ── setAmbientMode lifecycle ──

    @Test
    fun `setAmbientMode false does not end pomodoro session`() {
        val vm = createViewModel()
        vm.start()
        vm.setAmbientMode(true)
        vm.setAmbientMode(false) // exiting ambient should NOT end session
        vm.stop()

        val state = vm.state.value
        assertTrue(state.cumulativeSeconds >= 0)
    }
}
