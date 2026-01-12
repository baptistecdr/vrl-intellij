package eu.bcosp.vrlintellij.functions

val randomFunctions = mapOf(
    "random_bool" to VRLFunction(
        name = "random_bool",
        description = "Returns a random boolean.",
        isFallible = false,
        isPure = true,
        arguments = listOf(),
        returnTypes = setOf("boolean")
    ),
    "random_bytes" to VRLFunction(
        name = "random_bytes",
        description = "A cryptographically secure random number generator. Returns a string value containing the number of random bytes requested.",
        isFallible = true,
        isPure = true,
        arguments = listOf(
            VRLFunctionArgument(
                "length",
                setOf("integer"),
                "The number of bytes to generate. Must not be larger than 64k.",
                true
            )
        ),
        returnTypes = setOf("string", "error")
    ),
    "random_float" to VRLFunction(
        name = "random_float",
        description = "Returns a random float between [min, max).",
        isFallible = true,
        isPure = true,
        arguments = listOf(
            VRLFunctionArgument(
                "min",
                setOf("float"),
                "Minimum value (inclusive).",
                true
            ),
            VRLFunctionArgument(
                "max",
                setOf("float"),
                "Maximum value (exclusive).",
                true
            )
        ),
        returnTypes = setOf("float", "error")
    ),
    "random_int" to VRLFunction(
        name = "random_int",
        description = "Returns a random integer between [min, max).",
        isFallible = true,
        isPure = true,
        arguments = listOf(
            VRLFunctionArgument(
                "min",
                setOf("integer"),
                "Minimum value (inclusive).",
                true
            ),
            VRLFunctionArgument(
                "max",
                setOf("integer"),
                "Maximum value (exclusive).",
                true
            )
        ),
        returnTypes = setOf("integer", "error")
    ),
    "uuid_from_friendly_id" to VRLFunction(
        name = "uuid_from_friendly_id",
        description = "Convert a Friendly ID (base62 encoding a 128-bit word) to a UUID.",
        isFallible = true,
        isPure = true,
        arguments = listOf(
            VRLFunctionArgument(
                "value",
                setOf("string"),
                "A string that is a Friendly ID.",
                true
            )
        ),
        returnTypes = setOf("string", "error")
    ),
    "uuid_v4" to VRLFunction(
        name = "uuid_v4",
        description = "Generates a random UUIDv4 string.",
        isFallible = false,
        isPure = true,
        arguments = listOf(),
        returnTypes = setOf("string")
    ),
    "uuid_v7" to VRLFunction(
        name = "uuid_v7",
        description = "Generates a random UUIDv7 string.",
        isFallible = false,
        isPure = true,
        arguments = listOf(
            VRLFunctionArgument(
                "timestamp",
                setOf("timestamp"),
                "The timestamp used to generate the UUIDv7.",
                false,
                "now()"
            )
        ),
        returnTypes = setOf("string")
    )
)