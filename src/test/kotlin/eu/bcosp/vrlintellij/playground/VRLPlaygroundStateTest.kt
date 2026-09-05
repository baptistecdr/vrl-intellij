package eu.bcosp.vrlintellij.playground

import org.junit.Assert.assertEquals
import org.junit.Test

class VRLPlaygroundStateTest {

    @Test
    fun `returns an empty string for a file with no saved event`() {
        val state = VRLPlaygroundState()
        assertEquals("", state.sampleEventFor("file:///a.vrl"))
    }

    @Test
    fun `remembers the sample event per file`() {
        val state = VRLPlaygroundState()
        state.setSampleEventFor("file:///a.vrl", """{"a":1}""")
        state.setSampleEventFor("file:///b.vrl", """{"b":2}""")

        assertEquals("""{"a":1}""", state.sampleEventFor("file:///a.vrl"))
        assertEquals("""{"b":2}""", state.sampleEventFor("file:///b.vrl"))
    }

    @Test
    fun `clearing the event to blank removes it instead of storing blank text`() {
        val state = VRLPlaygroundState()
        state.setSampleEventFor("file:///a.vrl", """{"a":1}""")
        state.setSampleEventFor("file:///a.vrl", "   ")

        assertEquals("", state.sampleEventFor("file:///a.vrl"))
        assertEquals(0, state.state.sampleEventsByFileUrl.size)
    }

    @Test
    fun `state survives a save-load round trip`() {
        val original = VRLPlaygroundState()
        original.setSampleEventFor("file:///a.vrl", """{"a":1}""")

        val restored = VRLPlaygroundState()
        restored.loadState(original.state)

        assertEquals("""{"a":1}""", restored.sampleEventFor("file:///a.vrl"))
    }
}
