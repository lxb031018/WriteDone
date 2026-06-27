package me.lxb.writedone.service.ambient

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class AmbientControllerTest {

    private lateinit var mockContext: Context

    @Before
    fun setUp() {
        val mockSensorManager = mockk<SensorManager> {
            every { getDefaultSensor(Sensor.TYPE_ACCELEROMETER) } returns null
        }
        mockContext = mockk {
            every { getSystemService(Context.SENSOR_SERVICE) } returns mockSensorManager
        }
    }

    @Test
    fun `initial state is Normal with breathing disabled`() = runTest {
        val controller = AmbientController(mockContext, this)
        assertEquals(AmbientStatus.Normal, controller.state.value.status)
        assertEquals(false, controller.state.value.breathingEnabled)
    }

    @Test
    fun `enter does not immediately set Active`() = runTest {
        val controller = AmbientController(mockContext, this)
        controller.enter()
        advanceUntilIdle()
        // No real sensor → isReady stays false → state stays Normal
        assertEquals(AmbientStatus.Normal, controller.state.value.status)
    }

    @Test
    fun `enter does not enable breathing without sensor`() = runTest {
        val controller = AmbientController(mockContext, this)
        controller.enter()
        advanceUntilIdle()
        assertEquals(false, controller.state.value.breathingEnabled)
    }

    @Test
    fun `exit resets to Normal`() = runTest {
        val controller = AmbientController(mockContext, this)
        controller.enter()
        advanceUntilIdle()
        controller.exit()
        assertEquals(AmbientStatus.Normal, controller.state.value.status)
        assertEquals(false, controller.state.value.breathingEnabled)
    }

    @Test
    fun `multi enter exit sequences are safe`() = runTest {
        val controller = AmbientController(mockContext, this)
        repeat(5) {
            controller.enter()
            advanceUntilIdle()
            controller.exit()
        }
        assertEquals(AmbientStatus.Normal, controller.state.value.status)
    }

    @Test
    fun `enter after exit stays Normal without sensor`() = runTest {
        val controller = AmbientController(mockContext, this)
        controller.enter()
        advanceUntilIdle()
        controller.exit()
        controller.enter()
        advanceUntilIdle()
        // No sensor → stays Normal
        assertEquals(AmbientStatus.Normal, controller.state.value.status)
    }

    @Test
    fun `dispose does not throw`() = runTest {
        val controller = AmbientController(mockContext, this)
        controller.enter()
        advanceUntilIdle()
        controller.dispose()
        assertEquals(AmbientStatus.Normal, controller.state.value.status)
    }
}
