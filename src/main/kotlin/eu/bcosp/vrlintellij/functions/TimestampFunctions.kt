package eu.bcosp.vrlintellij.functions

val timestampFunctions = mapOf(
    "format_timestamp" to VRLFunction(
        name = "format_timestamp",
        description = "Formats value into a string representation of the timestamp.",
        isFallible = false,
        isPure = true,
        arguments = listOf(
            VRLFunctionArgument(
                "value",
                setOf("timestamp"),
                "The timestamp to format as text.",
                true
            ),
            VRLFunctionArgument(
                "format",
                setOf("string"),
                "The format string as described by the Chrono library.",
                true
            ),
            VRLFunctionArgument(
                "timezone",
                setOf("string"),
                "The timezone to use when formatting the timestamp. The parameter uses the TZ identifier or local.",
                false
            )
        ),
        returnTypes = setOf("string")
    ),
    "now" to VRLFunction(
        name = "now",
        description = "Returns the current timestamp in the UTC timezone with nanosecond precision.",
        isFallible = false,
        isPure = true,
        arguments = listOf(),
        returnTypes = setOf("timestamp")
    )
)