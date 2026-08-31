package eu.bcosp.vrlintellij.injection

import com.intellij.lang.injection.MultiHostInjector
import com.intellij.lang.injection.MultiHostRegistrar
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiLanguageInjectionHost
import eu.bcosp.vrlintellij.psi.VRLElementTypes
import eu.bcosp.vrlintellij.psi.VRLLiteral
import org.intellij.lang.regexp.RegExpLanguage

/**
 * Automatically injects the platform's bundled RegExp language into every VRL regex literal
 * (`r'...'`). Unlike the manual `Alt+Enter -> Inject language` support [VRLLiteralMixin] enables
 * for string-shaped literals generally, this always applies to regex literals specifically - it's
 * what turns on regex syntax highlighting inside the literal and the "Check RegExp" intention with
 * no user action needed, the same way it works for regex literals in most other IntelliJ languages.
 *
 * Reuses [VRLLiteralTextEscaperBase]'s delimiter-stripping (via [PsiLanguageInjectionHost.createLiteralTextEscaper])
 * for the injected range rather than [com.intellij.psi.ElementManipulators], since no
 * `ElementManipulator` is registered for [VRLLiteral] - see [eu.bcosp.vrlintellij.references.VRLVariableReference]'s
 * doc comment for why a rename handler had to route around the same gap.
 */
class VRLRegexInjector : MultiHostInjector {

    override fun getLanguagesToInject(registrar: MultiHostRegistrar, context: PsiElement) {
        if (context !is VRLLiteral) return
        val host = context as? PsiLanguageInjectionHost ?: return
        if (context.firstChild?.node?.elementType != VRLElementTypes.REGEX) return
        if (!host.isValidHost) return

        val range = host.createLiteralTextEscaper().relevantTextRange
        registrar.startInjecting(RegExpLanguage.INSTANCE)
            .addPlace(null, null, host, range)
            .doneInjecting()
    }

    override fun elementsToInjectIn(): List<Class<out PsiElement>> = listOf(VRLLiteral::class.java)
}
