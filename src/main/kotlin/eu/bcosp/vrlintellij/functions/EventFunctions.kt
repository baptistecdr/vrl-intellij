package eu.bcosp.vrlintellij.functions

val eventFunctions = mapOf(
    "get_secret" to VRLFunction(
        name = "get_secret",
        description = "Returns the value of the given secret from an event.",
        isFallible = false,
        isPure = true,
        arguments = listOf(
            VRLFunctionArgument(
                "key",
                setOf("string"),
                "The name of the secret.",
                true
            )
        ),
        returnTypes = setOf("string")
    ),
    "remove_secret" to VRLFunction(
        name = "remove_secret",
        description = "Removes a secret from an event.",
        isFallible = false,
        isPure = true,
        arguments = listOf(
            VRLFunctionArgument(
                "key",
                setOf("string"),
                "The name of the secret to remove.",
                true
            )
        ),
        returnTypes = setOf("null")
    ),
    "set_secret" to VRLFunction(
        name = "set_secret",
        description = "Sets the given secret in the event.",
        isFallible = false,
        isPure = true,
        arguments = listOf(
            VRLFunctionArgument(
                "key",
                setOf("string"),
                "The name of the secret.",
                true
            ),
            VRLFunctionArgument(
                "secret",
                setOf("string"),
                "The secret value.",
                true
            )
        ),
        returnTypes = setOf("null")
    ),
    "set_semantic_meaning" to VRLFunction(
        name = "set_semantic_meaning",
        description = "Sets a semantic meaning for an event. ",
        isFallible = false,
        isPure = true,
        arguments = listOf(
            VRLFunctionArgument(
                "target",
                setOf("path"),
                "The path of the value that is assigned a meaning.",
                true
            ),
            VRLFunctionArgument(
                "meaning",
                setOf("string"),
                "The name of the meaning to assign.",
                true
            )
        ),
        returnTypes = setOf("null")
    )
)