package eu.bcosp.vrlintellij.functions

val convertFunctions = mapOf(
    "from_unix_timestamp" to VRLFunction(
        name = "from_unix_timestamp",
        description = "Converts the value integer from a Unix timestamp to a VRL timestamp.\nConverts from the number of seconds since the Unix epoch by default. To convert from milliseconds or nanoseconds, set the unit argument to milliseconds or nanoseconds.",
        isFallible = false,
        isPure = true,
        arguments = listOf(
            VRLFunctionArgument(
                "value",
                setOf("integer"),
                "The Unix timestamp to convert.",
                true
            ),
            VRLFunctionArgument(
                "unit",
                setOf("string"),
                "The time unit.",
                false,
                defaultValue = "seconds"
            )
        ),
        returnTypes = setOf("timestamp"),
    ),
    "to_syslog_facility" to VRLFunction(
        name = "to_syslog_facility",
        description = "Converts the value, a Syslog facility code, into its corresponding Syslog keyword. For example, 0 into \"kern\", 1 into \"user\", etc.",
        isFallible = true,
        isPure = true,
        arguments = listOf(
            VRLFunctionArgument(
                "value",
                setOf("integer"),
                "The facility code.",
                true
            )
        ),
        returnTypes = setOf("string", "error")
    ),
    "to_syslog_facility_code" to VRLFunction(
        name = "to_syslog_facility_code",
        description = "Converts the value, a Syslog facility keyword, into a Syslog integer facility code (0 to 23).",
        isFallible = true,
        isPure = true,
        arguments = listOf(
            VRLFunctionArgument(
                "value",
                setOf("string"),
                "The Syslog facility keyword to convert.",
                true
            )
        ),
        returnTypes = setOf("integer", "error")
    ),
    "to_syslog_level" to VRLFunction(
        name = "to_syslog_level",
        description = "Converts the value, a Syslog severity level, into its corresponding keyword, i.e. 0 into \"emerg\", 1 into \"alert\", etc.",
        isFallible = true,
        isPure = true,
        arguments = listOf(
            VRLFunctionArgument(
                "value",
                setOf("integer"),
                "The severity level.",
                true
            )
        ),
        returnTypes = setOf("string", "error")
    ),
    "to_syslog_severity" to VRLFunction(
        name = "to_syslog_severity",
        description = "Converts the value, a Syslog log level keyword, into a Syslog integer severity level (0 to 7).",
        isFallible = true,
        isPure = true,
        arguments = listOf(
            VRLFunctionArgument(
                "value",
                setOf("string"),
                "The Syslog level keyword to convert.",
                true
            )
        ),
        returnTypes = setOf("integer", "error")
    ),
    "to_unix_timestamp" to VRLFunction(
        name = "to_unix_timestamp",
        description = "Converts the value timestamp into a Unix timestamp.\nReturns the number of seconds since the Unix epoch by default. To return the number in milliseconds or nanoseconds, set the unit argument to milliseconds or nanoseconds.",
        isFallible = true,
        isPure = true,
        arguments = listOf(
            VRLFunctionArgument(
                "value",
                setOf("timestamp"),
                "The timestamp to convert into a Unix timestamp.",
                true
            ),
            VRLFunctionArgument(
                "unit",
                setOf("string"),
                "The time unit.",
                false,
                defaultValue = "seconds"
            )
        ),
        returnTypes = setOf("integer", "error"),
    )
)