@file:JvmName("VRLTemplateElementTypes")

package eu.bcosp.vrlintellij.highlighting

import eu.bcosp.vrlintellij.psi.VRLTokenType

@JvmField
val TEMPLATE_START: VRLTokenType = VRLTokenType("TEMPLATE_START")

@JvmField
val TEMPLATE_END: VRLTokenType = VRLTokenType("TEMPLATE_END")

@JvmField
val TEMPLATE_VARIABLE: VRLTokenType = VRLTokenType("TEMPLATE_VARIABLE")
