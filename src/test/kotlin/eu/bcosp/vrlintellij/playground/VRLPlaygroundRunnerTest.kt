package eu.bcosp.vrlintellij.playground

import org.junit.Assert.assertTrue
import org.junit.Test

class VRLPlaygroundRunnerTest {

    @Test
    fun `reports a friendly error when the binary can't be found`() {
        val result = VRLPlaygroundRunner.run(
            vectorBinaryPath = "eu-bcosp-vrl-intellij-definitely-not-a-real-executable",
            program = ".",
            sampleEvent = "{}",
        )

        assertTrue(result is VRLPlaygroundResult.Failure)
        val message = (result as VRLPlaygroundResult.Failure).message
        assertTrue(message.contains("eu-bcosp-vrl-intellij-definitely-not-a-real-executable"))
        assertTrue(message.contains("Settings"))
    }
}
