package eu.bcosp.vrlintellij.functions

val stringFunctions = mapOf(
    "basename" to VRLFunction(
        name = "basename",
        description = "Returns the filename component of the given path. This is similar to the Unix basename command. If the path ends in a directory separator, the function returns the name of the directory.",
        isFallible = true,
        isPure = true,
        arguments = listOf(
            VRLFunctionArgument(
                "value",
                setOf("string"),
                "The path from which to extract the basename.",
                true
            )
        ),
        returnTypes = setOf("string", "null", "error")
    ),
    "camelcase" to VRLFunction(
        name = "camelcase",
        description = "Takes the value string, and turns it into camelCase. Optionally, you can pass in the existing case of the function, or else an attempt is made to determine the case automatically.",
        isFallible = false,
        isPure = true,
        arguments = listOf(
            VRLFunctionArgument(
                "value",
                setOf("string"),
                "The string to convert to camelCase.",
                true
            ),
            VRLFunctionArgument(
                "original_case",
                setOf("string"),
                "Optional hint on the original case type. Must be one of: kebab-case, camelCase, PascalCase, SCREAMING_SNAKE, snake_case",
                false
            )
        ),
        returnTypes = setOf("string")
    ),
    "community_id" to VRLFunction(
        name = "community_id",
        description = "Generates an ID based on the Community ID Spec.",
        isFallible = false,
        isPure = true,
        arguments = listOf(
            VRLFunctionArgument(
                "source_ip",
                setOf("string"),
                "The source IP address.",
                true
            ),
            VRLFunctionArgument(
                "destination_ip",
                setOf("string"),
                "The destination IP address.",
                true
            ),
            VRLFunctionArgument(
                "protocol",
                setOf("integer"),
                "The protocol number.",
                true
            ),
            VRLFunctionArgument(
                "source_port",
                setOf("integer"),
                "The source port or ICMP type.",
                false
            ),
            VRLFunctionArgument(
                "destination_port",
                setOf("integer"),
                "The destination port or ICMP code.",
                false
            ),
            VRLFunctionArgument(
                "seed",
                setOf("integer"),
                "The custom seed number.",
                false
            )
        ),
        returnTypes = setOf("string")
    ),
    "contains" to VRLFunction(
        name = "contains",
        description = "Determines whether the value string contains the specified substring.",
        isFallible = false,
        isPure = true,
        arguments = listOf(
            VRLFunctionArgument(
                "value",
                setOf("string"),
                "The text to search.",
                true
            ),
            VRLFunctionArgument(
                "substring",
                setOf("string"),
                "The substring to search for in value.",
                true
            ),
            VRLFunctionArgument(
                "case_sensitive",
                setOf("boolean"),
                "Whether the match should be case sensitive.",
                false,
                true
            )
        ),
        returnTypes = setOf("boolean")
    ),
    "contains_all" to VRLFunction(
        name = "contains_all",
        description = "Determines whether the value string contains all the specified substrings.",
        isFallible = false,
        isPure = true,
        arguments = listOf(
            VRLFunctionArgument(
                "value",
                setOf("string"),
                "The text to search.",
                true
            ),
            VRLFunctionArgument(
                "substrings",
                setOf("array"),
                "An array of substrings to search for in value.",
                true
            ),
            VRLFunctionArgument(
                "case_sensitive",
                setOf("boolean"),
                "Whether the match should be case sensitive.",
                false
            )
        ),
        returnTypes = setOf("boolean")
    ),
    "dirname" to VRLFunction(
        name = "dirname",
        description = "Returns the directory component of the given path. This is similar to the Unix dirname command. The directory component is the path with the final component removed.",
        isFallible = true,
        isPure = true,
        arguments = listOf(
            VRLFunctionArgument(
                "value",
                setOf("string"),
                "The path from which to extract the directory name.",
                true
            )
        ),
        returnTypes = setOf("string", "error")
    ),
    "downcase" to VRLFunction(
        name = "downcase",
        description = "Downcases the value string, where downcase is defined according to the Unicode Derived Core Property Lowercase.",
        isFallible = false,
        isPure = true,
        arguments = listOf(
            VRLFunctionArgument(
                "value",
                setOf("string"),
                "The string to convert to lowercase.",
                true
            )
        ),
        returnTypes = setOf("string")
    ),
    "ends_with" to VRLFunction(
        name = "ends_with",
        description = "Determines whether the value string ends with the specified substring.",
        isFallible = false,
        isPure = true,
        arguments = listOf(
            VRLFunctionArgument(
                "value",
                setOf("string"),
                "The string to search.",
                true
            ),
            VRLFunctionArgument(
                "substring",
                setOf("string"),
                "The substring with which value must end.",
                true
            ),
            VRLFunctionArgument(
                "case_sensitive",
                setOf("boolean"),
                "Whether the match should be case sensitive.",
                false,
                true
            )
        ),
        returnTypes = setOf("boolean")
    ),
    "find" to VRLFunction(
        name = "find",
        description = "Determines from left to right the start position of the first found element in value that matches pattern. Returns -1 if not found.",
        isFallible = false,
        isPure = true,
        arguments = listOf(
            VRLFunctionArgument(
                "value",
                setOf("string"),
                "The string to find the pattern in.",
                true
            ),
            VRLFunctionArgument(
                "pattern",
                setOf("regex", "string"),
                "The regular expression or string pattern to match against.",
                true
            ),
            VRLFunctionArgument(
                "from",
                setOf("integer"),
                "Offset to start searching.",
                false
            )
        ),
        returnTypes = setOf("integer")
    ),
    "join" to VRLFunction(
        name = "join",
        description = "Joins each string in the value array into a single string, with items optionally separated from one another by a separator.",
        isFallible = false,
        isPure = true,
        arguments = listOf(
            VRLFunctionArgument(
                "value",
                setOf("array"),
                "The array of strings to join together.",
                true
            ),
            VRLFunctionArgument(
                "separator",
                setOf("string"),
                "The string separating each original element when joined.",
                false
            )
        ),
        returnTypes = setOf("string")
    ),
    "kebabcase" to VRLFunction(
        name = "kebabcase",
        description = "Takes the value string, and turns it into kebab-case. Optionally, you can pass in the existing case of the function, or else we will try to figure out the case automatically.",
        isFallible = false,
        isPure = true,
        arguments = listOf(
            VRLFunctionArgument(
                "value",
                setOf("string"),
                "The string to convert to kebab-case.",
                true
            ),
            VRLFunctionArgument(
                "original_case",
                setOf("string"),
                "Optional hint on the original case type. Must be one of: kebab-case, camelCase, PascalCase, SCREAMING_SNAKE, snake_case",
                false
            )
        ),
        returnTypes = setOf("string")
    ),
    "match" to VRLFunction(
        name = "match",
        description = "Determines whether the value matches the pattern.",
        isFallible = false,
        isPure = true,
        arguments = listOf(
            VRLFunctionArgument(
                "value",
                setOf("string"),
                "The value to match.",
                true
            ),
            VRLFunctionArgument(
                "pattern",
                setOf("regex"),
                "The regular expression pattern to match against.",
                true
            )
        ),
        returnTypes = setOf("boolean")
    ),
    "match_any" to VRLFunction(
        name = "match_any",
        description = "Determines whether value matches any of the given patterns. All patterns are checked in a single pass over the target string, giving this function a potential performance advantage over the multiple calls in the match function.",
        isFallible = false,
        isPure = true,
        arguments = listOf(
            VRLFunctionArgument(
                "value",
                setOf("string"),
                "The value to match.",
                true
            ),
            VRLFunctionArgument(
                "patterns",
                setOf("array"),
                "The array of regular expression patterns to match against.",
                true
            )
        ),
        returnTypes = setOf("boolean")
    ),
    "parse_float" to VRLFunction(
        name = "parse_float",
        description = "Parses the string value representing a floating point number in base 10 to a float.",
        isFallible = true,
        isPure = true,
        arguments = listOf(
            VRLFunctionArgument(
                "value",
                setOf("string"),
                "The string to parse.",
                true
            )
        ),
        returnTypes = setOf("float", "error")
    ),
    "pascalcase" to VRLFunction(
        name = "pascalcase",
        description = "Takes the value string, and turns it into PascalCase. Optionally, you can pass in the existing case of the function, or else we will try to figure out the case automatically.",
        isFallible = false,
        isPure = true,
        arguments = listOf(
            VRLFunctionArgument(
                "value",
                setOf("string"),
                "The string to convert to PascalCase.",
                true
            ),
            VRLFunctionArgument(
                "original_case",
                setOf("string"),
                "Optional hint on the original case type. Must be one of: kebab-case, camelCase, PascalCase, SCREAMING_SNAKE, snake_case",
                false
            )
        ),
        returnTypes = setOf("string")
    ),
    "redact" to VRLFunction(
        name = "redact",
        description = "Redact sensitive data in value such as US social security card numbers and other forms of personally identifiable information with custom patterns. This can help achieve compliance by ensuring sensitive data does not leave your network.",
        isFallible = false,
        isPure = true,
        arguments = listOf(
            VRLFunctionArgument(
                "value",
                setOf("string", "object", "array"),
                "The value to redact sensitive data from. For strings, the sensitive data is redacted and a new string is returned. For arrays, the sensitive data is redacted in each string element. For objects, the sensitive data in each string value is masked, but the keys are not masked.",
                true
            ),
            VRLFunctionArgument(
                "filters",
                setOf("array"),
                "List of filters applied to value. Each filter can be specified as a regular expression, as an object with a type key, or as a named filter.",
                true
            ),
            VRLFunctionArgument(
                "redactor",
                setOf("string", "object"),
                "Specifies what to replace the redacted strings with. Supports types: full, text, sha2, sha3.",
                false
            )
        ),
        returnTypes = setOf("string", "object", "array")
    ),
    "replace" to VRLFunction(
        name = "replace",
        description = "Replaces all matching instances of pattern in value. The pattern argument accepts regular expression capture groups.",
        isFallible = false,
        isPure = true,
        arguments = listOf(
            VRLFunctionArgument(
                "value",
                setOf("string"),
                "The original string.",
                true
            ),
            VRLFunctionArgument(
                "pattern",
                setOf("regex", "string"),
                "Replace all matches of this pattern. Can be a static string or a regular expression.",
                true
            ),
            VRLFunctionArgument(
                "with",
                setOf("string"),
                "The string that the matches are replaced with.",
                true
            ),
            VRLFunctionArgument(
                "count",
                setOf("integer"),
                "The maximum number of replacements to perform. -1 means replace all matches.",
                false,
                -1
            )
        ),
        returnTypes = setOf("string")
    ),
    "replace_with" to VRLFunction(
        name = "replace_with",
        description = "Replaces all matching instances of pattern using a closure. The pattern argument accepts a regular expression that can use capture groups.",
        isFallible = false,
        isPure = true,
        arguments = listOf(
            VRLFunctionArgument(
                "value",
                setOf("string"),
                "The original string.",
                true
            ),
            VRLFunctionArgument(
                "pattern",
                setOf("regex"),
                "Replace all matches of this pattern. Must be a regular expression.",
                true
            ),
            VRLFunctionArgument(
                "count",
                setOf("integer"),
                "The maximum number of replacements to perform. -1 means replace all matches.",
                false,
                -1
            )
        ),
        returnTypes = setOf("string")
    ),
    "screamingsnakecase" to VRLFunction(
        name = "screamingsnakecase",
        description = "Takes the value string, and turns it into SCREAMING_SNAKE case. Optionally, you can pass in the existing case of the function, or else we will try to figure out the case automatically.",
        isFallible = false,
        isPure = true,
        arguments = listOf(
            VRLFunctionArgument(
                "value",
                setOf("string"),
                "The string to convert to SCREAMING_SNAKE case.",
                true
            ),
            VRLFunctionArgument(
                "original_case",
                setOf("string"),
                "Optional hint on the original case type. Must be one of: kebab-case, camelCase, PascalCase, SCREAMING_SNAKE, snake_case",
                false
            )
        ),
        returnTypes = setOf("string")
    )
)