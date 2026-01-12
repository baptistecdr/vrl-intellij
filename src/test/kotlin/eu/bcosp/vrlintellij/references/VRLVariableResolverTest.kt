package eu.bcosp.vrlintellij.references

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiRecursiveElementWalkingVisitor
import com.intellij.testFramework.ParsingTestCase
import eu.bcosp.vrlintellij.psi.VRLElementTypes
import eu.bcosp.vrlintellij.psi.VRLParserDefinition
import eu.bcosp.vrlintellij.psi.VRLPrimaryExpr

class VRLVariableResolverTest : ParsingTestCase("", "vrl", VRLParserDefinition()) {
    override fun getTestDataPath(): String = "src/test/testData"

    private fun identifiersNamed(root: PsiElement, name: String): List<PsiElement> {
        val result = mutableListOf<PsiElement>()
        root.accept(object : PsiRecursiveElementWalkingVisitor() {
            override fun visitElement(element: PsiElement) {
                if (element.node?.elementType == VRLElementTypes.PRIMARY_EXPR) {
                    val id = (element as? VRLPrimaryExpr)?.identifier
                    if (id != null && id.text == name) result.add(id)
                }
                super.visitElement(element)
            }
        })
        return result
    }

    fun testUsageResolvesToNearestPrecedingAssignment() {
        val file = createPsiFile("t", "a = 1;\nc = a;\na = 2;\nd = a;\n")
        val occurrences = identifiersNamed(file, "a")
        assertEquals(4, occurrences.size)
        // resolve() now returns the declaration's wrapping PRIMARY_EXPR (not the bare identifier
        // leaf), so it can double as a PsiNameIdentifierOwner rename target.
        assertEquals(occurrences[0].parent, VRLVariableResolver.resolve(occurrences[1]))
        assertEquals(occurrences[2].parent, VRLVariableResolver.resolve(occurrences[3]))
    }

    fun testResolvingBeforeAnyAssignmentExistsReturnsNull() {
        val file = createPsiFile("t", "a = 1;\n")
        val occurrences = identifiersNamed(file, "a")
        assertNull(VRLVariableResolver.resolve(occurrences[0]))
    }

    fun testUnassignedNameResolvesToNull() {
        val file = createPsiFile("t", "x = never_assigned;\n")
        val usage = identifiersNamed(file, "never_assigned").single()
        assertNull(VRLVariableResolver.resolve(usage))
    }

    fun testBareAssignmentIsADeclaration() {
        val file = createPsiFile("t", "a = 1;\n")
        val a = identifiersNamed(file, "a").single()
        assertTrue(VRLVariableResolver.isBareAssignmentTarget(a.parent))
    }

    fun testDottedAssignmentTargetIsAUsageNotADeclaration() {
        val file = createPsiFile("t", "x = {};\nx.y = 1;\n")
        val occurrences = identifiersNamed(file, "x")
        assertEquals(2, occurrences.size)
        assertTrue(VRLVariableResolver.isBareAssignmentTarget(occurrences[0].parent))
        assertFalse(VRLVariableResolver.isBareAssignmentTarget(occurrences[1].parent))
        assertEquals(occurrences[0].parent, VRLVariableResolver.resolve(occurrences[1]))
    }

    fun testIndexedAssignmentTargetIsAUsageNotADeclaration() {
        val file = createPsiFile("t", "x = [];\nx[0] = 1;\n")
        val occurrences = identifiersNamed(file, "x")
        assertEquals(2, occurrences.size)
        assertFalse(VRLVariableResolver.isBareAssignmentTarget(occurrences[1].parent))
    }

    fun testPlainReadIsNotADeclaration() {
        val file = createPsiFile("t", "a = 1;\nb = a;\n")
        val occurrences = identifiersNamed(file, "a")
        assertFalse(VRLVariableResolver.isBareAssignmentTarget(occurrences[1].parent))
    }

    fun testClosureParamShadowsOuterVariable() {
        val file = createPsiFile("t", "value = 1;\nmap_values(.) -> |value| { upcase(value) }\n")
        val outerAndUsage = identifiersNamed(file, "value")
        // the closure param itself isn't PRIMARY_EXPR-parented, so this only finds the outer
        // declaration and the read inside upcase(value)
        assertEquals(2, outerAndUsage.size)
        val usageInClosure = outerAndUsage[1]
        val resolved = VRLVariableResolver.resolve(usageInClosure)
        assertNotNull(resolved)
        assertTrue(resolved!!.textRange.startOffset != outerAndUsage[0].textRange.startOffset)
    }

    fun testVisibleVariableNamesIncludesPrecedingAssignmentsAndClosureParams() {
        val file = createPsiFile("t", "count = 1;\nmap_values(.) -> |item| { x = item; }\n")
        val contextElement = identifiersNamed(file, "item").single()
        val names = VRLVariableResolver.visibleVariableNames(contextElement.textRange.startOffset, contextElement)
        assertTrue(names.contains("count"))
        assertTrue(names.contains("item"))
    }

    fun testVisibleVariableNamesExcludesAssignmentsAfterTheOffset() {
        val file = createPsiFile("t", "a = 1;\nb = 2;\n")
        val bDecl = identifiersNamed(file, "b").single()
        val names = VRLVariableResolver.visibleVariableNames(bDecl.textRange.startOffset, bDecl)
        assertTrue(names.contains("a"))
        assertFalse(names.contains("b"))
    }
}
