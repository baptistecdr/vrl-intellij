package eu.bcosp.vrlintellij.formatting

import com.intellij.openapi.command.WriteCommandAction
import com.intellij.psi.codeStyle.CodeStyleManager
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class VRLFormattingModelBuilderTest : BasePlatformTestCase() {

    private fun reformat(text: String): String {
        myFixture.configureByText("t.vrl", text)
        WriteCommandAction.runWriteCommandAction(project) {
            CodeStyleManager.getInstance(project).reformat(myFixture.file)
        }
        return myFixture.file.text
    }

    fun testIndentsStatementsInsideAnIfBlock() {
        val result = reformat("if true {\ncount = 1;\n}\n")
        assertEquals("if true {\n    count = 1;\n}\n", result)
    }

    fun testIndentsNestedBlocks() {
        val result = reformat("if true {\nif false {\ncount = 1;\n}\n}\n")
        assertEquals("if true {\n    if false {\n        count = 1;\n    }\n}\n", result)
    }

    fun testIndentsObjectLiteralFields() {
        val result = reformat("x = {\n\"a\": 1,\n\"b\": 2\n}\n")
        assertEquals("x = {\n    \"a\": 1,\n    \"b\": 2\n}\n", result)
    }

    fun testFixesSpacingAroundOperatorsAndCommas() {
        val result = reformat("x=1+2;\ny=[1,2,3];\n")
        assertEquals("x = 1 + 2;\ny = [1, 2, 3];\n", result)
    }

    fun testKeepsPathSegmentsTight() {
        val result = reformat("x=.foo.bar;\n")
        assertEquals("x = .foo.bar;\n", result)
    }

    fun testDoesNotTouchAlreadySingleLineCode() {
        val result = reformat("x = upcase(\"a\")\n")
        assertEquals("x = upcase(\"a\")\n", result)
    }

    fun testIndentsMultiLineArrayElements() {
        val result = reformat("x = [\n1,\n2,\n3\n]\n")
        assertEquals("x = [\n    1,\n    2,\n    3\n]\n", result)
    }

    fun testIndentsMultipleStatementsInABlockConsistently() {
        val result = reformat("if true {\na = 1;\nb = 2;\nc = 3;\n}\n")
        assertEquals("if true {\n    a = 1;\n    b = 2;\n    c = 3;\n}\n", result)
    }

    fun testIndentsElseBranchBlockAtTheSameLevelAsIf() {
        val result = reformat("if true {\na = 1;\n} else {\nb = 2;\n}\n")
        assertEquals("if true {\n    a = 1;\n} else {\n    b = 2;\n}\n", result)
    }

    fun testIndentsClosureBodyOfAFunctionCall() {
        val result = reformat("map_values(.) -> |value| {\nupcase(value)\n}\n")
        assertEquals("map_values(.) -> |value| {\n    upcase(value)\n}\n", result)
    }
}
