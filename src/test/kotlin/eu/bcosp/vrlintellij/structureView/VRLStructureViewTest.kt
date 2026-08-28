package eu.bcosp.vrlintellij.structureView

import com.intellij.ide.structureView.TreeBasedStructureViewBuilder
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class VRLStructureViewTest : BasePlatformTestCase() {

    private fun builder() = VRLStructureViewFactory().getStructureViewBuilder(myFixture.file) as TreeBasedStructureViewBuilder

    private fun rootChildren(text: String): List<VRLStructureViewElement> {
        myFixture.configureByText("t.vrl", text)
        val root = builder().createStructureViewModel(null).root as VRLStructureViewElement
        return root.childrenBase.map { it as VRLStructureViewElement }
    }

    private fun childrenOf(element: VRLStructureViewElement): List<String> =
        element.childrenBase.map { (it as VRLStructureViewElement).presentableText }

    fun testFileRootShowsFileName() {
        myFixture.configureByText("t.vrl", "x = 1\n")
        val root = builder().createStructureViewModel(null).root as VRLStructureViewElement
        assertEquals("t.vrl", root.presentableText)
    }

    fun testTopLevelAssignmentsAreLabeledByTheirTarget() {
        // Semicolon-terminated: an unterminated `x = 1` immediately followed by a line starting
        // with `.` would parse as `x = 1.foo` (a single statement) rather than two statements -
        // see the VRLParsingTest regression coverage for that pre-existing grammar gap.
        val children = rootChildren("x = 1;\n.foo = 2\n")
        assertEquals(listOf("x =", ".foo ="), children.map { it.presentableText })
    }

    fun testMergeAssignOperatorIsShown() {
        val children = rootChildren(". |= {}\n")
        assertEquals(listOf(". |="), children.map { it.presentableText })
    }

    fun testMultiTargetErrorAssignmentIsLabeledByBothTargets() {
        val children = rootChildren("value, err = parse_json(.message)\n")
        assertEquals(listOf("value, err"), children.map { it.presentableText })
    }

    fun testIfStatementIsLabeledByItsCondition() {
        val children = rootChildren("if .status == 200 {\nx = 1\n}\n")
        assertEquals(listOf("if .status == 200"), children.map { it.presentableText })
    }

    fun testIfElseChildrenIncludeBothBranches() {
        val children = rootChildren("if true {\ny = 3\n} else {\nz = 4\n}\n")
        assertEquals(1, children.size)
        assertEquals(listOf("y =", "z ="), childrenOf(children[0]))
    }

    fun testClosureBodyStatementsAreNestedUnderTheCallStatement() {
        val children = rootChildren("map_values(.) -> |v| {\nupcase(v)\n}\n")
        assertEquals(1, children.size)
        assertEquals(listOf("upcase(v)"), childrenOf(children[0]))
    }

    fun testDeeplyNestedIfIsNestedNotFlattened() {
        val children = rootChildren("if a {\nif b {\nx = 1\n}\n}\n")
        assertEquals(listOf("if a"), children.map { it.presentableText })
        val nested = children[0].childrenBase.map { it as VRLStructureViewElement }
        assertEquals(listOf("if b"), nested.map { it.presentableText })
        assertEquals(listOf("x ="), childrenOf(nested[0]))
    }

    fun testBareFunctionCallStatementFallsBackToItsText() {
        val children = rootChildren("abort\n")
        assertEquals(listOf("abort"), children.map { it.presentableText })
    }
}
