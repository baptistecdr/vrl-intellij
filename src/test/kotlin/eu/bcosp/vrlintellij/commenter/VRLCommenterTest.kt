package eu.bcosp.vrlintellij.commenter

import com.intellij.lang.LanguageCommenters
import eu.bcosp.vrlintellij.VRL
import junit.framework.TestCase

class VRLCommenterTest : TestCase() {

    // Note: an end-to-end "invoke Ctrl+/ and check the result" test would be more convincing,
    // but performEditorAction("CommentByLineComment") doesn't fire its handler at all in this
    // project's test sandbox even for a built-in file type (.properties) with a well-established
    // commenter, so it can't distinguish a real regression here from an environment limitation.

    fun testLineCommentPrefixIsHash() {
        val commenter = VRLCommenter()
        assertEquals("#", commenter.lineCommentPrefix)
        assertNull(commenter.blockCommentPrefix)
        assertNull(commenter.blockCommentSuffix)
        assertNull(commenter.commentedBlockCommentPrefix)
        assertNull(commenter.commentedBlockCommentSuffix)
    }

    fun testRegisteredForVRLLanguage() {
        assertTrue(LanguageCommenters.INSTANCE.forLanguage(VRL) is VRLCommenter)
    }
}
