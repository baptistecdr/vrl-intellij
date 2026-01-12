package eu.bcosp.vrlintellij.colors

import junit.framework.TestCase

class VRLColorSettingsPageTest : TestCase() {

    fun testAttributeDescriptorsCoverEveryColor() {
        val page = VRLColorSettingsPage()
        assertEquals(VRLColor.entries.size, page.attributeDescriptors.size)
    }

    fun testDemoTextTagsAreAllDeclaredColors() {
        val page = VRLColorSettingsPage()
        val declaredTags = page.additionalHighlightingTagToDescriptorMap.keys
        val usedTags = Regex("<([A-Z_]+)>").findAll(page.demoText).map { it.groupValues[1] }.toSet()
        for (tag in usedTags) {
            assertTrue("demo text uses <$tag> but VRLColor declares no such entry", tag in declaredTags)
        }
    }

    fun testDemoTextTagsAreBalanced() {
        val page = VRLColorSettingsPage()
        val opens = Regex("<([A-Z_]+)>").findAll(page.demoText).map { it.groupValues[1] }.toList()
        val closes = Regex("</([A-Z_]+)>").findAll(page.demoText).map { it.groupValues[1] }.toList()
        assertEquals(opens.sorted(), closes.sorted())
    }
}
