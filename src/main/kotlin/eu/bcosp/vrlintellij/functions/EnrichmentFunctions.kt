package eu.bcosp.vrlintellij.functions

val enrichmentFunctions = mapOf(
    "find_enrichment_table_records" to VRLFunction(
        name = "find_enrichment_table_records",
        description = "Searches an enrichment table for rows that match the provided condition.\nTo use this function, you need to update your configuration to include an enrichment_tables parameter.",
        isFallible = false,
        isPure = true,
        arguments = listOf(
            VRLFunctionArgument(
                "table",
                setOf("string"),
                "The enrichment table to search.",
                true
            ),
            VRLFunctionArgument(
                "condition",
                setOf("object"),
                "The condition to search on. Since the condition is used at boot time to create indices into the data, these conditions must be statically defined.",
                true
            ),
            VRLFunctionArgument(
                "select",
                setOf("array"),
                "A subset of fields from the enrichment table to return. If not specified, all fields are returned.",
                false,
            ),
            VRLFunctionArgument(
                "case_sensitive",
                setOf("boolean"),
                "Whether text fields need to match cases exactly.",
                isRequired = false,
                defaultValue = true
            )
        ),
        returnTypes = setOf("array")
    ),
    "get_enrichment_table_record" to VRLFunction(
        name = "get_enrichment_table_record",
        description = "Searches an enrichment table for a row that matches the provided condition. A single row must be matched. If no rows are found or more than one row is found, an error is returned.\nTo use this function, you need to update your configuration to include an enrichment_tables parameter.",
        isFallible = true,
        isPure = true,
        arguments = listOf(
            VRLFunctionArgument(
                "table",
                setOf("string"),
                "The enrichment table to search.",
                true
            ),
            VRLFunctionArgument(
                "condition",
                setOf("object"),
                "The condition to search on. Since the condition is used at boot time to create indices into the data, these conditions must be statically defined.",
                true
            ),
            VRLFunctionArgument(
                "select",
                setOf("array"),
                "A subset of fields from the enrichment table to return. If not specified, all fields are returned.",
                false,
            ),
            VRLFunctionArgument(
                "case_sensitive",
                setOf("boolean"),
                "Whether the text fields match the case exactly.",
                isRequired = false,
                defaultValue = true
            ),
        ),
        returnTypes = setOf("object", "error")
    )
)