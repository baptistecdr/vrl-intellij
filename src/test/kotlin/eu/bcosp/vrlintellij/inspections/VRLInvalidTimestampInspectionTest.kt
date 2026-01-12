package eu.bcosp.vrlintellij.inspections

import com.intellij.testFramework.fixtures.BasePlatformTestCase

class VRLInvalidTimestampInspectionTest : BasePlatformTestCase() {

    override fun setUp() {
        super.setUp()
        myFixture.enableInspections(VRLInvalidTimestampInspection())
    }

    private fun hasInvalidTimestampWarning(): Boolean =
        myFixture.doHighlighting().any { it.description?.contains("Invalid timestamp literal") == true }

    fun testFlagsGarbageContent() {
        myFixture.configureByText("t.vrl", "t'not a timestamp'")
        assertTrue(hasInvalidTimestampWarning())
    }

    fun testFlagsMissingOffset() {
        myFixture.configureByText("t.vrl", "t'2021-01-01T00:00:00'")
        assertTrue(hasInvalidTimestampWarning())
    }

    fun testFlagsSpaceInsteadOfT() {
        myFixture.configureByText("t.vrl", "t'2021-01-01 00:00:00Z'")
        assertTrue(hasInvalidTimestampWarning())
    }

    fun testFlagsLowercaseSeparators() {
        myFixture.configureByText("t.vrl", "t'2021-01-01t00:00:00z'")
        assertTrue(hasInvalidTimestampWarning())
    }

    fun testFlagsInvalidCalendarDate() {
        myFixture.configureByText("t.vrl", "t'2021-13-01T00:00:00Z'")
        assertTrue(hasInvalidTimestampWarning())
    }

    fun testDoesNotFlagUtcTimestamp() {
        myFixture.configureByText("t.vrl", "t'2021-01-01T00:00:00Z'")
        assertFalse(hasInvalidTimestampWarning())
    }

    fun testDoesNotFlagNanosecondPrecisionTimestamp() {
        myFixture.configureByText("t.vrl", "t'2021-02-11T10:32:50.553955473Z'")
        assertFalse(hasInvalidTimestampWarning())
    }

    fun testDoesNotFlagOffsetTimestamp() {
        myFixture.configureByText("t.vrl", "t'2021-02-11T10:32:50.553-04:00'")
        assertFalse(hasInvalidTimestampWarning())
    }
}
