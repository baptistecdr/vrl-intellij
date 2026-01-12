package eu.bcosp.vrlintellij.functions

val debugFunctions = mapOf(
    "assert" to VRLFunction(
        name = "assert",
        description = "Asserts the condition, which must be a Boolean expression. The program is aborted with message if the condition evaluates to false.",
        isFallible = true,
        isPure = false,
        arguments = listOf(
            VRLFunctionArgument(
                "condition",
                setOf("boolean"),
                "The condition to check.",
                true
            ),
            VRLFunctionArgument(
                "message",
                setOf("string"),
                "An optional custom error message. If the equality assertion fails, message is appended to the default message prefix. See the examples below for a fully formed log message sample.",
                false
            )
        ),
        returnTypes = setOf("boolean", "error")
    ),
    "assert_eq" to VRLFunction(
        name = "assert_eq",
        description = "Asserts that two expressions, left and right, have the same value. The program is aborted with message if they do not have the same value.",
        isFallible = false,
        isPure = false,
        arguments = listOf(
            VRLFunctionArgument(
                "left",
                setOf("any"),
                "The value to check for equality against right.",
                true
            ),
            VRLFunctionArgument(
                "right",
                setOf("any"),
                "The value to check for equality against left.",
                true
            ),
            VRLFunctionArgument(
                "message",
                setOf("string"),
                "An optional custom error message. If the equality assertion fails, message is appended to the default message prefix. See the examples below for a fully formed log message sample.",
                false
            )
        ),
        returnTypes = setOf("boolean")
    ),
    "log" to VRLFunction(
        name = "log",
        description = "Logs the value to stdout at the specified level.",
        isFallible = false,
        isPure = false,
        arguments = listOf(
            VRLFunctionArgument(
                "value",
                setOf("any"),
                "The value to log.",
                true
            ),
            VRLFunctionArgument(
                "level",
                setOf("string"),
                "The log level.",
                false,
                "info"
            ),
            VRLFunctionArgument(
                "rate_limit_secs",
                setOf("integer"),
                "Specifies that the log message is output no more than once per the given number of seconds. Use a value of 0 to turn rate limiting off.",
                true
            )
        ),
        returnTypes = setOf("null")
    )
)