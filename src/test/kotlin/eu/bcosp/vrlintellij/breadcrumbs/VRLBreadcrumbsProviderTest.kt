package eu.bcosp.vrlintellij.breadcrumbs

import com.intellij.psi.util.PsiTreeUtil
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import eu.bcosp.vrlintellij.psi.VRLClosureExpr
import eu.bcosp.vrlintellij.psi.VRLIfExpr

class VRLBreadcrumbsProviderTest : BasePlatformTestCase() {

    private val provider = VRLBreadcrumbsProvider()

    fun `test accepts an if expression and describes its condition`() {
        myFixture.configureByText("t.vrl", "if .status == 200 {\nx = 1;\n}\n")
        val ifExpr = PsiTreeUtil.findChildOfType(myFixture.file, VRLIfExpr::class.java)!!
        assertTrue(provider.acceptElement(ifExpr))
        assertEquals("if .status == 200", provider.getElementInfo(ifExpr))
    }

    fun `test accepts a closure expression and describes its parameters`() {
        myFixture.configureByText("t.vrl", "x = map(.items) -> |item| { item }\n")
        val closure = PsiTreeUtil.findChildOfType(myFixture.file, VRLClosureExpr::class.java)!!
        assertTrue(provider.acceptElement(closure))
        assertEquals("|item|", provider.getElementInfo(closure))
    }

    fun `test does not accept an unrelated element`() {
        myFixture.configureByText("t.vrl", "x = 1;\n")
        assertFalse(provider.acceptElement(myFixture.file))
    }

    fun `test truncates a long if condition`() {
        val longCondition = (1..10).joinToString(" && ") { ".field$it == $it" }
        myFixture.configureByText("t.vrl", "if $longCondition {\nx = 1;\n}\n")
        val ifExpr = PsiTreeUtil.findChildOfType(myFixture.file, VRLIfExpr::class.java)!!
        val info = provider.getElementInfo(ifExpr)
        assertTrue(info.length <= 34)
        assertTrue(info.endsWith("…"))
    }
}
