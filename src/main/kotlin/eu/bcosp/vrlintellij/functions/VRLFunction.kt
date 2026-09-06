package eu.bcosp.vrlintellij.functions

data class VRLFunction(
    val name: String,
    val description: String,
    val isFallible: Boolean,
    val isPure: Boolean,
    val arguments: List<VRLFunctionArgument>,
    val returnTypes: Set<String>,
)
