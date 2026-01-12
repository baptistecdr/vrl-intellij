package eu.bcosp.vrlintellij.functions

val mapFunctions = mapOf(
    "haversine" to VRLFunction(
        name = "haversine",
        description = "Calculates haversine distance and bearing between two points. Results are available in kilometers or miles.",
        isFallible = false,
        isPure = true,
        arguments = listOf(
            VRLFunctionArgument(
                "latitude1",
                setOf("float"),
                "Latitude of the first point.",
                true
            ),
            VRLFunctionArgument(
                "longitude1",
                setOf("float"),
                "Longitude of the first point.",
                true
            ),
            VRLFunctionArgument(
                "latitude2",
                setOf("float"),
                "Latitude of the second point.",
                true
            ),
            VRLFunctionArgument(
                "longitude2",
                setOf("float"),
                "Longitude of the second point.",
                true
            ),
            VRLFunctionArgument(
                "measurement",
                setOf("string"),
                "Measurement system to use for resulting distance.",
                false,
                "kilometers"
            )
        ),
        returnTypes = setOf("object")
    )
)