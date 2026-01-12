package eu.bcosp.vrlintellij.functions

val systemFunctions = mapOf(
    "dns_lookup" to VRLFunction(
        name = "dns_lookup",
        description = "Performs a DNS lookup on the provided domain name. This function performs synchronous blocking operations and is not recommended for frequent or performance-critical workflows due to potential network-related delays.",
        isFallible = false,
        isPure = true,
        arguments = listOf(
            VRLFunctionArgument(
                "value",
                setOf("string"),
                "The domain name to query.",
                true
            ),
            VRLFunctionArgument(
                "qtype",
                setOf("string"),
                "The DNS record type to query (e.g., A, AAAA, MX, TXT). Defaults to A.",
                false,
                "A"
            ),
            VRLFunctionArgument(
                "class",
                setOf("string"),
                "The DNS query class. Defaults to IN (Internet).",
                false,
                "IN"
            ),
            VRLFunctionArgument(
                "options",
                setOf("object"),
                "DNS resolver options. Supported fields: servers (array of nameserver addresses), timeout (seconds), attempts (number of retry attempts), ndots, aa_only, tcp, recurse, rotate.",
                false
            )
        ),
        returnTypes = setOf("object")
    ),
    "get_env_var" to VRLFunction(
        name = "get_env_var",
        description = "Returns the value of the environment variable specified by name.",
        isFallible = true,
        isPure = true,
        arguments = listOf(
            VRLFunctionArgument(
                "name",
                setOf("string"),
                "The name of the environment variable.",
                true
            )
        ),
        returnTypes = setOf("string", "error")
    ),
    "get_hostname" to VRLFunction(
        name = "get_hostname",
        description = "Returns the local system's hostname.",
        isFallible = true,
        isPure = true,
        arguments = listOf(),
        returnTypes = setOf("string", "error")
    ),
    "get_timezone_name" to VRLFunction(
        name = "get_timezone_name",
        description = "Returns the name of the timezone in the Vector configuration (see global configuration options). If the configuration is set to local, then it attempts to determine the name of the timezone from the host OS. If this is not possible, then it returns the fixed offset of the local timezone for the current time in the format \"[+-]HH:MM\", for example, \"+02:00\".",
        isFallible = true,
        isPure = true,
        arguments = listOf(),
        returnTypes = setOf("string", "error")
    ),
    "http_request" to VRLFunction(
        name = "http_request",
        description = "Makes an HTTP request to the specified URL. This function performs synchronous blocking operations and is not recommended for frequent or performance-critical workflows due to potential network-related delays.",
        isFallible = false,
        isPure = true,
        arguments = listOf(
            VRLFunctionArgument(
                "url",
                setOf("string"),
                "The URL to make the HTTP request to.",
                true
            ),
            VRLFunctionArgument(
                "method",
                setOf("string"),
                "The HTTP method to use (e.g., GET, POST, PUT, DELETE). Defaults to GET.",
                false,
                "get"
            ),
            VRLFunctionArgument(
                "headers",
                setOf("object"),
                "An object containing HTTP headers to send with the request.",
                false
            ),
            VRLFunctionArgument(
                "body",
                setOf("string"),
                "The request body content to send.",
                false
            ),
            VRLFunctionArgument(
                "http_proxy",
                setOf("string"),
                "HTTP proxy URL to use for the request.",
                false
            ),
            VRLFunctionArgument(
                "https_proxy",
                setOf("string"),
                "HTTPS proxy URL to use for the request.",
                false
            ),
            VRLFunctionArgument(
                "redact_headers",
                setOf("boolean"),
                "Whether to redact sensitive header values in error messages.",
                false,
                true
            )
        ),
        returnTypes = setOf("string")
    ),
    "reverse_dns" to VRLFunction(
        name = "reverse_dns",
        description = "Performs a reverse DNS lookup on the provided IP address to retrieve the associated hostname. This function performs synchronous blocking operations and is not recommended for frequent or performance-critical workflows due to potential network-related delays.",
        isFallible = false,
        isPure = true,
        arguments = listOf(
            VRLFunctionArgument(
                "value",
                setOf("string"),
                "The IP address (IPv4 or IPv6) to perform the reverse DNS lookup on.",
                true
            )
        ),
        returnTypes = setOf("string")
    )
)