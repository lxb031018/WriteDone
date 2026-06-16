package me.lxb.writedone.ambient

import org.junit.Assert.assertEquals
import org.junit.Test

class AmbientControllerTest {

    @Test
    fun `initial state is Normal with breathing disabled`() {
        val controller = AmbientController()
        assertEquals(AmbientStatus.Normal, controller.state.value.status)
        assertEquals(false, controller.state.value.breathingEnabled)
    }

    @Test
    fun `enter immediately sets status to Active`() {
        val controller = AmbientController()
        controller.enter()
        assertEquals(AmbientStatus.Active, controller.state.value.status)
    }

    @Test
    fun `enter does not immediately enable breathing`() {
        val controller = AmbientController()
        controller.enter()
        assertEquals(false, controller.state.value.breathingEnabled)
    }

    @Test
    fun `exit resets to Normal`() {
        val controller = AmbientController()
        controller.enter()
        controller.exit()
        assertEquals(AmbientStatus.Normal, controller.state.value.status)
        assertEquals(false, controller.state.value.breathingEnabled)
    }

    @Test
    fun `multi enter exit sequences are safe`() {
        val controller = AmbientController()
        repeat(5) {
            controller.enter()
            controller.exit()
        }
        assertEquals(AmbientStatus.Normal, controller.state.value.status)
    }

    @Test
    fun `enter after exit sets Active again`() {
        val controller = AmbientController()
        controller.enter()
        controller.exit()
        controller.enter()
        assertEquals(AmbientStatus.Active, controller.state.value.status)
    }

    @Test
    fun `dispose does not throw`() {
        val controller = AmbientController()
        controller.enter()
        controller.dispose()
        // dispose only cancels job, doesn't reset state
        assertEquals(AmbientStatus.Active, controller.state.value.status)
    }
}
