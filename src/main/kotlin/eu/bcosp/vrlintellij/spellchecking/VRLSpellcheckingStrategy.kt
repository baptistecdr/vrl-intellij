package eu.bcosp.vrlintellij.spellchecking

import com.intellij.psi.PsiElement
import com.intellij.spellchecker.tokenizer.SpellcheckingStrategy
import com.intellij.spellchecker.tokenizer.Tokenizer
import eu.bcosp.vrlintellij.psi.VRLElementTypes

/**
 * Spellchecks the natural-language content of a `.vrl` file - string literals and comments -
 * without touching identifiers, keywords, or other code tokens, which would just be noisy false
 * positives (VRL function/variable names aren't English words).
 */
class VRLSpellcheckingStrategy : SpellcheckingStrategy() {

    override fun getTokenizer(element: PsiElement): Tokenizer<*> {
        val type = element.node?.elementType
        return when (type) {
            VRLElementTypes.STRING, VRLElementTypes.RAW_STRING, VRLElementTypes.COMMENT -> TEXT_TOKENIZER
            else -> super.getTokenizer(element)
        }
    }
}
