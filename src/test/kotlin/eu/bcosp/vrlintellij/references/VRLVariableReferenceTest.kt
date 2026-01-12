package eu.bcosp.vrlintellij.references

import com.intellij.testFramework.fixtures.BasePlatformTestCase

/** End-to-end tests for the real Go to Declaration / Find Usages code paths (as opposed to
 * [VRLVariableResolverTest], which tests [VRLVariableResolver]'s resolution logic directly). */
class VRLVariableReferenceTest : BasePlatformTestCase() {

    fun testVariableUsageResolvesToItsDeclaration() {
        myFixture.configureByText("t.vrl", "count = 1;\nx = count;\n")
        val usageOffset = myFixture.file.text.lastIndexOf("count") + 1
        val reference = myFixture.file.findReferenceAt(usageOffset)
        assertNotNull(reference)
        val resolved = reference!!.resolve()
        assertNotNull(resolved)
        assertEquals(myFixture.file.text.indexOf("count"), resolved!!.textRange.startOffset)
    }

    fun testDeclarationItselfHasNoReference() {
        myFixture.configureByText("t.vrl", "count = 1;\n")
        val declOffset = myFixture.file.text.indexOf("count") + 1
        assertNull(myFixture.file.findReferenceAt(declOffset))
    }

    fun testFunctionCallTokenHasNoVariableReference() {
        myFixture.configureByText("t.vrl", "upcase(\"x\")")
        val offset = myFixture.file.text.indexOf("upcase") + 1
        assertNull(myFixture.file.findReferenceAt(offset))
    }

    fun testFindUsagesFindsAllReadsButNotLaterReassignments() {
        myFixture.configureByText("t.vrl", "count = 1;\nx = count;\ny = count;\ncount = 2;\nz = count;\n")
        val firstDeclOffset = myFixture.file.text.indexOf("count") + 1
        // Find Usages targets are PRIMARY_EXPR/CLOSURE_PARAM, not the bare IDENTIFIER leaf.
        val firstDecl = myFixture.file.findElementAt(firstDeclOffset)!!.parent
        val usages = myFixture.findUsages(firstDecl)
        assertEquals(2, usages.size)
    }

    fun testFindUsagesOnSecondDeclarationFindsOnlyLaterReads() {
        myFixture.configureByText("t.vrl", "count = 1;\nx = count;\ncount = 2;\ny = count;\n")
        // Occurrences in order: [0] "count = 1" decl, [1] "x = count" read, [2] "count = 2" decl,
        // [3] "y = count" read. We want the offset of occurrence [2].
        val text = myFixture.file.text
        var end = text.indexOf("count") + "count".length
        end = text.indexOf("count", end) + "count".length
        val secondDeclOffset = text.indexOf("count", end) + 1
        val secondDecl = myFixture.file.findElementAt(secondDeclOffset)!!.parent
        val usages = myFixture.findUsages(secondDecl)
        assertEquals(1, usages.size)
    }
}
