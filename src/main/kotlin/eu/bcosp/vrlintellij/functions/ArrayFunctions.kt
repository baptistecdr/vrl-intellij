package eu.bcosp.vrlintellij.functions

val arrayFunctions = mapOf(
    "append" to VRLFunction(
        name = "append",
        description = "Appends each item in the items array to the end of the value array.",
        isFallible = false,
        isPure = true,
        arguments = listOf(
            VRLFunctionArgument("value", setOf("array"), "The initial array.", true),
            VRLFunctionArgument("items", setOf("array"), "The items to append.", true)
        ),
        returnTypes = setOf("array")
    ),
    "chunks" to VRLFunction(
        name = "chunks",
        description = "Chunks value into slices of length chunk_size bytes.",
        isFallible = true,
        isPure = true,
        arguments = listOf(
            VRLFunctionArgument("value", setOf("array", "string"), "The array of bytes to split.", true),
            VRLFunctionArgument("size", setOf("int"), "The size of each chunk.", true)
        ),
        returnTypes = setOf("array", "error")
    ),
    "pop" to VRLFunction(
        name = "pop",
        description = "Removes the last item from the value array.",
        isFallible = false,
        isPure = true,
        arguments = listOf(
            VRLFunctionArgument("value", setOf("array"), "The target array.", true)
        ),
        returnTypes = setOf("array")
    ),
    "push" to VRLFunction(
        name = "push",
        description = "Adds the item to the end of the value array.",
        isFallible = false,
        isPure = true,
        arguments = listOf(
            VRLFunctionArgument("value", setOf("array"), "The target array.", true),
            VRLFunctionArgument("item", setOf("any"), "The item to push.", true)
        ),
        returnTypes = setOf("array")
    ),
    "zip" to VRLFunction(
        name = "zip",
        description = "Iterate over several arrays in parallel, producing a new array containing arrays of items from each source. The resulting array will be as long as the shortest input array, with all the remaining elements dropped. This function is modeled from the zip function in Python, but similar methods can be found in Ruby and Rust.\nIf a single parameter is given, it must contain an array of all the input arrays.",
        isFallible = true,
        isPure = true,
        arguments = listOf(
            VRLFunctionArgument(
                "array_0",
                setOf("array"),
                "The first array of elements, or the array of input arrays if no other parameter is present.",
                true
            ),
            VRLFunctionArgument(
                "array_1",
                setOf("array"),
                "The second array of elements. If not present, the first parameter contains all the arrays.",
                false
            )
        ),
        returnTypes = setOf("array", "error")
    )
)