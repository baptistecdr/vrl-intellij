package eu.bcosp.vrlintellij.injection

import org.intellij.lang.regexp.DefaultRegExpPropertiesProvider
import org.intellij.lang.regexp.RegExpLanguageHost
import org.intellij.lang.regexp.psi.RegExpChar
import org.intellij.lang.regexp.psi.RegExpGroup
import org.intellij.lang.regexp.psi.RegExpNamedGroupRef

/**
 * Tells the platform's "Check RegExp" intention and annotator which constructs a VRL `r'...'`
 * literal actually supports, so it flags what `vector vrl` would reject instead of accepting the
 * platform's default do-everything RegExp dialect. Registered for [eu.bcosp.vrlintellij.psi.VRLLiteral]
 * via plugin.xml's `regExpLanguageHost` extension (the same `ClassExtensionPoint` mechanism the
 * bundled Java/Kotlin/Groovy/JS regex hosts use - `RegExpLanguageHosts.findRegExpHost` resolves it
 * from the injection host's class, not from anything [VRLRegexInjector] itself passes along).
 *
 * VRL compiles every regex literal with Rust's `regex` crate (`regex-syntax` 1.12.4, the version
 * vrl/Cargo.lock pins). Every answer below was checked against both `regex-syntax`'s own parser
 * source (`ast/parse.rs`) and `vector vrl`:
 * - Look-behind (`(?<=...)`/`(?<!...)`) - `parse_group`'s `is_lookaround_prefix` check rejects it
 *   with `UnsupportedLookAround`; `vector vrl` confirms. Pure lookahead (`(?=...)`/`(?!...)`) hits
 *   the exact same rejection in Rust, but decompiling `RegExpAnnotator.visitRegExpGroup` shows the
 *   platform only ever consults [supportsLookbehind] - there's no equivalent hook for lookahead,
 *   so that half of VRL's restriction can't be surfaced here; it's a platform gap, not something
 *   this class can close.
 * - Perl5 embedded comments (`(?#...)`) - unrecognized by `parse_group`; `vector vrl` confirms.
 * - Atomic groups (`(?>...)`) - also gated by [supportsPossessiveQuantifiers] (decompiled
 *   alongside it in `visitRegExpGroup`, both being PCRE "no backtracking" features); `regex-syntax`
 *   has no possessive-quantifier or atomic-group concept at all (`a++`/`a*+` just parse as a
 *   redundant second quantifier, and `vector vrl` rejects `(?>...)` as an unrecognized flag).
 * - Named group syntax - `parse_group` recognizes exactly `?P<name>` and `?<name>`
 *   ([RegExpGroup.Type.PYTHON_NAMED_GROUP] / [RegExpGroup.Type.NAMED_GROUP]); the Java/.NET
 *   `(?'name'...)` spelling has no equivalent branch in the Rust grammar.
 * - Named-group backreferences (`\k<name>`) - `regex-syntax` has no backreference construct of any
 *   kind; `vector vrl` confirms with "unrecognized escape sequence". This is the one backreference
 *   form the platform can actually flag through [supportsNamedGroupRefSyntax] - numbered
 *   backreferences (`(a)\1`) are equally unsupported in VRL, but `RegExpAnnotator.visitRegExpBackref`
 *   only ever checks whether they *resolve* to a real group, with no host hook for "this dialect
 *   has no backreferences at all" - another platform gap.
 *
 * Escapes, Unicode properties (`\p{...}`), and character classes are left at the platform's own
 * defaults via [DefaultRegExpPropertiesProvider] - `\xFF`, the braced `\x{...}` and `\u{...}`
 * forms, and `\p{L}`/`\p{Letter}` were all confirmed accepted by `vector vrl`, and Rust's Unicode
 * property names follow the same standard naming the default provider already knows.
 */
object VRLRegExpLanguageHost : RegExpLanguageHost {

    override fun supportsPerl5EmbeddedComments(): Boolean = false

    override fun supportsPossessiveQuantifiers(): Boolean = false

    override fun supportsPythonConditionalRefs(): Boolean = false

    override fun supportsNamedGroupSyntax(group: RegExpGroup): Boolean =
        group.type == RegExpGroup.Type.NAMED_GROUP || group.type == RegExpGroup.Type.PYTHON_NAMED_GROUP

    override fun supportsNamedGroupRefSyntax(ref: RegExpNamedGroupRef): Boolean = false

    override fun supportsExtendedHexCharacter(char: RegExpChar): Boolean = true

    override fun supportsLookbehind(group: RegExpGroup): RegExpLanguageHost.Lookbehind =
        if (group.type == RegExpGroup.Type.POSITIVE_LOOKBEHIND || group.type == RegExpGroup.Type.NEGATIVE_LOOKBEHIND) {
            RegExpLanguageHost.Lookbehind.NOT_SUPPORTED
        } else {
            RegExpLanguageHost.Lookbehind.FULL
        }

    override fun isValidCategory(category: String): Boolean =
        DefaultRegExpPropertiesProvider.getInstance().isValidCategory(category)

    override fun getPropertyDescription(name: String?): String? =
        DefaultRegExpPropertiesProvider.getInstance().getPropertyDescription(name)

    override fun getAllKnownProperties(): Array<Array<String>> =
        DefaultRegExpPropertiesProvider.getInstance().allKnownProperties

    override fun getKnownCharacterClasses(): Array<Array<String>> =
        DefaultRegExpPropertiesProvider.getInstance().knownCharacterClasses
}
