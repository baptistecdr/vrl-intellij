package eu.bcosp.vrlintellij.psi

import com.intellij.psi.tree.IElementType
import eu.bcosp.vrlintellij.VRL

class VRLTokenType(debugName: String) : IElementType(debugName, VRL)
class VRLCompositeType(debugName: String) : IElementType(debugName, VRL)

// Grammar-Kit doesn't emit a VRLElementTypes constant for a token whose declared pattern matches
// only whitespace characters (same as WHITE_SPACE itself) even though it's declared in VRL.bnf's
// `tokens=[...]` block - hence declaring it by hand here instead, the same way TokenType.WHITE_SPACE
// itself is a hand-written platform constant rather than a generated one.
@JvmField
val NEWLINE: IElementType = VRLTokenType("NEWLINE")
