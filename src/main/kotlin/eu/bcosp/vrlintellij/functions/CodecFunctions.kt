package eu.bcosp.vrlintellij.functions

val codecFunctions = mapOf(
    "decode_base16" to VRLFunction(
        name = "decode_base16",
        description = "Decodes the value (a Base16 string) into its original string.",
        isFallible = true,
        isPure = true,
        arguments = listOf(
            VRLFunctionArgument("value", setOf("string"), "The Base16 data to decode.", true)
        ),
        returnTypes = setOf("string", "error")
    ),
    "decode_base64" to VRLFunction(
        name = "decode_base64",
        description = "Decodes the value (a Base64 string) into its original string.",
        isFallible = true,
        isPure = true,
        arguments = listOf(
            VRLFunctionArgument("value", setOf("string"), "The Base64 data to decode.", true),
            VRLFunctionArgument(
                "charset",
                setOf("string"),
                "The character set to use when decoding the data.",
                false,
                "standard"
            ),
        ),
        returnTypes = setOf("string", "error")
    ),
    "decode_charset" to VRLFunction(
        name = "decode_charset",
        description = "Decodes the value (a non-UTF8 string) to a UTF8 string using the specified character set.",
        isFallible = true,
        isPure = true,
        arguments = listOf(
            VRLFunctionArgument("value", setOf("string"), "The non-UTF8 string to decode.", true),
            VRLFunctionArgument(
                "from_charset",
                setOf("string"),
                "The character set to use when decoding the data.",
                true
            )
        ),
        returnTypes = setOf("string", "error")
    ),
    "decode_gzip" to VRLFunction(
        name = "decode_gzip",
        description = "Decodes the value (a Gzip string) into its original string.",
        isFallible = true,
        isPure = true,
        arguments = listOf(
            VRLFunctionArgument("value", setOf("string"), "The Gzip data to decode.", true)
        ),
        returnTypes = setOf("string", "error")
    ),
    "decode_lz4" to VRLFunction(
        name = "decode_lz4",
        description = "Decodes the value (an lz4 string) into its original string. buf_size is the size of the buffer to decode into, this must be equal to or larger than the uncompressed size. If prepended_size is set to true, it expects the original uncompressed size to be prepended to the compressed data. prepended_size is useful for some implementations of lz4 that require the original size to be known before decoding.",
        isFallible = true,
        isPure = true,
        arguments = listOf(
            VRLFunctionArgument("value", setOf("string"), "The lz4 block data to decode.", true),
            VRLFunctionArgument(
                "buf_size",
                setOf("integer"),
                "The size of the buffer to decode into, this must be equal to or larger than the uncompressed size.",
                false,
                1.048576e+06
            ),
            VRLFunctionArgument(
                "prepended_size",
                setOf("boolean"),
                "Some implementations of lz4 require the original uncompressed size to be prepended to the compressed data.",
                false
            )
        ),
        returnTypes = setOf("string", "error")
    ),
    "decode_mime_q" to VRLFunction(
        name = "decode_mime_q",
        description = "Replaces q-encoded or base64-encoded encoded-word substrings in the value with their original string.",
        isFallible = true,
        isPure = true,
        arguments = listOf(
            VRLFunctionArgument("value", setOf("string"), "The string with encoded-words to decode.", true)
        ),
        returnTypes = setOf("string", "error")
    ),
    "decode_percent" to VRLFunction(
        name = "decode_percent",
        description = "Decodes a percent-encoded value like a URL.",
        isFallible = false,
        isPure = true,
        arguments = listOf(
            VRLFunctionArgument("value", setOf("string"), "The string to decode.", true)
        ),
        returnTypes = setOf("string")
    ),
    "decode_punycode" to VRLFunction(
        name = "decode_punycode",
        description = "Decodes a punycode encoded value, such as an internationalized domain name (IDN). This function assumes that the value passed is meant to be used in IDN context and that it is either a domain name or a part of it.",
        isFallible = true,
        isPure = true,
        arguments = listOf(
            VRLFunctionArgument("value", setOf("string"), "The string to decode.", true),
            VRLFunctionArgument(
                "validate",
                setOf("boolean"),
                "If enabled, checks if the input string is a valid domain name.",
                isRequired = false,
                defaultValue = true
            )
        ),
        returnTypes = setOf("string", "error")
    ),
    "decode_snappy" to VRLFunction(
        name = "decode_snappy",
        description = "Decodes the value (a Snappy string) into its original string.",
        isFallible = true,
        isPure = true,
        arguments = listOf(
            VRLFunctionArgument("value", setOf("string"), "The Snappy data to decode.", true)
        ),
        returnTypes = setOf("string", "error")
    ),
    "decode_zlib" to VRLFunction(
        name = "decode_zlib",
        description = "Decodes the value (a Zlib string) into its original string.",
        isFallible = true,
        isPure = true,
        arguments = listOf(
            VRLFunctionArgument("value", setOf("string"), "The Zlib data to decode.", true)
        ),
        returnTypes = setOf("string", "error")
    ),
    "decode_zstd" to VRLFunction(
        name = "decode_zstd",
        description = "Decodes the value (a Zstandard string) into its original string.",
        isFallible = true,
        isPure = true,
        arguments = listOf(
            VRLFunctionArgument("value", setOf("string"), "The Zstandard data to decode.", true)
        ),
        returnTypes = setOf("string", "error")
    ),
    "encode_base16" to VRLFunction(
        name = "encode_base16",
        description = "Encodes the value to Base16.",
        isFallible = false,
        isPure = true,
        arguments = listOf(
            VRLFunctionArgument("value", setOf("string"), "The string to encode.", true)
        ),
        returnTypes = setOf("string")
    ),
    "encode_base64" to VRLFunction(
        name = "encode_base64",
        description = "Encodes the value to Base64.",
        isFallible = false,
        isPure = true,
        arguments = listOf(
            VRLFunctionArgument("value", setOf("string"), "The string to encode.", true),
            VRLFunctionArgument(
                "padding",
                setOf("boolean"),
                "Whether the Base64 output is padded.",
                isRequired = false,
                defaultValue = true
            ),
            VRLFunctionArgument(
                "charset",
                setOf("string"),
                "The character set to use when encoding the data.",
                true,
            )
        ),
        returnTypes = setOf("string"),
    ),
    "encode_charset" to VRLFunction(
        name = "encode_charset",
        description = "Encodes the value to a non-UTF8 string using the specified character set.",
        isFallible = true,
        isPure = true,
        arguments = listOf(
            VRLFunctionArgument("value", setOf("string"), "The UTF8 string to encode.", true),
            VRLFunctionArgument(
                "to_charset",
                setOf("string"),
                "The character set to use when encoding the data.",
                true,
            )
        ),
        returnTypes = setOf("string", "error"),
    ),
    "encode_gzip" to VRLFunction(
        name = "encode_gzip",
        description = "Encodes the value to Gzip.",
        isFallible = false,
        isPure = true,
        arguments = listOf(
            VRLFunctionArgument("value", setOf("string"), "The string to encode.", true),
            VRLFunctionArgument(
                "compression_level",
                setOf("integer"),
                "The default compression level.",
                false,
                6,
            ),
        ),
        returnTypes = setOf("string"),
    ),
    "encode_json" to VRLFunction(
        name = "encode_json",
        description = "Encodes the value to JSON.",
        isFallible = false,
        isPure = true,
        arguments = listOf(
            VRLFunctionArgument("value", setOf("any"), "The value to convert to a JSON string.", true),
            VRLFunctionArgument(
                "pretty",
                setOf("boolean"),
                "Whether to pretty print the JSON string or not.",
                isRequired = false,
                defaultValue = false,
            )
        ),
        returnTypes = setOf("string"),
    ),
    "encode_key_value" to VRLFunction(
        name = "encode_key_value",
        description = "Encodes the value into key-value format with customizable delimiters. Default delimiters match the logfmt format.",
        isFallible = true,
        isPure = true,
        arguments = listOf(
            VRLFunctionArgument("value", setOf("object"), "The value to convert to a string.", true),
            VRLFunctionArgument(
                "fields_ordering",
                setOf("array"),
                "The ordering of fields to preserve. Any fields not in this list are listed unordered, after all ordered fields.",
                false,
            ),
            VRLFunctionArgument(
                "key_value_delimiter",
                setOf("string"),
                "The string that separates the key from the value.",
                false,
                "="
            ),
            VRLFunctionArgument(
                "field_delimiter",
                setOf("string"),
                "The string that separates each key-value pair.",
                false,
            ),
            VRLFunctionArgument(
                "flatten_boolean",
                setOf("string"),
                "Whether to encode key-value with a boolean value as a standalone key if true and nothing if false.",
                false,
            ),
        ),
        returnTypes = setOf("string", "error"),
    ),
    "encode_logfmt" to VRLFunction(
        name = "encode_logfmt",
        description = "Encodes the value to logfmt.",
        isFallible = true,
        isPure = true,
        arguments = listOf(
            VRLFunctionArgument("value", setOf("object"), "The value to convert to a logfmt string.", true),
            VRLFunctionArgument(
                "fields_ordering",
                setOf("array"),
                "The ordering of fields to preserve. Any fields not in this list are listed unordered, after all ordered fields.",
                false,
            )
        ),
        returnTypes = setOf("string"),
    ),
    "encode_lz4" to VRLFunction(
        name = "encode_lz4",
        description = "Encodes the value to Lz4. This function compresses the input string into an lz4 block. If prepend_size is set to true, it prepends the original uncompressed size to the compressed data. This is useful for some implementations of lz4 that require the original size to be known before decoding.",
        isFallible = false,
        isPure = true,
        arguments = listOf(
            VRLFunctionArgument("value", setOf("string"), "The string to encode.", true),
            VRLFunctionArgument(
                "prepend_size",
                setOf("boolean"),
                "Whether to prepend the original size to the compressed data.",
                false,
            )
        ),
        returnTypes = setOf("string"),
    ),
    "encode_percent" to VRLFunction(
        name = "encode_percent",
        description = "Encodes a value with percent encoding to safely be used in URLs.",
        isFallible = false,
        isPure = true,
        arguments = listOf(
            VRLFunctionArgument("value", setOf("string"), "The string to encode.", true),
            VRLFunctionArgument(
                "ascii_set",
                setOf("string"),
                "The ASCII set to use when encoding the data.",
                false,
                defaultValue = "NON_ALPHANUMERIC"
            )
        ),
        returnTypes = setOf("string")
    ),
    "encode_proto" to VRLFunction(
        name = "encode_proto",
        description = "Encodes the value to Protocol Buffers.",
        isFallible = true,
        isPure = true,
        arguments = listOf(
            VRLFunctionArgument("value", setOf("object"), "The object to convert to a protocol buffer payload.", true),
            VRLFunctionArgument(
                "desc_file",
                setOf("string"),
                "The path to the protobuf descriptor set file. Must be a literal string.\nThis file is the output of protoc -o ...",
                true,
            ),
            VRLFunctionArgument(
                "message_type",
                setOf("string"),
                "The name of the message type to use for serializing.\nMust be a literal string.",
                true
            )
        ),
        returnTypes = setOf("string", "error"),
    ),
    "encode_punycode" to VRLFunction(
        name = "encode_punycode",
        description = "Encodes a value to punycode. Useful for internationalized domain names (IDN). This function assumes that the value passed is meant to be used in IDN context and that it is either a domain name or a part of it.",
        isFallible = true,
        isPure = true,
        arguments = listOf(
            VRLFunctionArgument("value", setOf("string"), "The string to encode.", true),
            VRLFunctionArgument(
                "validate",
                setOf("boolean"),
                "Whether to validate the input string to check if it is a valid domain name.",
                isRequired = false,
                defaultValue = true
            )
        ),
        returnTypes = setOf("string", "error")
    ),
    "encode_snappy" to VRLFunction(
        name = "encode_snappy",
        description = "Encodes the value to Snappy.",
        isFallible = true,
        isPure = true,
        arguments = listOf(
            VRLFunctionArgument("value", setOf("string"), "The string to encode.", true)
        ),
        returnTypes = setOf("string", "error"),
    ),
    "encode_zlib" to VRLFunction(
        name = "encode_zlib",
        description = "Encodes the value to Zlib.",
        isFallible = false,
        isPure = true,
        arguments = listOf(
            VRLFunctionArgument("value", setOf("string"), "The string to encode.", true),
            VRLFunctionArgument(
                "compression_level",
                setOf("integer"),
                "The default compression level.",
                false,
                6,
            )
        ),
        returnTypes = setOf("string"),
    ),
    "encode_zstd" to VRLFunction(
        name = "encode_zstd",
        description = "Encodes the value to Zstandard.",
        isFallible = false,
        isPure = true,
        arguments = listOf(
            VRLFunctionArgument("value", setOf("string"), "The string to encode.", true),
            VRLFunctionArgument(
                "compression_level",
                setOf("integer"),
                "The default compression level.",
                false,
                3,
            )
        ),
        returnTypes = setOf("string"),
    )
)