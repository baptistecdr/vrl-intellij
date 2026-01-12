package eu.bcosp.vrlintellij.functions

val metricsFunctions = mapOf(
    "aggregate_vector_metric" to VRLFunction(
        name = "aggregate_vector_metric",
        description = "Aggregates internal Vector metrics, using one of 4 aggregation functions, filtering by name and optionally by tags. Returns the aggregated value. Only includes counter and gauge metrics.",
        isFallible = false,
        isPure = true,
        arguments = listOf(
            VRLFunctionArgument(
                "function",
                setOf("string"),
                "The metric name to search.",
                true
            ),
            VRLFunctionArgument(
                "key",
                setOf("string"),
                "The metric name to aggregate.",
                true
            ),
            VRLFunctionArgument(
                "tags",
                setOf("object"),
                "Tags to filter the results on. Values in this object support wildcards (’*’) to match on parts of the tag value.",
                false
            ),
        ),
        returnTypes = setOf("float")
    ),
    "find_vector_metrics" to VRLFunction(
        name = "find_vector_metrics",
        description = "Searches internal Vector metrics by name and optionally by tags. Returns all matching metrics.",
        isFallible = false,
        isPure = true,
        arguments = listOf(
            VRLFunctionArgument(
                "key",
                setOf("string"),
                "The metric name to search.",
                true
            ),
            VRLFunctionArgument(
                "tags",
                setOf("object"),
                "Tags to filter the results on. Values in this object support wildcards (’*’) to match on parts of the tag value.",
                false
            ),
        ),
        returnTypes = setOf("array")
    ),
    "get_vector_metric" to VRLFunction(
        name = "get_vector_metric",
        description = "Searches internal Vector metrics by name and optionally by tags. Returns the first matching metric.",
        isFallible = false,
        isPure = true,
        arguments = listOf(
            VRLFunctionArgument(
                "key",
                setOf("string"),
                "The metric name to search.",
                true
            ),
            VRLFunctionArgument(
                "tags",
                setOf("object"),
                "Tags to filter the results on. Values in this object support wildcards (’*’) to match on parts of the tag value.",
                false
            ),
        ),
        returnTypes = setOf("object")
    )
)