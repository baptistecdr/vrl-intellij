package eu.bcosp.vrlintellij.functions

val coerceFunctions = mapOf(
    "to_bool" to VRLFunction(
        name = "to_bool",
        description = "Coerces the value into a boolean.",
        isFallible = true,
        isPure = true,
        arguments = listOf(
            VRLFunctionArgument(
                "value",
                setOf("boolean", "integer", "float", "null", "string"),
                "The value to convert to a Boolean.",
                true
            )
        ),
        returnTypes = setOf("boolean", "error"),
    ),
    "to_float" to VRLFunction(
        name = "to_float",
        description = "Coerces the value into a float.",
        isFallible = true,
        isPure = true,
        arguments = listOf(
            VRLFunctionArgument(
                "value",
                setOf("integer", "float", "boolean", "string", "timestamp"),
                "The value to convert to a float. Must be convertible to a float, otherwise an error is raised.",
                true
            )
        ),
        returnTypes = setOf("float", "error"),
    ),
    "to_int" to VRLFunction(
        name = "to_int",
        description = "Coerces the value into an integer.",
        isFallible = true,
        isPure = true,
        arguments = listOf(
            VRLFunctionArgument(
                "value",
                setOf("integer", "float", "boolean", "string", "timestamp", "null"),
                "The value to convert to an integer.",
                true
            )
        ),
        returnTypes = setOf("float", "error"),
    ),
    "to_regex" to VRLFunction(
        name = "to_regex",
        description = "Coerces the value into a regex.",
        isFallible = true,
        isPure = true,
        arguments = listOf(
            VRLFunctionArgument(
                "value",
                setOf("string"),
                "The value to convert to a regex.",
                true
            )
        ),
        returnTypes = setOf("regex", "error"),
    ),
    "to_string" to VRLFunction(
        name = "to_string",
        description = "Coerces the value into a string.",
        isFallible = true,
        isPure = true,
        arguments = listOf(
            VRLFunctionArgument(
                "value",
                setOf("integer", "float", "boolean", "string", "timestamp", "null"),
                "The value to convert to a string.",
                true
            )
        ),
        returnTypes = setOf("string", "error"),
    ),
)