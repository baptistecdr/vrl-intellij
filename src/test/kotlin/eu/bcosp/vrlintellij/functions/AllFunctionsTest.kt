package eu.bcosp.vrlintellij.functions

import junit.framework.TestCase

class AllFunctionsTest : TestCase() {

    private val perCategory = listOf(
        "array" to arrayFunctions,
        "checksum" to checksumFunctions,
        "codec" to codecFunctions,
        "coerce" to coerceFunctions,
        "convert" to convertFunctions,
        "cryptography" to cryptographyFunctions,
        "debug" to debugFunctions,
        "enrichment" to enrichmentFunctions,
        "enumerate" to enumerateFunctions,
        "event" to eventFunctions,
        "ip" to ipFunctions,
        "map" to mapFunctions,
        "metrics" to metricsFunctions,
        "number" to numberFunctions,
        "object" to objectFunctions,
        "parse" to parseFunctions,
        "path" to pathFunctions,
        "random" to randomFunctions,
        "string" to stringFunctions,
        "system" to systemFunctions,
        "timestamp" to timestampFunctions,
        "type" to typeFunctions,
    )

    fun testNoDuplicateFunctionNamesAcrossCategories() {
        val seenIn = mutableMapOf<String, String>()
        for ((category, functions) in perCategory) {
            for (name in functions.keys) {
                val existing = seenIn[name]
                assertNull("'$name' is declared in both '$existing' and '$category'", existing)
                seenIn[name] = category
            }
        }
    }

    fun testAllFunctionsIsExactlyTheUnionOfEveryCategory() {
        val union = perCategory.flatMap { it.second.keys }.toSet()
        assertEquals(union, allFunctions.keys)
        assertEquals(union.size, allFunctions.size)
    }

    fun testEveryMapKeyMatchesItsFunctionName() {
        for ((key, function) in allFunctions) {
            assertEquals("map key must match VRLFunction.name", key, function.name)
        }
    }

    fun testNoBlankDescriptionsOrMissingReturnTypes() {
        for ((name, function) in allFunctions) {
            assertTrue("$name has a blank description", function.description.isNotBlank())
            assertTrue("$name declares no return types", function.returnTypes.isNotEmpty())
        }
    }

    fun testArgumentsAreWellFormed() {
        for ((name, function) in allFunctions) {
            val argNames = function.arguments.map { it.name }
            assertEquals("$name has duplicate argument names", argNames.distinct(), argNames)
            for (arg in function.arguments) {
                assertTrue("$name argument has a blank name", arg.name.isNotBlank())
                assertTrue("$name.${arg.name} declares no types", arg.types.isNotEmpty())
                assertTrue("$name.${arg.name} has a blank description", arg.description.isNotBlank())
            }
        }
    }

    fun testExamplesAreWellFormed() {
        // Not asserting a non-blank `result`: vector.dev itself renders an empty "Return" code
        // block for at least one real example (get's "Returns null for unknown field", whose return
        // value - `null` - the page's syntax highlighter renders as nothing), so an empty result
        // mirrors real upstream content rather than indicating a parsing bug.
        for ((name, function) in allFunctions) {
            for (example in function.examples) {
                assertTrue("$name has an example with a blank title", example.title.isNotBlank())
                assertTrue("$name.'${example.title}' has a blank source", example.source.isNotBlank())
            }
        }
    }

    fun testRequiredArgumentsPrecedeNoOptionalOnesTheyDependOn() {
        // Not a VRL requirement, but a sanity check that required args aren't accidentally
        // marked optional (or vice versa) by checking every required arg has no default value.
        for ((name, function) in allFunctions) {
            for (arg in function.arguments) {
                if (arg.isRequired) {
                    assertNull("$name.${arg.name} is required but has a default value", arg.defaultValue)
                }
            }
        }
    }
}
