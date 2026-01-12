package eu.bcosp.vrlintellij.highlighting

import com.intellij.psi.tree.IElementType
import eu.bcosp.vrlintellij.colors.VRLColor
import eu.bcosp.vrlintellij.psi.VRLElementTypes
import junit.framework.TestCase

class VRLHighlighterTest : TestCase() {

    private fun colorFor(type: IElementType) = VRLHighlighter().getTokenHighlights(type).toList()

    fun testKeywordsAreColored() {
        assertEquals(listOf(VRLColor.KEYWORD.textAttributesKey), colorFor(VRLElementTypes.IF))
        assertEquals(listOf(VRLColor.KEYWORD.textAttributesKey), colorFor(VRLElementTypes.IN))
    }

    fun testFunctionCallIsColoredDistinctlyFromIdentifier() {
        assertEquals(listOf(VRLColor.FUNCTION_CALL.textAttributesKey), colorFor(VRLElementTypes.FUNCTION_CALL))
        assertEquals(listOf(VRLColor.IDENTIFIER.textAttributesKey), colorFor(VRLElementTypes.IDENTIFIER))
        assertTrue(VRLColor.FUNCTION_CALL.textAttributesKey != VRLColor.IDENTIFIER.textAttributesKey)
    }

    fun testRegexAndTimestampLiteralsAreColored() {
        assertEquals(listOf(VRLColor.REGEX.textAttributesKey), colorFor(VRLElementTypes.REGEX))
        assertEquals(listOf(VRLColor.TIMESTAMP.textAttributesKey), colorFor(VRLElementTypes.TIMESTAMP))
    }

    fun testStringAndRawStringShareTheStringColor() {
        assertEquals(listOf(VRLColor.STRING.textAttributesKey), colorFor(VRLElementTypes.STRING))
        assertEquals(listOf(VRLColor.STRING.textAttributesKey), colorFor(VRLElementTypes.RAW_STRING))
    }

    fun testQuestionIsAnOperator() {
        assertEquals(listOf(VRLColor.OPERATOR.textAttributesKey), colorFor(VRLElementTypes.QUESTION))
    }

    fun testArrowHasItsOwnColor() {
        assertEquals(listOf(VRLColor.ARROW.textAttributesKey), colorFor(VRLElementTypes.ARROW))
    }
}
