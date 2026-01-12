package eu.bcosp.vrlintellij.functions

val ipFunctions = mapOf(
    "decrypt_ip" to VRLFunction(
        name = "decrypt_ip",
        description = "Decrypts an IP address that was previously encrypted, restoring the original IP address.",
        isFallible = true,
        isPure = true,
        arguments = listOf(
            VRLFunctionArgument(
                "ip",
                setOf("string"),
                "The encrypted IP address to decrypt (v4 or v6).",
                true
            ),
            VRLFunctionArgument(
                "key",
                setOf("string"),
                "The decryption key in raw bytes (not encoded). Must be the same key that was used for encryption. For AES128 mode, the key must be exactly 16 bytes. For PFX mode, the key must be exactly 32 bytes.",
                true
            ),
            VRLFunctionArgument(
                "mode",
                setOf("string"),
                "The decryption mode to use. Must match the mode used for encryption: either aes128 or pfx.",
                true,
            ),
        ),
        returnTypes = setOf("string", "error")
    ),
    "encrypt_ip" to VRLFunction(
        name = "encrypt_ip",
        description = "Encrypts an IP address using AES128 mode.",
        isFallible = true,
        isPure = true,
        arguments = listOf(
            VRLFunctionArgument(
                "ip",
                setOf("string"),
                "The IP address to encrypt (v4 or v6).",
                true
            ),
            VRLFunctionArgument(
                "key",
                setOf("string"),
                "The encryption key in raw bytes (not encoded). For AES128 mode, the key must be exactly 16 bytes. For PFX mode, the key must be exactly 32 bytes.",
                true
            ),
            VRLFunctionArgument(
                "mode",
                setOf("string"),
                "The encryption mode to use. Must be either aes128 or pfx.",
                true,
            ),
        ),
        returnTypes = setOf("string", "error")
    ),
    "ip_aton" to VRLFunction(
        name = "ip_aton",
        description = "Converts IPv4 address in numbers-and-dots notation into network-order bytes represented as an integer.",
        isFallible = true,
        isPure = true,
        arguments = listOf(
            VRLFunctionArgument(
                "value",
                setOf("string"),
                "The IP address to convert to binary.",
                true
            )
        ),
        returnTypes = setOf("string", "error")
    ),
    "ip_cidr_contains" to VRLFunction(
        name = "ip_cidr_contains",
        description = "Checks whether an IP address is within a CIDR range.",
        isFallible = true,
        isPure = true,
        arguments = listOf(
            VRLFunctionArgument(
                "cidr",
                setOf("string", "array"),
                "The CIDR mask (v4 or v6).",
                true
            ),
            VRLFunctionArgument(
                "ip",
                setOf("string"),
                "The IP address (v4 or v6).",
                true
            ),
        ),
        returnTypes = setOf("boolean", "error")
    ),
    "ip_ntoa" to VRLFunction(
        name = "ip_ntoa",
        description = "Converts numeric representation of IPv4 address in network-order bytes to numbers-and-dots notation.",
        isFallible = true,
        isPure = true,
        arguments = listOf(
            VRLFunctionArgument(
                "value",
                setOf("string"),
                "The integer representation of an IPv4 address.",
                true
            )
        ),
        returnTypes = setOf("string", "error")
    ),
    "ip_ntop" to VRLFunction(
        name = "ip_ntop",
        description = "Converts IPv4 and IPv6 addresses from binary to text form.",
        isFallible = true,
        isPure = true,
        arguments = listOf(
            VRLFunctionArgument(
                "value",
                setOf("string"),
                "The binary data to convert from. For IPv4 addresses, it must be 4 bytes (32 bits) long. For IPv6 addresses, it must be 16 bytes (128 bits) long.",
                true
            )
        ),
        returnTypes = setOf("string", "error")
    ),
    "ip_pton" to VRLFunction(
        name = "ip_pton",
        description = "Converts IPv4 and IPv6 addresses from text to binary form.",
        isFallible = true,
        isPure = true,
        arguments = listOf(
            VRLFunctionArgument(
                "value",
                setOf("string"),
                "The IP address (v4 or v6) to convert to binary form.",
                true
            )
        ),
        returnTypes = setOf("string", "error")
    ),
    "ip_subnet" to VRLFunction(
        name = "ip_subnet",
        description = "Extracts the subnet address from the ip using the supplied subnet.",
        isFallible = true,
        isPure = true,
        arguments = listOf(
            VRLFunctionArgument(
                "ip",
                setOf("string"),
                "The IP address (v4 or v6).",
                true
            ),
            VRLFunctionArgument(
                "subnet",
                setOf("string"),
                "The subnet to extract from the IP address. This can be either a prefix length like /8 or a net mask like 255.255.0.0. The net mask can be either an IPv4 or IPv6 address.",
                true
            )
        ),
        returnTypes = setOf("string", "error")
    ),
    "ip_to_ipv6" to VRLFunction(
        name = "ip_to_ipv6",
        description = "Converts the ip to an IPv6 address.",
        isFallible = true,
        isPure = true,
        arguments = listOf(
            VRLFunctionArgument(
                "ip",
                setOf("string"),
                "The IP address to convert to IPv6.",
                true
            )
        ),
        returnTypes = setOf("string", "error")
    ),
    "ipv6_to_ipv4" to VRLFunction(
        name = "ipv6_to_ipv4",
        description = "Converts the ip to an IPv4 address. ip is returned unchanged if it’s already an IPv4 address. If ip is currently an IPv6 address then it needs to be IPv4 compatible, otherwise an error is thrown.",
        isFallible = true,
        isPure = true,
        arguments = listOf(
            VRLFunctionArgument(
                "ip",
                setOf("string"),
                "The IPv4-mapped IPv6 address to convert.",
                true
            )
        ),
        returnTypes = setOf("string", "error")
    ),
    "is_ipv4" to VRLFunction(
        name = "is_ipv4",
        description = "Check if the string is a valid IPv4 address or not.",
        isFallible = false,
        isPure = true,
        arguments = listOf(
            VRLFunctionArgument(
                "value",
                setOf("string"),
                "The IP address to check",
                true
            )
        ),
        returnTypes = setOf("boolean")
    ),
    "is_ipv6" to VRLFunction(
        name = "is_ipv6",
        description = "Check if the string is a valid IPv6 address or not.",
        isFallible = false,
        isPure = true,
        arguments = listOf(
            VRLFunctionArgument(
                "value",
                setOf("string"),
                "The IP address to check",
                true
            )
        ),
        returnTypes = setOf("boolean")
    )
)