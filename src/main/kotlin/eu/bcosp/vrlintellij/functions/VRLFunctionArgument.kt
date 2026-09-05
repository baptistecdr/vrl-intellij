package eu.bcosp.vrlintellij.functions

data class VRLFunctionArgument(
    val name: String,
    val types: Set<String>,
    val description: String,
    val isRequired: Boolean,
    val defaultValue: Any? = null,
    // Fixed string values this argument actually accepts (e.g. encode_base64's charset: "standard"
    // or "url_safe") - sourced from the `.enum_variants(...)` VRL's own stdlib validates against,
    // not from vector.dev's docs (which rarely spell these out in prose). Empty for every argument
    // that isn't a closed string enum. See scripts/refresh-vrl-functions.mjs.
    val enumValues: List<String> = emptyList(),
)
