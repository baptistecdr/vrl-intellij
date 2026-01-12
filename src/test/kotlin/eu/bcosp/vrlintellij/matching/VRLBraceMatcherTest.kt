package eu.bcosp.vrlintellij.matching

import eu.bcosp.vrlintellij.psi.VRLElementTypes
import junit.framework.TestCase

class VRLBraceMatcherTest : TestCase() {

    fun testDeclaresAllThreeBracePairs() {
        val pairs = VRLBraceMatcher().pairs.associate { it.leftBraceType to it.rightBraceType }
        assertEquals(
            mapOf(
                VRLElementTypes.LPAREN to VRLElementTypes.RPAREN,
                VRLElementTypes.LBRACE to VRLElementTypes.RBRACE,
                VRLElementTypes.LBRACKET to VRLElementTypes.RBRACKET,
            ),
            pairs
        )
    }

    fun testBracesPairIsStructural() {
        val bracePair = VRLBraceMatcher().pairs.first { it.leftBraceType == VRLElementTypes.LBRACE }
        assertTrue(bracePair.isStructural)
    }

    fun testParenthesesPairIsNotStructural() {
        val parenPair = VRLBraceMatcher().pairs.first { it.leftBraceType == VRLElementTypes.LPAREN }
        assertFalse(parenPair.isStructural)
    }
}
