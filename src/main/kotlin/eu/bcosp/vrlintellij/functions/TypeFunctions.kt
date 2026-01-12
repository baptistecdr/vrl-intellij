package eu.bcosp.vrlintellij.functions

val typeFunctions = mapOf(
    "array" to VRLFunction(
        name = "array",
        description = "Returns value if it is an array, otherwise returns an error. This enables the type checker to guarantee that the returned value is an array and can be used in any function that expects an array.",
        isFallible = true,
        isPure = true,
        arguments = listOf(
            VRLFunctionArgument(
                "value",
                setOf("any"),
                "The value to check if it is an array.",
                true
            )
        ),
        returnTypes = setOf("array", "error")
    ),
    "bool" to VRLFunction(
        name = "bool",
        description = "Returns value if it is a Boolean, otherwise returns an error. This enables the type checker to guarantee that the returned value is a Boolean and can be used in any function that expects a Boolean.",
        isFallible = true,
        isPure = true,
        arguments = listOf(
            VRLFunctionArgument(
                "value",
                setOf("any"),
                "The value to check if it is a Boolean.",
                true
            )
        ),
        returnTypes = setOf("boolean", "error")
    ),
    "float" to VRLFunction(
        name = "float",
        description = "Returns value if it is a float, otherwise returns an error. This enables the type checker to guarantee that the returned value is a float and can be used in any function that expects a float.",
        isFallible = true,
        isPure = true,
        arguments = listOf(
            VRLFunctionArgument(
                "value",
                setOf("any"),
                "The value to check if it is a float.",
                true
            )
        ),
        returnTypes = setOf("float", "error")
    ),
    "int" to VRLFunction(
        name = "int",
        description = "Returns value if it is an integer, otherwise returns an error. This enables the type checker to guarantee that the returned value is an integer and can be used in any function that expects an integer.",
        isFallible = true,
        isPure = true,
        arguments = listOf(
            VRLFunctionArgument(
                "value",
                setOf("any"),
                "The value to check if it is an integer.",
                true
            )
        ),
        returnTypes = setOf("integer", "error")
    ),
    "is_array" to VRLFunction(
        name = "is_array",
        description = "Check if the value's type is an array.",
        isFallible = false,
        isPure = true,
        arguments = listOf(
            VRLFunctionArgument(
                "value",
                setOf("any"),
                "The value to check if it is an array.",
                true
            )
        ),
        returnTypes = setOf("boolean")
    ),
    "is_boolean" to VRLFunction(
        name = "is_boolean",
        description = "Check if the value's type is a boolean.",
        isFallible = false,
        isPure = true,
        arguments = listOf(
            VRLFunctionArgument(
                "value",
                setOf("any"),
                "The value to check if it is a Boolean.",
                true
            )
        ),
        returnTypes = setOf("boolean")
    ),
    "is_empty" to VRLFunction(
        name = "is_empty",
        description = "Check if the object, array, or string has a length of 0.",
        isFallible = false,
        isPure = true,
        arguments = listOf(
            VRLFunctionArgument(
                "value",
                setOf("object", "array", "string"),
                "The value to check.",
                true
            )
        ),
        returnTypes = setOf("boolean")
    ),
    "is_float" to VRLFunction(
        name = "is_float",
        description = "Check if the value's type is a float.",
        isFallible = false,
        isPure = true,
        arguments = listOf(
            VRLFunctionArgument(
                "value",
                setOf("any"),
                "The value to check if it is a float.",
                true
            )
        ),
        returnTypes = setOf("boolean")
    ),
    "is_integer" to VRLFunction(
        name = "is_integer",
        description = "Check if the value's type is an integer.",
        isFallible = false,
        isPure = true,
        arguments = listOf(
            VRLFunctionArgument(
                "value",
                setOf("any"),
                "The value to check if it is an integer.",
                true
            )
        ),
        returnTypes = setOf("boolean")
    ),
    "is_json" to VRLFunction(
        name = "is_json",
        description = "Check if the string is a valid JSON document.",
        isFallible = false,
        isPure = true,
        arguments = listOf(
            VRLFunctionArgument(
                "value",
                setOf("string"),
                "The value to check if it is a valid JSON document.",
                true
            ),
            VRLFunctionArgument(
                "variant",
                setOf("string"),
                "The variant of the JSON type to explicitly check for.",
                false
            )
        ),
        returnTypes = setOf("boolean")
    ),
    "is_null" to VRLFunction(
        name = "is_null",
        description = "Check if value's type is null. For a more relaxed function, see is_nullish.",
        isFallible = false,
        isPure = true,
        arguments = listOf(
            VRLFunctionArgument(
                "value",
                setOf("any"),
                "The value to check if it is null.",
                true
            )
        ),
        returnTypes = setOf("boolean")
    ),
    "is_nullish" to VRLFunction(
        name = "is_nullish",
        description = "Determines whether value is nullish. Returns true if the specified value is null, an empty string, a string containing only whitespace, or the string \"-\". Returns false otherwise.",
        isFallible = false,
        isPure = true,
        arguments = listOf(
            VRLFunctionArgument(
                "value",
                setOf("any"),
                "The value to check for nullishness, for example, a useless value.",
                true
            )
        ),
        returnTypes = setOf("boolean")
    ),
    "is_object" to VRLFunction(
        name = "is_object",
        description = "Check if value's type is an object.",
        isFallible = false,
        isPure = true,
        arguments = listOf(
            VRLFunctionArgument(
                "value",
                setOf("any"),
                "The value to check if it is an object.",
                true
            )
        ),
        returnTypes = setOf("boolean")
    ),
    "is_regex" to VRLFunction(
        name = "is_regex",
        description = "Check if value's type is a regex.",
        isFallible = false,
        isPure = true,
        arguments = listOf(
            VRLFunctionArgument(
                "value",
                setOf("any"),
                "The value to check if it is a regex.",
                true
            )
        ),
        returnTypes = setOf("boolean")
    ),
    "is_string" to VRLFunction(
        name = "is_string",
        description = "Check if value's type is a string.",
        isFallible = false,
        isPure = true,
        arguments = listOf(
            VRLFunctionArgument(
                "value",
                setOf("any"),
                "The value to check if it is a string.",
                true
            )
        ),
        returnTypes = setOf("boolean")
    ),
    "is_timestamp" to VRLFunction(
        name = "is_timestamp",
        description = "Check if value's type is a timestamp.",
        isFallible = false,
        isPure = true,
        arguments = listOf(
            VRLFunctionArgument(
                "value",
                setOf("any"),
                "The value to check if it is a timestamp.",
                true
            )
        ),
        returnTypes = setOf("boolean")
    ),
    "object" to VRLFunction(
        name = "object",
        description = "Returns value if it is an object, otherwise returns an error. This enables the type checker to guarantee that the returned value is an object and can be used in any function that expects an object.",
        isFallible = true,
        isPure = true,
        arguments = listOf(
            VRLFunctionArgument(
                "value",
                setOf("any"),
                "The value to check if it is an object.",
                true
            )
        ),
        returnTypes = setOf("object", "error")
    ),
    "string" to VRLFunction(
        name = "string",
        description = "Returns value if it is a string, otherwise returns an error. This enables the type checker to guarantee that the returned value is a string and can be used in any function that expects a string.",
        isFallible = true,
        isPure = true,
        arguments = listOf(
            VRLFunctionArgument(
                "value",
                setOf("any"),
                "The value to check if it is a string.",
                true
            )
        ),
        returnTypes = setOf("string", "error")
    ),
    "tag_types_externally" to VRLFunction(
        name = "tag_types_externally",
        description = "Adds type information to all (nested) scalar values in the provided value. The type information is added externally, meaning that value has the form of \"type\": value after this transformation.",
        isFallible = false,
        isPure = true,
        arguments = listOf(
            VRLFunctionArgument(
                "value",
                setOf("any"),
                "The value to tag with types.",
                true
            )
        ),
        returnTypes = setOf("object", "array", "null")
    ),
    "timestamp" to VRLFunction(
        name = "timestamp",
        description = "Returns value if it is a timestamp, otherwise returns an error. This enables the type checker to guarantee that the returned value is a timestamp and can be used in any function that expects a timestamp.",
        isFallible = true,
        isPure = true,
        arguments = listOf(
            VRLFunctionArgument(
                "value",
                setOf("any"),
                "The value to check if it is a timestamp.",
                true
            )
        ),
        returnTypes = setOf("timestamp", "error")
    ),
    "validate_json_schema" to VRLFunction(
        name = "validate_json_schema",
        description = "Check if value conforms to a JSON Schema definition. This function validates a JSON payload against a JSON Schema definition. It can be used to ensure that the data structure and types in value match the expectations defined in schema_definition.",
        isFallible = true,
        isPure = true,
        arguments = listOf(
            VRLFunctionArgument(
                "value",
                setOf("any"),
                "The value to check if it conforms to the JSON schema definition.",
                true
            ),
            VRLFunctionArgument(
                "schema_definition",
                setOf("any"),
                "The location (path) of the JSON Schema definition.",
                true
            ),
            VRLFunctionArgument(
                "ignore_unknown_formats",
                setOf("boolean"),
                "Unknown formats can be silently ignored by setting this to true and validation continues without failing due to those fields.",
                false
            )
        ),
        returnTypes = setOf("boolean", "error")
    )
)