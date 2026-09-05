package eu.bcosp.vrlintellij.playground

import org.junit.Assert.assertEquals
import org.junit.Test

class VRLPlaygroundSettingsTest {

    @Test
    fun `defaults to resolving vector from PATH`() {
        assertEquals("vector", VRLPlaygroundSettings().vectorBinaryPath)
    }

    @Test
    fun `remembers a configured binary path`() {
        val settings = VRLPlaygroundSettings()
        settings.vectorBinaryPath = "/usr/local/bin/vector"
        assertEquals("/usr/local/bin/vector", settings.vectorBinaryPath)
    }

    @Test
    fun `state survives a save-load round trip`() {
        val original = VRLPlaygroundSettings()
        original.vectorBinaryPath = "/opt/vector/bin/vector"

        val restored = VRLPlaygroundSettings()
        restored.loadState(original.state)

        assertEquals("/opt/vector/bin/vector", restored.vectorBinaryPath)
    }

    @Test
    fun `external diagnostics default to disabled`() {
        // Opt-in: this spawns a `vector` process automatically on every edit once turned on,
        // unlike the Playground which only ever runs when the user presses Run.
        assertEquals(false, VRLPlaygroundSettings().externalDiagnosticsEnabled)
    }

    @Test
    fun `external diagnostics setting survives a save-load round trip`() {
        val original = VRLPlaygroundSettings()
        original.externalDiagnosticsEnabled = true

        val restored = VRLPlaygroundSettings()
        restored.loadState(original.state)

        assertEquals(true, restored.externalDiagnosticsEnabled)
    }
}
