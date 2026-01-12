package eu.bcosp.vrlintellij.functions

val objectFunctions = mapOf(
    "match_datadog_query" to VRLFunction(
        name = "match_datadog_query",
        description = "Matches a Datadog query against a log event.",
        isFallible = false,
        isPure = true,
        arguments = listOf(
            VRLFunctionArgument(
                "value",
                setOf("string"),
                "The object.",
                true
            ),
            VRLFunctionArgument(
                "query",
                setOf("string"),
                "The Datadog Search Syntax query.",
                true
            )
        ),
        returnTypes = setOf("boolean")
    ),
    "merge" to VRLFunction(
        name = "merge",
        description = "Merges the from object into the to object.",
        isFallible = false,
        isPure = true,
        arguments = listOf(
            VRLFunctionArgument(
                "to",
                setOf("object"),
                "The object to merge into.",
                true
            ),
            VRLFunctionArgument(
                "from",
                setOf("object"),
                "The object to merge from.",
                true
            ),
            VRLFunctionArgument(
                "deep",
                setOf("boolean"),
                "A deep merge is performed if true, otherwise only top-level fields are merged.",
                false,
            )
        ),
        returnTypes = setOf("object")
    ),
    "object_from_array" to VRLFunction(
        name = "object_from_array",
        description = "Iterate over either one array of arrays or a pair of arrays and create an object out of all the key-value pairs contained in them. With one array of arrays, any entries with no value use null instead. Any keys that are null skip the corresponding value.",
        isFallible = true,
        isPure = true,
        arguments = listOf(
            VRLFunctionArgument(
                "values",
                setOf("array"),
                "The first array of elements, or the array of input arrays if no other parameter is present.",
                true
            ),
            VRLFunctionArgument(
                "keys",
                setOf("array"),
                "The second array of elements. If not present, the first parameter must contain all the arrays.",
                false,
            )
        ),
        returnTypes = setOf("object", "error")
    ),
    "unnest" to VRLFunction(
        name = "unnest",
        description = "Unnest an array field from an object to create an array of objects using that field; keeping all other fields.\n" +
                "Assigning the array result of this to . results in multiple events being emitted from remap. See the remap transform docs for more details.\n" +
                "This is also referred to as explode in some language",
        isFallible = true,
        isPure = true,
        arguments = listOf(
            VRLFunctionArgument(
                "path",
                setOf("path"),
                "The path of the field to unnest.",
                true
            )
        ),
        returnTypes = setOf("array", "error")
    )
)