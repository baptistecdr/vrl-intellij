package eu.bcosp.vrlintellij.functions

val checksumFunctions = mapOf(
    "crc" to VRLFunction(
        name = "crc",
        description = "Calculates a CRC of the value. The CRC algorithm used can be optionally specified. This function is infallible if either the default algorithm value or a recognized-valid compile-time algorithm string literal is used. Otherwise, it is fallible.",
        isFallible = true,
        isPure = true,
        arguments = listOf(
            VRLFunctionArgument(
                "value",
                setOf("string"),
                "The string to calculate the checksum for.",
                true
            ),
            VRLFunctionArgument(
                "algorithm",
                setOf("string"),
                "The CRC algorithm to use.",
                false,
                "CRC_32_ISO_HDLC"
            )
        ),
        returnTypes = setOf("string", "error")
    ),
    "xxhash" to VRLFunction(
        name = "xxhash",
        description = "Calculates a xxHash hash of the value. Note: Due to limitations in the underlying VRL data types, this function converts the unsigned 64-bit integer hash result to a signed 64-bit integer. Results higher than the signed 64-bit integer maximum value wrap around to negative values. For the XXH3-128 hash algorithm, values are returned as a string.",
        isFallible = false,
        isPure = true,
        arguments = listOf(
            VRLFunctionArgument(
                "value",
                setOf("string"),
                "The string to calculate the hash for.",
                true
            ),
            VRLFunctionArgument(
                "variant",
                setOf("string"),
                "The xxHash hashing algorithm to use.",
                false,
                "XXH32"
            )
        ),
        returnTypes = setOf("integer", "string")
    )
)