package eu.bcosp.vrlintellij.diagnostics

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import eu.bcosp.vrlintellij.playground.VRLPlaygroundSettings

class VRLExternalAnnotatorTest : BasePlatformTestCase() {

    private val annotator = VRLExternalAnnotator()

    override fun tearDown() {
        try {
            val settings = VRLPlaygroundSettings.getInstance()
            settings.externalDiagnosticsEnabled = false
            settings.vectorBinaryPath = "vector"
        } finally {
            super.tearDown()
        }
    }

    fun testCollectInformationReturnsNullWhenDisabled() {
        val settings = VRLPlaygroundSettings.getInstance()
        settings.externalDiagnosticsEnabled = false
        settings.vectorBinaryPath = "vector"
        myFixture.configureByText("t.vrl", ".x = 1")

        assertNull(annotator.collectInformation(myFixture.file))
    }

    fun testCollectInformationReturnsNullWhenBinaryPathIsBlank() {
        val settings = VRLPlaygroundSettings.getInstance()
        settings.externalDiagnosticsEnabled = true
        settings.vectorBinaryPath = "   "
        myFixture.configureByText("t.vrl", ".x = 1")

        assertNull(annotator.collectInformation(myFixture.file))
    }

    fun testCollectInformationReturnsTheFileTextAndConfiguredPathWhenEnabled() {
        val settings = VRLPlaygroundSettings.getInstance()
        settings.externalDiagnosticsEnabled = true
        settings.vectorBinaryPath = "/opt/vector/bin/vector"
        myFixture.configureByText("t.vrl", ".x = 1")

        val info = annotator.collectInformation(myFixture.file)

        assertNotNull(info)
        assertEquals(".x = 1", info!!.programText)
        assertEquals("/opt/vector/bin/vector", info.vectorBinaryPath)
    }

    fun testDoAnnotateReturnsNullWhenTheConfiguredBinaryCannotBeRun() {
        val info = VRLDiagnosticsInfo(programText = ".x = 1", vectorBinaryPath = "/no/such/vector-binary-at-all")

        assertNull(annotator.doAnnotate(info))
    }

    // End-to-end through the real highlighting pipeline (registration in plugin.xml, collectInformation,
    // doAnnotate spawning a real "vector" process, and apply() mapping its position back into this
    // document) - skipped when "vector" isn't on PATH, e.g. on CI, matching
    // VRLPlaygroundRunnerIntegrationTest's pattern (JUnit's Assume doesn't apply to this JUnit3-style
    // BasePlatformTestCase, so this just returns early instead).
    fun testAnnotatesARealCompileErrorAtTheRightSpotThroughTheFullPipeline() {
        if (!vectorAvailable()) return

        val settings = VRLPlaygroundSettings.getInstance()
        settings.externalDiagnosticsEnabled = true
        settings.vectorBinaryPath = "vector"

        myFixture.configureByText("t.vrl", "x = parse_json(.message)\n")
        val highlights = myFixture.doHighlighting()

        val highlight = highlights.singleOrNull { it.description?.startsWith("vector: ") == true }
        assertNotNull("expected one \"vector: \" highlight, got: $highlights", highlight)
        assertTrue(highlight!!.description.contains("E103"))
        assertTrue(highlight.description.contains("unhandled fallible assignment"))
        // "parse_json(.message)" starts right after "x = " (offset 4) - the compiler reports the
        // error at that column, and apply() should anchor the highlight there, not at offset 0.
        assertEquals(4, highlight.startOffset)
    }

    private fun vectorAvailable(): Boolean = try {
        ProcessBuilder("vector", "--version").start().waitFor() == 0
    } catch (e: Exception) {
        false
    }
}
