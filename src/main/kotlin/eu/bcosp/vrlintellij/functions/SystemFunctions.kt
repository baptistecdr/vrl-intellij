package eu.bcosp.vrlintellij.functions

val systemFunctions = mapOf(
    "get_env_var" to VRLFunction(
        name = "get_env_var",
        description = "Returns the value of the environment variable specified by name.",
        isFallible = true,
        isPure = true,
        arguments = listOf(
            VRLFunctionArgument(
                "name",
                setOf("string"),
                "The name of the environment variable.",
                true
            )
        ),
        returnTypes = setOf("string", "error")
    ),
    "get_hostname" to VRLFunction(
        name = "get_hostname",
        description = "Returns the local system's hostname.",
        isFallible = true,
        isPure = true,
        arguments = listOf(),
        returnTypes = setOf("string", "error")
    ),
    "get_timezone_name" to VRLFunction(
        name = "get_timezone_name",
        description = "Returns the name of the timezone in the Vector configuration (see global configuration options). If the configuration is set to local, then it attempts to determine the name of the timezone from the host OS. If this is not possible, then it returns the fixed offset of the local timezone for the current time in the format \"[+-]HH:MM\", for example, \"+02:00\".",
        isFallible = true,
        isPure = true,
        arguments = listOf(),
        returnTypes = setOf("string", "error")
    )
)