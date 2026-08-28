package eu.bcosp.vrlintellij.parameterInfo

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.testFramework.utils.parameterInfo.MockCreateParameterInfoContext
import com.intellij.testFramework.utils.parameterInfo.MockParameterInfoUIContext
import com.intellij.testFramework.utils.parameterInfo.MockUpdateParameterInfoContext
import eu.bcosp.vrlintellij.functions.VRLFunction

// parse_timestamp(value: string, format: string, timezone: string = optional) is used throughout
// as a fixed 3-argument function whose last argument is optional.
class VRLParameterInfoHandlerTest : BasePlatformTestCase() {

    private val handler = VRLParameterInfoHandler()

    fun testFindsKnownFunctionSignature() {
        myFixture.configureByText("t.vrl", "x = parse_timestamp(.timestamp, <caret>\"%Y\")\n")
        val context = MockCreateParameterInfoContext(myFixture.editor, myFixture.file)
        val element = handler.findElementForParameterInfo(context)
        assertNotNull(element)
        assertEquals(1, context.itemsToShow?.size)
        assertEquals("parse_timestamp", (context.itemsToShow!![0] as VRLFunction).name)
    }

    fun testReturnsNullOutsideACall() {
        myFixture.configureByText("t.vrl", "x =<caret> 1\n")
        val context = MockCreateParameterInfoContext(myFixture.editor, myFixture.file)
        assertNull(handler.findElementForParameterInfo(context))
    }

    fun testReturnsNullForUnknownFunction() {
        myFixture.configureByText("t.vrl", "x = totally_unknown(<caret>1, 2)\n")
        val context = MockCreateParameterInfoContext(myFixture.editor, myFixture.file)
        assertNull(handler.findElementForParameterInfo(context))
    }

    fun testCurrentParameterIsZeroAtFirstArgument() {
        myFixture.configureByText("t.vrl", "x = parse_timestamp(<caret>.timestamp, \"%Y\")\n")
        val element = handler.findElementForParameterInfo(MockCreateParameterInfoContext(myFixture.editor, myFixture.file))!!
        val updateContext = MockUpdateParameterInfoContext(myFixture.editor, myFixture.file)
        handler.updateParameterInfo(element, updateContext)
        assertEquals(0, updateContext.currentParameter)
    }

    fun testCurrentParameterAdvancesWithEachArgument() {
        myFixture.configureByText("t.vrl", "x = parse_timestamp(.timestamp, \"%Y\", <caret>\"UTC\")\n")
        val element = handler.findElementForParameterInfo(MockCreateParameterInfoContext(myFixture.editor, myFixture.file))!!
        val updateContext = MockUpdateParameterInfoContext(myFixture.editor, myFixture.file)
        handler.updateParameterInfo(element, updateContext)
        assertEquals(2, updateContext.currentParameter)
    }

    fun testUiPresentationHighlightsCurrentParameterAndMarksOptionalArgument() {
        myFixture.configureByText("t.vrl", "x = parse_timestamp(.timestamp, <caret>\"%Y\")\n")
        val createContext = MockCreateParameterInfoContext(myFixture.editor, myFixture.file)
        val element = handler.findElementForParameterInfo(createContext)!!
        val function = createContext.itemsToShow!![0] as VRLFunction
        val updateContext = MockUpdateParameterInfoContext(myFixture.editor, myFixture.file)
        handler.updateParameterInfo(element, updateContext)

        val uiContext = MockParameterInfoUIContext(element)
        uiContext.setCurrentParameterIndex(updateContext.currentParameter)
        handler.updateUI(function, uiContext)

        val text = uiContext.text!!
        assertTrue(text.contains("value: string"))
        assertTrue(text.contains("format: string"))
        assertTrue(text.contains("timezone?: string"))
        assertEquals("format: string", text.substring(uiContext.highlightStart, uiContext.highlightEnd))
    }

    fun testUiPresentationForZeroArgumentFunction() {
        myFixture.configureByText("t.vrl", "x = now(<caret>)\n")
        val createContext = MockCreateParameterInfoContext(myFixture.editor, myFixture.file)
        val element = handler.findElementForParameterInfo(createContext)!!
        val function = createContext.itemsToShow!![0] as VRLFunction
        val uiContext = MockParameterInfoUIContext(element)
        handler.updateUI(function, uiContext)
        assertEquals("<no arguments>", uiContext.text)
    }
}
