package eu.bcosp.vrlintellij.hints

import com.intellij.codeInsight.hints.declarative.CollapseState
import com.intellij.codeInsight.hints.declarative.CollapsiblePresentationTreeBuilder
import com.intellij.codeInsight.hints.declarative.InlayActionData
import com.intellij.codeInsight.hints.declarative.InlayPayload
import com.intellij.codeInsight.hints.declarative.InlayPosition
import com.intellij.codeInsight.hints.declarative.InlayTreeSink
import com.intellij.codeInsight.hints.declarative.InlineInlayPosition
import com.intellij.codeInsight.hints.declarative.PresentationTreeBuilder
import com.intellij.codeInsight.hints.declarative.SharedBypassCollector
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiRecursiveElementWalkingVisitor
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import eu.bcosp.vrlintellij.psi.VRLElementTypes

class VRLParameterNameHintsProviderTest : BasePlatformTestCase() {

    private class RecordingSink : InlayTreeSink {
        data class Hint(val offset: Int, val text: String)

        val hints = mutableListOf<Hint>()

        override fun addPresentation(
            position: InlayPosition,
            payloads: List<InlayPayload>?,
            tooltip: String?,
            hintFormat: com.intellij.codeInsight.hints.declarative.HintFormat,
            builder: PresentationTreeBuilder.() -> Unit,
        ) {
            val offset = (position as InlineInlayPosition).offset
            val treeBuilder = RecordingPresentationTreeBuilder()
            treeBuilder.builder()
            hints.add(Hint(offset, treeBuilder.text.toString()))
        }

        override fun whenOptionEnabled(optionId: String, block: () -> Unit) = block()
    }

    private class RecordingPresentationTreeBuilder : PresentationTreeBuilder {
        val text = StringBuilder()
        override fun list(builder: PresentationTreeBuilder.() -> Unit) = builder()
        override fun collapsibleList(
            state: CollapseState,
            expandedState: CollapsiblePresentationTreeBuilder.() -> Unit,
            collapsedState: CollapsiblePresentationTreeBuilder.() -> Unit,
        ) = Unit

        override fun text(text: String, actionData: InlayActionData?) {
            this.text.append(text)
        }

        override fun clickHandlerScope(actionData: InlayActionData, builder: PresentationTreeBuilder.() -> Unit) =
            builder()
    }

    private fun collectHints(text: String): List<RecordingSink.Hint> {
        myFixture.configureByText("t.vrl", text)
        val collector = VRLParameterNameHintsProvider().createCollector(myFixture.file, myFixture.editor) as SharedBypassCollector
        val sink = RecordingSink()
        myFixture.file.accept(object : PsiRecursiveElementWalkingVisitor() {
            override fun visitElement(element: PsiElement) {
                if (element.node?.elementType == VRLElementTypes.FUNCTION_CALL) {
                    collector.collectFromElement(element, sink)
                }
                super.visitElement(element)
            }
        })
        return sink.hints
    }

    fun testShowsParameterNamesForMultiArgumentFunction() {
        val hints = collectHints("x = slice(.foo, 0, 5)\n")
        assertEquals(listOf("value:", "start:", "end:"), hints.map { it.text })
    }

    fun testSkipsTheFirstArgumentWhenItsTextAlreadyMatchesTheParameterName() {
        val hints = collectHints("value = 1\nx = slice(value, 0, 5)\n")
        assertEquals(listOf("start:", "end:"), hints.map { it.text })
    }

    fun testNoHintsForSingleParameterFunction() {
        assertTrue(collectHints("x = upcase(\"a\")\n").isEmpty())
    }

    fun testNoHintForUnknownFunction() {
        assertTrue(collectHints("x = totallymadeup(.a, .b)\n").isEmpty())
    }

    fun testAlreadyNamedArgumentsGetNoHint() {
        val hints = collectHints("x = slice(value: .foo, start: 0, end: 5)\n")
        assertTrue(hints.isEmpty())
    }

    fun testMixOfPositionalAndNamedOnlyHintsThePositionalOnes() {
        val hints = collectHints("x = slice(.foo, 0, end: 5)\n")
        assertEquals(listOf("value:", "start:"), hints.map { it.text })
    }
}
