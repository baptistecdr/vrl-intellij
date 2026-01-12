package eu.bcosp.vrlintellij.functions

val parseFunctions = mapOf(
    "parse_apache_log" to VRLFunction(
        name = "parse_apache_log",
        description = "Parses Apache access and error log lines. Lines can be in common, combined, or the default error format.",
        isFallible = true,
        isPure = true,
        arguments = listOf(
            VRLFunctionArgument(
                "value",
                setOf("string"),
                "The string to parse.",
                true
            ),
            VRLFunctionArgument(
                "format",
                setOf("string"),
                "The format to use for parsing the log.",
                false,
                "common"
            ),
            VRLFunctionArgument(
                "timestamp_format",
                setOf("string"),
                "The date/time format to use for encoding the timestamp. The time is parsed in local time if the timestamp does not specify a timezone.",
                false,
                "%d/%b/%Y:%T %z"
            )
        ),
        returnTypes = setOf("object", "error")
    ),
    "parse_aws_alb_log" to VRLFunction(
        name = "parse_aws_alb_log",
        description = "Parses value in the Elastic Load Balancer Access format.",
        isFallible = true,
        isPure = true,
        arguments = listOf(
            VRLFunctionArgument(
                "value",
                setOf("string"),
                "Access log of the Application Load Balancer.",
                true
            ),
            VRLFunctionArgument(
                "strict_mode",
                setOf("boolean"),
                "When set to false, the parser ignores any newly added or trailing fields in AWS ALB logs instead of failing. Defaults to true to preserve strict parsing behavior.",
                isRequired = false,
                defaultValue = true
            )
        ),
        returnTypes = setOf("object", "error")
    ),
    "parse_aws_cloudwatch_log_subscription_message" to VRLFunction(
        name = "parse_aws_cloudwatch_log_subscription_message",
        description = "Parses AWS CloudWatch Logs events (configured through AWS Cloudwatch subscriptions) from the aws_kinesis_firehose source.",
        isFallible = true,
        isPure = true,
        arguments = listOf(
            VRLFunctionArgument(
                "value",
                setOf("string"),
                "The string representation of the message to parse.",
                true
            )
        ),
        returnTypes = setOf("object", "error")
    ),
    "parse_aws_vpc_flow_log" to VRLFunction(
        name = "parse_aws_vpc_flow_log",
        description = "Parses value in the VPC Flow Logs format.",
        isFallible = true,
        isPure = true,
        arguments = listOf(
            VRLFunctionArgument(
                "value",
                setOf("string"),
                "VPC Flow Log.",
                true
            ),
            VRLFunctionArgument(
                "format",
                setOf("string"),
                "VPC Flow Log format.",
                false
            )
        ),
        returnTypes = setOf("object", "error")
    ),
    "parse_bytes" to VRLFunction(
        name = "parse_bytes",
        description = "Parses the value into a human-readable bytes format specified by unit and base.",
        isFallible = true,
        isPure = true,
        arguments = listOf(
            VRLFunctionArgument(
                "value",
                setOf("string"),
                "The string of the duration with either binary or SI unit.",
                true
            ),
            VRLFunctionArgument(
                "unit",
                setOf("string"),
                "The output units for the byte.",
                true
            ),
            VRLFunctionArgument(
                "base",
                setOf("string"),
                "The base for the byte, either 2 or 10.",
                false,
                "2"
            )
        ),
        returnTypes = setOf("float", "error")
    ),
    "parse_cbor" to VRLFunction(
        name = "parse_cbor",
        description = "Parses the value as CBOR.",
        isFallible = true,
        isPure = true,
        arguments = listOf(
            VRLFunctionArgument(
                "value",
                setOf("string"),
                "The CBOR payload to parse.",
                true
            )
        ),
        returnTypes = setOf("boolean", "integer", "float", "string", "object", "array", "null", "error")
    ),
    "parse_cef" to VRLFunction(
        name = "parse_cef",
        description = "Parses the value in CEF (Common Event Format) format. Ignores everything up to CEF header. Empty values are returned as empty strings. Surrounding quotes are removed from values.",
        isFallible = true,
        isPure = true,
        arguments = listOf(
            VRLFunctionArgument(
                "value",
                setOf("string"),
                "The string to parse.",
                true
            ),
            VRLFunctionArgument(
                "translate_custom_fields",
                setOf("boolean"),
                "Toggles translation of custom field pairs to key:value.",
                false
            )
        ),
        returnTypes = setOf("object", "error")
    ),
    "parse_common_log" to VRLFunction(
        name = "parse_common_log",
        description = "Parses the value using the Common Log Format (CLF).",
        isFallible = true,
        isPure = true,
        arguments = listOf(
            VRLFunctionArgument(
                "value",
                setOf("string"),
                "The string to parse.",
                true
            ),
            VRLFunctionArgument(
                "timestamp_format",
                setOf("string"),
                "The date/time format to use for encoding the timestamp.",
                false,
                "%d/%b/%Y:%T %z"
            )
        ),
        returnTypes = setOf("object", "error")
    ),
    "parse_csv" to VRLFunction(
        name = "parse_csv",
        description = "Parses a single CSV formatted row. Only the first row is parsed in case of multiline input value.",
        isFallible = true,
        isPure = true,
        arguments = listOf(
            VRLFunctionArgument(
                "value",
                setOf("string"),
                "The string to parse.",
                true
            ),
            VRLFunctionArgument(
                "delimiter",
                setOf("string"),
                "The field delimiter to use when parsing. Must be a single-byte utf8 character.",
                false,
                ","
            )
        ),
        returnTypes = setOf("array", "error")
    ),
    "parse_dnstap" to VRLFunction(
        name = "parse_dnstap",
        description = "Parses the value as base64 encoded DNSTAP data.",
        isFallible = true,
        isPure = true,
        arguments = listOf(
            VRLFunctionArgument(
                "value",
                setOf("string"),
                "The base64 encoded representation of the DNSTAP data to parse.",
                true
            ),
            VRLFunctionArgument(
                "lowercase_hostnames",
                setOf("boolean"),
                "Whether to turn all hostnames found in resulting data lowercase, for consistency.",
                false
            )
        ),
        returnTypes = setOf("object", "error")
    ),
    "parse_duration" to VRLFunction(
        name = "parse_duration",
        description = "Parses the value into a human-readable duration format specified by unit.",
        isFallible = true,
        isPure = true,
        arguments = listOf(
            VRLFunctionArgument(
                "value",
                setOf("string"),
                "The string of the duration.",
                true
            ),
            VRLFunctionArgument(
                "unit",
                setOf("string"),
                "The output units for the duration.",
                true
            )
        ),
        returnTypes = setOf("float", "error")
    ),
    "parse_etld" to VRLFunction(
        name = "parse_etld",
        description = "Parses the eTLD from value representing domain name.",
        isFallible = true,
        isPure = true,
        arguments = listOf(
            VRLFunctionArgument(
                "value",
                setOf("string"),
                "The domain string.",
                true
            ),
            VRLFunctionArgument(
                "plus_parts",
                setOf("integer"),
                "Can be provided to get additional parts of the domain name. When 1 is passed, eTLD+1 will be returned, which represents a domain registrable by a single organization. Higher numbers will return subdomains.",
                false
            ),
            VRLFunctionArgument(
                "psl",
                setOf("string"),
                "Can be provided to use a different public suffix list. By default, https://publicsuffix.org/list/public_suffix_list.dat is used.",
                false
            )
        ),
        returnTypes = setOf("object", "error")
    ),
    "parse_glog" to VRLFunction(
        name = "parse_glog",
        description = "Parses the value using the glog (Google Logging Library) format.",
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
        returnTypes = setOf("object", "error")
    ),
    "parse_grok" to VRLFunction(
        name = "parse_grok",
        description = "Parses the value using the grok format. All patterns listed here are supported.",
        isFallible = true,
        isPure = true,
        arguments = listOf(
            VRLFunctionArgument(
                "value",
                setOf("string"),
                "The string to parse.",
                true
            ),
            VRLFunctionArgument(
                "pattern",
                setOf("string"),
                "The Grok pattern.",
                true
            )
        ),
        returnTypes = setOf("object", "error")
    ),
    "parse_groks" to VRLFunction(
        name = "parse_groks",
        description = "Parses the value using multiple grok patterns. All patterns listed here are supported.",
        isFallible = true,
        isPure = true,
        arguments = listOf(
            VRLFunctionArgument(
                "value",
                setOf("string"),
                "The string to parse.",
                true
            ),
            VRLFunctionArgument(
                "patterns",
                setOf("array"),
                "The Grok patterns, which are tried in order until the first match.",
                true
            ),
            VRLFunctionArgument(
                "aliases",
                setOf("object"),
                "The shared set of grok aliases that can be referenced in the patterns to simplify them.",
                false
            ),
            VRLFunctionArgument(
                "alias_sources",
                setOf("string"),
                "Path to the file containing aliases in a JSON format.",
                false
            )
        ),
        returnTypes = setOf("object", "error")
    ),
    "parse_influxdb" to VRLFunction(
        name = "parse_influxdb",
        description = "Parses the value as an InfluxDB line protocol string, producing a list of Vector-compatible metrics.",
        isFallible = true,
        isPure = true,
        arguments = listOf(
            VRLFunctionArgument(
                "value",
                setOf("string"),
                "The string representation of the InfluxDB line protocol to parse.",
                true
            )
        ),
        returnTypes = setOf("array", "error")
    ),
    "parse_int" to VRLFunction(
        name = "parse_int",
        description = "Parses the string value representing a number in an optional base/radix to an integer.",
        isFallible = true,
        isPure = true,
        arguments = listOf(
            VRLFunctionArgument(
                "value",
                setOf("string"),
                "The string to parse.",
                true
            ),
            VRLFunctionArgument(
                "base",
                setOf("integer"),
                "The base the number is in. Must be between 2 and 36 (inclusive). If unspecified, the string prefix is used to determine the base: \"0b\", 8 for \"0\" or \"0o\", 16 for \"0x\", and 10 otherwise.",
                false
            )
        ),
        returnTypes = setOf("integer", "error")
    ),
    "parse_json" to VRLFunction(
        name = "parse_json",
        description = "Parses the value as JSON.",
        isFallible = true,
        isPure = true,
        arguments = listOf(
            VRLFunctionArgument(
                "value",
                setOf("string"),
                "The string representation of the JSON to parse.",
                true
            ),
            VRLFunctionArgument(
                "max_depth",
                setOf("integer"),
                "Number of layers to parse for nested JSON-formatted documents. The value must be in the range of 1 to 128.",
                false
            ),
            VRLFunctionArgument(
                "lossy",
                setOf("boolean"),
                "Whether to parse the JSON in a lossy manner. Replaces invalid UTF-8 characters with the Unicode character � (U+FFFD) if set to true, otherwise returns an error if there are any invalid UTF-8 characters present.",
                isRequired = false,
                defaultValue = true
            )
        ),
        returnTypes = setOf("boolean", "integer", "float", "string", "object", "array", "null", "error")
    ),
    "parse_key_value" to VRLFunction(
        name = "parse_key_value",
        description = "Parses the value in key-value format. Also known as logfmt. Keys and values can be wrapped with \". \" characters can be escaped using \\.",
        isFallible = true,
        isPure = true,
        arguments = listOf(
            VRLFunctionArgument(
                "value",
                setOf("string"),
                "The string to parse.",
                true
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
                false
            ),
            VRLFunctionArgument(
                "whitespace",
                setOf("string"),
                "Defines the acceptance of unnecessary whitespace surrounding the configured key_value_delimiter.",
                false,
                "lenient"
            ),
            VRLFunctionArgument(
                "accept_standalone_key",
                setOf("boolean"),
                "Whether a standalone key should be accepted, the resulting object associates such keys with the boolean value true.",
                isRequired = false,
                defaultValue = true
            )
        ),
        returnTypes = setOf("object", "error")
    ),
    "parse_klog" to VRLFunction(
        name = "parse_klog",
        description = "Parses the value using the klog format used by Kubernetes components.",
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
        returnTypes = setOf("object", "error")
    ),
    "parse_linux_authorization" to VRLFunction(
        name = "parse_linux_authorization",
        description = "Parses Linux authorization logs usually found under either /var/log/auth.log (for Debian-based systems) or /var/log/secure (for RedHat-based systems) according to Syslog format.",
        isFallible = true,
        isPure = true,
        arguments = listOf(
            VRLFunctionArgument(
                "value",
                setOf("string"),
                "The text containing the message to parse.",
                true
            )
        ),
        returnTypes = setOf("object", "error")
    ),
    "parse_logfmt" to VRLFunction(
        name = "parse_logfmt",
        description = "Parses the value in logfmt. Keys and values can be wrapped using the \" character. \" characters can be escaped by the \\ character. As per this logfmt specification, the parse_logfmt function accepts standalone keys and assigns them a Boolean value of true.",
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
        returnTypes = setOf("object", "error")
    ),
    "parse_nginx_log" to VRLFunction(
        name = "parse_nginx_log",
        description = "Parses Nginx access and error log lines. Lines can be in combined, ingress_upstreaminfo, main or error format.",
        isFallible = true,
        isPure = true,
        arguments = listOf(
            VRLFunctionArgument(
                "value",
                setOf("string"),
                "The string to parse.",
                true
            ),
            VRLFunctionArgument(
                "format",
                setOf("string"),
                "The format to use for parsing the log.",
                true
            ),
            VRLFunctionArgument(
                "timestamp_format",
                setOf("string"),
                "The date/time format to use for encoding the timestamp. The time is parsed in local time if the timestamp doesn't specify a timezone. The default format is %d/%b/%Y:%T %z for combined logs and %Y/%m/%d %H:%M:%S for error logs.",
                false,
                "%d/%b/%Y:%T %z"
            )
        ),
        returnTypes = setOf("object", "error")
    ),
    "parse_proto" to VRLFunction(
        name = "parse_proto",
        description = "Parses the value as a protocol buffer payload.",
        isFallible = true,
        isPure = true,
        arguments = listOf(
            VRLFunctionArgument(
                "value",
                setOf("string"),
                "The protocol buffer payload to parse.",
                true
            ),
            VRLFunctionArgument(
                "desc_file",
                setOf("string"),
                "The path to the protobuf descriptor set file. Must be a literal string. This file is the output of protoc -o ...",
                true
            ),
            VRLFunctionArgument(
                "message_type",
                setOf("string"),
                "The name of the message type to use for serializing. Must be a literal string.",
                true
            )
        ),
        returnTypes = setOf("object", "error")
    ),
    "parse_query_string" to VRLFunction(
        name = "parse_query_string",
        description = "Parses the value as a query string.",
        isFallible = false,
        isPure = true,
        arguments = listOf(
            VRLFunctionArgument(
                "value",
                setOf("string"),
                "The string to parse.",
                true
            )
        ),
        returnTypes = setOf("object")
    ),
    "parse_regex" to VRLFunction(
        name = "parse_regex",
        description = "Parses the value using the provided Regex pattern. This function differs from the parse_regex_all function in that it returns only the first match.",
        isFallible = true,
        isPure = true,
        arguments = listOf(
            VRLFunctionArgument(
                "value",
                setOf("string"),
                "The string to search.",
                true
            ),
            VRLFunctionArgument(
                "pattern",
                setOf("regex"),
                "The regular expression pattern to search against.",
                true
            ),
            VRLFunctionArgument(
                "numeric_groups",
                setOf("boolean"),
                "If true, the index of each group in the regular expression is also captured. Index 0 contains the whole match.",
                false
            )
        ),
        returnTypes = setOf("object", "error")
    ),
    "parse_regex_all" to VRLFunction(
        name = "parse_regex_all",
        description = "Parses the value using the provided Regex pattern. This function differs from the parse_regex function in that it returns all matches, not just the first.",
        isFallible = true,
        isPure = true,
        arguments = listOf(
            VRLFunctionArgument(
                "value",
                setOf("string"),
                "The string to search.",
                true
            ),
            VRLFunctionArgument(
                "pattern",
                setOf("regex"),
                "The regular expression pattern to search against.",
                true
            ),
            VRLFunctionArgument(
                "numeric_groups",
                setOf("boolean"),
                "If true, the index of each group in the regular expression is also captured. Index 0 contains the whole match.",
                false
            )
        ),
        returnTypes = setOf("array", "error")
    ),
    "parse_ruby_hash" to VRLFunction(
        name = "parse_ruby_hash",
        description = "Parses the value as ruby hash.",
        isFallible = true,
        isPure = true,
        arguments = listOf(
            VRLFunctionArgument(
                "value",
                setOf("string"),
                "The string representation of the ruby hash to parse.",
                true
            )
        ),
        returnTypes = setOf("object", "error")
    ),
    "parse_syslog" to VRLFunction(
        name = "parse_syslog",
        description = "Parses the value in Syslog format.",
        isFallible = true,
        isPure = true,
        arguments = listOf(
            VRLFunctionArgument(
                "value",
                setOf("string"),
                "The text containing the Syslog message to parse.",
                true
            )
        ),
        returnTypes = setOf("object", "error")
    ),
    "parse_timestamp" to VRLFunction(
        name = "parse_timestamp",
        description = "Parses the value in strptime format.",
        isFallible = true,
        isPure = true,
        arguments = listOf(
            VRLFunctionArgument(
                "value",
                setOf("string"),
                "The text of the timestamp.",
                true
            ),
            VRLFunctionArgument(
                "format",
                setOf("string"),
                "The strptime format.",
                true
            ),
            VRLFunctionArgument(
                "timezone",
                setOf("string"),
                "The TZ database format. By default, this function parses the timestamp by global timezone option. This argument overwrites the setting and is useful for parsing timestamps without a specified timezone, such as 16/10/2019 12:00:00.",
                false
            )
        ),
        returnTypes = setOf("timestamp", "error")
    ),
    "parse_tokens" to VRLFunction(
        name = "parse_tokens",
        description = "Parses the value in token format. A token is considered to be one of the following: A word surrounded by whitespace. Text delimited by double quotes: \"..\" (quotes can be included in the token if they are escaped by a backslash). Text delimited by square brackets: [..] (closing square brackets can be included in the token if they are escaped by a backslash).",
        isFallible = true,
        isPure = true,
        arguments = listOf(
            VRLFunctionArgument(
                "value",
                setOf("string"),
                "The string to tokenize.",
                true
            )
        ),
        returnTypes = setOf("array", "error")
    ),
    "parse_url" to VRLFunction(
        name = "parse_url",
        description = "Parses the value in URL format.",
        isFallible = true,
        isPure = true,
        arguments = listOf(
            VRLFunctionArgument(
                "value",
                setOf("string"),
                "The text of the URL.",
                true
            ),
            VRLFunctionArgument(
                "default_known_ports",
                setOf("boolean"),
                "If true and the port number is not specified in the input URL string (or matches the default port for the scheme), it is populated from well-known ports for the following schemes: http, https, ws, wss, and ftp.",
                false
            )
        ),
        returnTypes = setOf("object", "error")
    ),
    "parse_user_agent" to VRLFunction(
        name = "parse_user_agent",
        description = "Parses the value as a user agent string, which has a loosely defined format so this parser only provides best effort guarantee.",
        isFallible = false,
        isPure = true,
        arguments = listOf(
            VRLFunctionArgument(
                "value",
                setOf("string"),
                "The string to parse.",
                true
            ),
            VRLFunctionArgument(
                "mode",
                setOf("string"),
                "Determines performance and reliability characteristics.",
                false,
                "fast"
            )
        ),
        returnTypes = setOf("object")
    ),
    "parse_xml" to VRLFunction(
        name = "parse_xml",
        description = "Parses the value as XML.",
        isFallible = true,
        isPure = true,
        arguments = listOf(
            VRLFunctionArgument(
                "value",
                setOf("string"),
                "The string representation of the XML document to parse.",
                true
            ),
            VRLFunctionArgument(
                "include_attr",
                setOf("boolean"),
                "Include XML tag attributes in the returned object.",
                isRequired = false,
                defaultValue = true
            ),
            VRLFunctionArgument(
                "attr_prefix",
                setOf("string"),
                "String prefix to use for XML tag attribute keys.",
                false,
                "@"
            ),
            VRLFunctionArgument(
                "text_key",
                setOf("string"),
                "Key name to use for expanded text nodes.",
                false,
                "text"
            ),
            VRLFunctionArgument(
                "always_use_text_key",
                setOf("boolean"),
                "Always return text nodes as {\"<text_key>\": \"value\"}.",
                false
            ),
            VRLFunctionArgument(
                "parse_bool",
                setOf("boolean"),
                "Parse \"true\" and \"false\" as boolean.",
                isRequired = false,
                defaultValue = true
            ),
            VRLFunctionArgument(
                "parse_null",
                setOf("boolean"),
                "Parse \"null\" as null.",
                isRequired = false,
                defaultValue = true
            ),
            VRLFunctionArgument(
                "parse_number",
                setOf("boolean"),
                "Parse numbers as integers/floats.",
                isRequired = false,
                defaultValue = true
            )
        ),
        returnTypes = setOf("object", "error")
    )
)