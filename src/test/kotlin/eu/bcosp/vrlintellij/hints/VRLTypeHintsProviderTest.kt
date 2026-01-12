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
import com.intellij.psi.util.elementType
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import eu.bcosp.vrlintellij.psi.VRLElementTypes

class VRLTypeHintsProviderTest : BasePlatformTestCase() {

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
        val collector = VRLTypeHintsProvider().createCollector(myFixture.file, myFixture.editor) as SharedBypassCollector
        val sink = RecordingSink()
        myFixture.file.accept(object : PsiRecursiveElementWalkingVisitor() {
            override fun visitElement(element: PsiElement) {
                if (element.elementType == VRLElementTypes.ASSIGNMENT_EXPR ||
                    element.elementType == VRLElementTypes.MULTI_ASSIGNMENT_EXPR
                ) {
                    collector.collectFromElement(element, sink)
                }
                super.visitElement(element)
            }
        })
        return sink.hints
    }

    fun testShowsReturnTypeForDirectFunctionCallAssignment() {
        val hints = collectHints("x = parse_json(.message)\n")
        assertEquals(1, hints.size)
        assertEquals(": array|boolean|float|integer|null|object|string", hints[0].text)
    }

    fun testNoHintForPlainVariableAssignment() {
        assertTrue(collectHints("x = y\n").isEmpty())
    }

    fun testNoHintWhenResultIsCombinedWithAnOperator() {
        assertTrue(collectHints("x = parse_json(.message) ?? {}\n").isEmpty())
    }

    fun testNoHintForUnknownFunction() {
        assertTrue(collectHints("x = totallymadeup(.message)\n").isEmpty())
    }

    fun testShowsReturnTypeForInfallibleFunctionToo() {
        val hints = collectHints("x = upcase(\"a\")\n")
        assertEquals(1, hints.size)
        assertEquals(": string", hints[0].text)
    }

    fun testErrorDestructuringAssignmentShowsBothTargets() {
        val hints = collectHints("value, err = parse_json(.message)\n")
        assertEquals(2, hints.size)
        assertEquals(": array|boolean|float|integer|null|object|string", hints[0].text)
        assertEquals(": error", hints[1].text)
    }

    fun testHintOffsetIsRightAfterTheTargetIdentifier() {
        val text = "count = upcase(\"a\")\n"
        val hints = collectHints(text)
        assertEquals(1, hints.size)
        assertEquals(text.indexOf("count") + "count".length, hints[0].offset)
    }
}
