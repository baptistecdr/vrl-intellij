package eu.bcosp.vrlintellij.functions

data class VRLFunctionArgument(
    val name: String,
    val types: Set<String>,
    val description: String,
    val isRequired: Boolean,
    val defaultValue: Any? = null,
)
