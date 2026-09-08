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

    fun testFlagsInvalidCalendarDate() {
        myFixture.configureByText("t.vrl", "t'2021-13-01T00:00:00Z'")
        assertTrue(hasInvalidTimestampWarning())
    }

    fun testFlagsSecondOutOfRange() {
        myFixture.configureByText("t.vrl", "t'2021-01-01T23:59:61Z'")
        assertTrue(hasInvalidTimestampWarning())
    }

    fun testFlagsShortOffset() {
        myFixture.configureByText("t.vrl", "t'2021-01-01T00:00:00+01'")
        assertTrue(hasInvalidTimestampWarning())
    }

    // VRL parses `t'...'` with chrono's `DateTime<Utc>: FromStr`, documented as "a relaxed form
    // of RFC 3339" - a space is accepted as the date/time separator (vector vrl accepts
    // 't\'2021-01-01 00:00:00Z\'' outright).
    fun testDoesNotFlagSpaceInsteadOfT() {
        myFixture.configureByText("t.vrl", "t'2021-01-01 00:00:00Z'")
        assertFalse(hasInvalidTimestampWarning())
    }

    fun testDoesNotFlagLowercaseSeparators() {
        myFixture.configureByText("t.vrl", "t'2021-01-01t00:00:00z'")
        assertFalse(hasInvalidTimestampWarning())
    }

    fun testDoesNotFlagOffsetWithoutColon() {
        myFixture.configureByText("t.vrl", "t'2021-01-01T00:00:00+0100'")
        assertFalse(hasInvalidTimestampWarning())
    }

    fun testDoesNotFlagLeapSecond() {
        myFixture.configureByText("t.vrl", "t'2021-01-01T10:32:60Z'")
        assertFalse(hasInvalidTimestampWarning())
    }

    fun testDoesNotFlagUnpaddedFields() {
        myFixture.configureByText("t.vrl", "t'2021-1-5T0:0:0Z'")
        assertFalse(hasInvalidTimestampWarning())
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
