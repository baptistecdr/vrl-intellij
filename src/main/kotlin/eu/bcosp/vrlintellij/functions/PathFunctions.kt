package eu.bcosp.vrlintellij.functions

val pathFunctions = mapOf(
    "del" to VRLFunction(
        name = "del",
        description = "Removes the field specified by the static path from the target.",
        isFallible = false,
        isPure = false,
        arguments = listOf(
            VRLFunctionArgument(
                "path",
                setOf("path"),
                "The path of the field to delete.",
                true
            ),
            VRLFunctionArgument(
                "compact",
                setOf("boolean"),
                "After deletion, if compact is true and there is an empty object or array left, the empty object or array is also removed, cascading up to the root. This only applies to the path being deleted, and any parent paths.",
                false,
            )
        ),
        returnTypes = setOf("any", "null")
    ),
    "exists" to VRLFunction(
        name = "exists",
        description = "Checks whether the path exists for the target.",
        isFallible = false,
        isPure = true,
        arguments = listOf(
            VRLFunctionArgument(
                "path",
                setOf("path"),
                "The path of the field to check.",
                true
            )
        ),
        returnTypes = setOf("boolean")
    ),
    "get" to VRLFunction(
        name = "get",
        description = "Dynamically get the value of a given path.",
        isFallible = true,
        isPure = true,
        arguments = listOf(
            VRLFunctionArgument("value", setOf("object", "array"), "The object or array to query.", true),
            VRLFunctionArgument("path", setOf("array"), "An array of path segments to look for the value.", true),
        ),
        returnTypes = setOf("any", "error")
    ),
    "remove" to VRLFunction(
        name = "remove",
        description = "Dynamically remove the value for a given path.",
        isFallible = true,
        isPure = true,
        arguments = listOf(
            VRLFunctionArgument(
                "value",
                setOf("object", "array"),
                "The object or array to remove data from.",
                true
            ),
            VRLFunctionArgument("path", setOf("array"), "An array of path segments to remove the value from.", true),
            VRLFunctionArgument(
                "compact",
                setOf("boolean"),
                "After deletion, if compact is true, any empty objects or arrays left are also removed.",
                false,
            )
        ),
        returnTypes = setOf("object", "array", "error")
    ),
    "set" to VRLFunction(
        name = "set",
        description = "Dynamically insert data into the path of a given object or array.",
        isFallible = true,
        isPure = true,
        arguments = listOf(
            VRLFunctionArgument(
                "value",
                setOf("object", "array"),
                "The object or array to insert data into.",
                true
            ),
            VRLFunctionArgument("path", setOf("array"), "An array of path segments to insert the value into.", true),
            VRLFunctionArgument("data", setOf("any"), "The data to be inserted.", true),
        ),
        returnTypes = setOf("object", "array", "error")
    )
)