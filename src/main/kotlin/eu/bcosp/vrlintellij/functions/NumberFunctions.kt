package eu.bcosp.vrlintellij.functions

val numberFunctions = mapOf(
    "abs" to VRLFunction(
        name = "abs",
        description = "Computes the absolute value of value.",
        isFallible = false,
        isPure = true,
        arguments = listOf(
            VRLFunctionArgument(
                "value",
                setOf("integer", "float"),
                "The number to calculate the absolute value.",
                true
            )
        ),
        returnTypes = setOf("integer", "float")
    ),
    "ceil" to VRLFunction(
        name = "ceil",
        description = "Rounds the value up to the specified precision.",
        isFallible = false,
        isPure = true,
        arguments = listOf(
            VRLFunctionArgument(
                "value",
                setOf("integer", "float"),
                "The number to round up.",
                true
            ),
            VRLFunctionArgument(
                "precision",
                setOf("integer"),
                "The number of decimal places to round to.",
                false,
            )
        ),
        returnTypes = setOf("integer", "float")
    ),
    "floor" to VRLFunction(
        name = "floor",
        description = "Rounds the value down to the specified precision.",
        isFallible = false,
        isPure = true,
        arguments = listOf(
            VRLFunctionArgument(
                "value",
                setOf("integer", "float"),
                "The number to round down.",
                true
            ),
            VRLFunctionArgument(
                "precision",
                setOf("integer"),
                "The number of decimal places to round to.",
                false,
            )
        ),
        returnTypes = setOf("integer", "float")
    ),
    "format_int" to VRLFunction(
        name = "format_int",
        description = "Formats the integer value into a string representation using the given base/radix.",
        isFallible = false,
        isPure = true,
        arguments = listOf(
            VRLFunctionArgument(
                "value",
                setOf("integer"),
                "The number to format.",
                true
            ),
            VRLFunctionArgument(
                "base",
                setOf("integer"),
                "The base to format the number in. Must be between 2 and 36 (inclusive).",
                false,
                defaultValue = "10"
            )
        ),
        returnTypes = setOf("string")
    ),
    "format_number" to VRLFunction(
        name = "format_number",
        description = "Formats the value into a string representation of the number.",
        isFallible = false,
        isPure = true,
        arguments = listOf(
            VRLFunctionArgument(
                "value",
                setOf("integer", "float"),
                "The number to format as a string.",
                true
            ),
            VRLFunctionArgument(
                "scale",
                setOf("integer"),
                "The number of decimal places to display.",
                false,
                defaultValue = "2"
            ),
            VRLFunctionArgument(
                "decimal_separator",
                setOf("string"),
                "The character to use between the whole and decimal parts of the number.",
                false,
                defaultValue = "."
            ),
            VRLFunctionArgument(
                "grouping_separator",
                setOf("string"),
                "The character to use between each thousands part of the number.",
                false,
                defaultValue = ","
            )
        ),
        returnTypes = setOf("string")
    ),
    "mod" to VRLFunction(
        name = "mod",
        description = "Calculates the remainder of value divided by modulus.",
        isFallible = true,
        isPure = true,
        arguments = listOf(
            VRLFunctionArgument(
                "value",
                setOf("integer", "float"),
                "The value the modulus is applied to.",
                true
            ),
            VRLFunctionArgument(
                "modulus",
                setOf("integer", "float"),
                "The modulus value.",
                true
            ),
        ),
        returnTypes = setOf("integer", "float", "error")
    ),
    "round" to VRLFunction(
        name = "round",
        description = "Rounds the value to the specified precision.",
        isFallible = false,
        isPure = true,
        arguments = listOf(
            VRLFunctionArgument(
                "value",
                setOf("integer", "float"),
                "The number to round.",
                true
            ),
            VRLFunctionArgument(
                "precision",
                setOf("integer"),
                "The number of decimal places to round to.",
                false,
            )
        ),
        returnTypes = setOf("integer", "float")
    )
)