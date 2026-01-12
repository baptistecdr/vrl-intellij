package eu.bcosp.vrlintellij.functions

val cryptographyFunctions = mapOf(
    "decrypt" to VRLFunction(
        name = "decrypt",
        description = "Decrypts a string with a symmetric encryption algorithm.",
        isFallible = true,
        isPure = true,
        arguments = listOf(
            VRLFunctionArgument(
                "ciphertext",
                setOf("string"),
                "The string in raw bytes (not encoded) to decrypt.",
                true
            ),
            VRLFunctionArgument("algorithm", setOf("string"), "The algorithm to use.", true),
            VRLFunctionArgument(
                "key",
                setOf("string"),
                "The key in raw bytes (not encoded) for decryption. The length must match the algorithm requested.",
                true
            ),
            VRLFunctionArgument(
                "iv",
                setOf("string"),
                "The IV in raw bytes (not encoded) for decryption. The length must match the algorithm requested. A new IV should be generated for every message. You can use random_bytes to generate a cryptographically secure random value. The value should match the one used during encryption.",
                true
            ),
        ),
        returnTypes = setOf("string", "error")
    ),
    "encrypt" to VRLFunction(
        name = "encrypt",
        description = "Encrypts a string with a symmetric encryption algorithm.",
        isFallible = true,
        isPure = true,
        arguments = listOf(
            VRLFunctionArgument("plaintext", setOf("string"), "The string to encrypt.", true),
            VRLFunctionArgument("algorithm", setOf("string"), "The algorithm to use.", true),
            VRLFunctionArgument(
                "key",
                setOf("string"),
                "The key in raw bytes (not encoded) for decryption. The length must match the algorithm requested.",
                true
            ),
            VRLFunctionArgument(
                "iv",
                setOf("string"),
                "The IV in raw bytes (not encoded) for encryption. The length must match the algorithm requested. A new IV should be generated for every message. You can use random_bytes to generate a cryptographically secure random value.",
                true
            ),
        ),
        returnTypes = setOf("string", "error")
    ),
    "hmac" to VRLFunction(
        name = "hmac",
        description = "Calculates a HMAC of the value using the given key. The hashing algorithm used can be optionally specified.",
        isFallible = false,
        isPure = true,
        arguments = listOf(
            VRLFunctionArgument("value", setOf("string"), "The string to calculate the HMAC for.", true),
            VRLFunctionArgument("key", setOf("string"), "The string to use as the cryptographic key.", true),
            VRLFunctionArgument(
                "algorithm",
                setOf("string"),
                "The hashing algorithm to use.",
                false,
                defaultValue = "SHA-256"
            ),
        ),
        returnTypes = setOf("string")
    ),
    "md5" to VRLFunction(
        name = "md5",
        description = "Calculates an md5 hash of the value.",
        isFallible = false,
        isPure = true,
        arguments = listOf(
            VRLFunctionArgument("value", setOf("string"), "The string to calculate the hash for.", true),
        ),
        returnTypes = setOf("string")
    ),
    "seahash" to VRLFunction(
        name = "seahash",
        description = "Calculates a Seahash hash of the value. Note: Due to limitations in the underlying VRL data types, this function converts the unsigned 64-bit integer SeaHash result to a signed 64-bit integer. Results higher than the signed 64-bit integer maximum value wrap around to negative values.",
        isFallible = false,
        isPure = false,
        arguments = listOf(
            VRLFunctionArgument("value", setOf("string"), "The string to calculate the hash for.", true),

            ),
        returnTypes = setOf("integer")
    ),
    "sha1" to VRLFunction(
        name = "sha1",
        description = "Calculates a SHA-1 hash of the value.",
        isFallible = false,
        isPure = true,
        arguments = listOf(
            VRLFunctionArgument("value", setOf("string"), "The string to calculate the hash for.", true),
        ),
        returnTypes = setOf("string")
    ),
    "sha2" to VRLFunction(
        name = "sha2",
        description = "Calculates a SHA-2 hash of the value.",
        isFallible = false,
        isPure = true,
        arguments = listOf(
            VRLFunctionArgument("value", setOf("string"), "The string to calculate the hash for.", true),
            VRLFunctionArgument(
                "variant",
                setOf("string"),
                "The variant of the algorithm to use.",
                false,
                defaultValue = "SHA-512/256"
            ),
        ),
        returnTypes = setOf("string")
    ),
    "sha3" to VRLFunction(
        name = "sha3",
        description = "Calculates a SHA-3 hash of the value.",
        isFallible = false,
        isPure = true,
        arguments = listOf(
            VRLFunctionArgument("value", setOf("string"), "The string to calculate the hash for.", true),
            VRLFunctionArgument(
                "variant",
                setOf("string"),
                "The variant of the algorithm to use.",
                false,
                defaultValue = "SHA3-512"
            ),
        ),
        returnTypes = setOf("string")
    ),
)