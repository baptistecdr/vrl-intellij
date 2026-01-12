package eu.bcosp.vrlintellij.functions

val enumerateFunctions = mapOf(
    "compact" to VRLFunction(
        name = "compact",
        description = "Compacts the value by removing empty values, where empty values are defined using the available parameters.",
        isFallible = false,
        isPure = true,
        arguments = listOf(
            VRLFunctionArgument(
                "value",
                setOf("array", "object"),
                "The object or array to compact.",
                true,
            ),
            VRLFunctionArgument(
                "recursive",
                setOf("boolean"),
                "Whether the compaction be recursive.",
                isRequired = false,
                defaultValue = true
            ),
            VRLFunctionArgument(
                "null",
                setOf("boolean"),
                "Whether null should be treated as an empty value.",
                isRequired = false,
                defaultValue = true
            ),
            VRLFunctionArgument(
                "string",
                setOf("boolean"),
                "Whether an empty string should be treated as an empty value.",
                isRequired = false,
                defaultValue = true
            ),
            VRLFunctionArgument(
                "object",
                setOf("boolean"),
                "Whether an empty object should be treated as an empty value.",
                isRequired = false,
                defaultValue = true
            ),
            VRLFunctionArgument(
                "array",
                setOf("boolean"),
                "Whether an empty array should be treated as an empty value.",
                isRequired = false,
                defaultValue = true
            ),
            VRLFunctionArgument(
                "nullish",
                setOf("boolean"),
                "Tests whether the value is “nullish” as defined by the is_nullish function.",
                isRequired = false,
            ),
        ),
        returnTypes = setOf("array", "object")
    ),
    "filter" to VRLFunction(
        name = "filter",
        description = "Filter elements from a collection.\nThis function currently does not support recursive iteration.",
        isFallible = false,
        isPure = true,
        arguments = listOf(
            VRLFunctionArgument(
                "value",
                setOf("array", "object"),
                "The array or object to filter.",
                true,
            )
        ),
        returnTypes = setOf("array", "object")
    ),
    "flatten" to VRLFunction(
        name = "flatten",
        description = "Flattens the value into a single-level representation.",
        isFallible = false,
        isPure = true,
        arguments = listOf(
            VRLFunctionArgument(
                "value",
                setOf("array", "object"),
                "The array or object to flatten.",
                isRequired = true,
            ),
            VRLFunctionArgument(
                "separator",
                setOf("string"),
                "The separator to join nested keys.",
                isRequired = false,
                defaultValue = "."
            )
        ),
        returnTypes = setOf("array", "object")
    ),
    "for_each" to VRLFunction(
        name = "for_each",
        description = "Iterates over a collection.",
        isFallible = false,
        isPure = true,
        arguments = listOf(
            VRLFunctionArgument(
                "value",
                setOf("array", "object"),
                "The array or object to iterate.",
                isRequired = true,
            )
        ),
        returnTypes = setOf("null")
    ),
    "includes" to VRLFunction(
        name = "includes",
        description = "Determines whether the value array includes the specified item.",
        isFallible = false,
        isPure = true,
        arguments = listOf(
            VRLFunctionArgument(
                "value",
                setOf("array"),
                "The array.",
                isRequired = true,
            ),
            VRLFunctionArgument(
                "item",
                setOf("any"),
                "The item to check.",
                isRequired = true,
            )
        ),
        returnTypes = setOf("boolean")
    ),
    "keys" to VRLFunction(
        name = "keys",
        description = "Returns the keys from the object passed into the function.",
        isFallible = false,
        isPure = true,
        arguments = listOf(
            VRLFunctionArgument("value", setOf("object"), "The object to extract keys from.", true)
        ),
        returnTypes = setOf("array")
    ),
    "length" to VRLFunction(
        name = "length",
        description = "Returns the length of the value.\nIf value is an array, returns the number of elements.\nIf value is an object, returns the number of top-level keys.\nIf value is a string, returns the number of bytes in the string. If you want the number of characters, see strlen.",
        isFallible = false,
        isPure = true,
        arguments = listOf(
            VRLFunctionArgument("value", setOf("array", "object", "string"), "The array or object.", true)
        ),
        returnTypes = setOf("integer"),
    ),
    "map_keys" to VRLFunction(
        name = "map_keys",
        description = "Map the keys within an object.",
        isFallible = false,
        isPure = true,
        arguments = listOf(
            VRLFunctionArgument("value", setOf("object"), "The object to iterate.", true),
            VRLFunctionArgument("recursive", setOf("bollean"), "Whether to recursively iterate the collection.", false),
        ),
        returnTypes = setOf("object")
    ),
    "map_values" to VRLFunction(
        name = "map_values",
        description = "Map the values within a collection.",
        isFallible = false,
        isPure = true,
        arguments = listOf(
            VRLFunctionArgument("value", setOf("object"), "The object or array to iterate.", true),
            VRLFunctionArgument("recursive", setOf("boolean"), "Whether to recursively iterate the collection.", false),
        ),
        returnTypes = setOf("array", "object")
    ),
    "match_array" to VRLFunction(
        name = "match_array",
        description = "Determines whether the elements in the value array matches the pattern. By default, it checks that at least one element matches, but can be set to determine if all the elements match.",
        isFallible = false,
        isPure = true,
        arguments = listOf(
            VRLFunctionArgument("value", setOf("array"), "The array.", true),
            VRLFunctionArgument("pattern", setOf("regex"), "The regular expression pattern to match against.", true),
            VRLFunctionArgument("all", setOf("boolean"), "Whether to match on all elements of value.", false),
        ),
        returnTypes = setOf("boolean")
    ),
    "strlen" to VRLFunction(
        name = "strlen",
        description = "Returns the number of UTF-8 characters in value. This differs from length which counts the number of bytes of a string.\nNote: This is the count of Unicode scalar values which can sometimes differ from Unicode code points.",
        isFallible = false,
        isPure = true,
        arguments = listOf(
            VRLFunctionArgument("value", setOf("string"), "The string.", true),
        ),
        returnTypes = setOf("integer")
    ),
    "unflatten" to VRLFunction(
        name = "unflatten",
        description = "Unflattens the value into a nested representation.",
        isFallible = false,
        isPure = true,
        arguments = listOf(
            VRLFunctionArgument("value", setOf("object"), "The array or object to unflatten.", true),
            VRLFunctionArgument("separator", setOf("string"), "The separator to split flattened keys.", false, "."),
            VRLFunctionArgument(
                "recursive",
                setOf("boolean"),
                "Whether to recursively unflatten the object values.",
                isRequired = false,
                defaultValue = true
            ),
        ),
        returnTypes = setOf("object")
    ),
    "unique" to VRLFunction(
        name = "unique",
        description = "Returns the unique values for an array.\nThe first occurrence of each element is kept.",
        isFallible = false,
        isPure = true,
        arguments = listOf(
            VRLFunctionArgument("value", setOf("array"), "The array to return unique elements from.", true),
        ),
        returnTypes = setOf("array")
    ),
    "values" to VRLFunction(
        name = "values",
        description = "Returns the values from the object passed into the function.",
        isFallible = false,
        isPure = true,
        arguments = listOf(
            VRLFunctionArgument("value", setOf("object"), "The object to extract values from.", true)
        ),
        returnTypes = setOf("array")
    )
)