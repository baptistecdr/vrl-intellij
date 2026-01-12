package eu.bcosp.vrlintellij.folding

import com.intellij.testFramework.fixtures.BasePlatformTestCase

class VRLFoldingBuilderTest : BasePlatformTestCase() {

    fun testFoldsMultilineBlock() {
        myFixture.configureByText("t.vrl", "if true {\n  x = 1;\n}\n")
        val descriptors = VRLFoldingBuilder().buildFoldRegions(myFixture.file, myFixture.editor.document, false)
        assertEquals(1, descriptors.size)
        assertEquals("{...}", VRLFoldingBuilder().getPlaceholderText(descriptors[0].element))
    }

    fun testDoesNotFoldSingleLineBlock() {
        myFixture.configureByText("t.vrl", "if true { x = 1; }\n")
        val descriptors = VRLFoldingBuilder().buildFoldRegions(myFixture.file, myFixture.editor.document, false)
        assertTrue(descriptors.isEmpty())
    }

    fun testFoldsMultilineArrayWithBracketPlaceholder() {
        myFixture.configureByText("t.vrl", "x = [\n  1,\n  2\n];\n")
        val descriptors = VRLFoldingBuilder().buildFoldRegions(myFixture.file, myFixture.editor.document, false)
        assertEquals(1, descriptors.size)
        assertEquals("[...]", VRLFoldingBuilder().getPlaceholderText(descriptors[0].element))
    }

    fun testFoldsMultilineObject() {
        myFixture.configureByText("t.vrl", "x = {\n  \"a\": 1\n};\n")
        val descriptors = VRLFoldingBuilder().buildFoldRegions(myFixture.file, myFixture.editor.document, false)
        assertEquals(1, descriptors.size)
        assertEquals("{...}", VRLFoldingBuilder().getPlaceholderText(descriptors[0].element))
    }

    fun testNotCollapsedByDefault() {
        myFixture.configureByText("t.vrl", "if true {\n  x = 1;\n}\n")
        val descriptors = VRLFoldingBuilder().buildFoldRegions(myFixture.file, myFixture.editor.document, false)
        assertFalse(VRLFoldingBuilder().isCollapsedByDefault(descriptors[0].element))
    }
}
