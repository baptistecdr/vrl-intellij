package eu.bcosp.vrlintellij.playground

import org.junit.Assert.assertEquals
import org.junit.Test

class VRLJsonFormatterTest {

    @Test
    fun `formats a flat object`() {
        val result = VRLJsonFormatter.prettyPrint("""{"a":1,"b":"x"}""")
        assertEquals(
            """
            {
              "a": 1,
              "b": "x"
            }
            """.trimIndent(),
            result,
        )
    }

    @Test
    fun `formats nested objects and arrays`() {
        val result = VRLJsonFormatter.prettyPrint("""{"a":[1,2],"b":{"c":true}}""")
        assertEquals(
            """
            {
              "a": [
                1,
                2
              ],
              "b": {
                "c": true
              }
            }
            """.trimIndent(),
            result,
        )
    }

    @Test
    fun `leaves empty objects and arrays on one line`() {
        assertEquals("{}", VRLJsonFormatter.prettyPrint("{}"))
        assertEquals("[]", VRLJsonFormatter.prettyPrint("[]"))
    }

    @Test
    fun `does not treat characters inside strings as structure`() {
        val result = VRLJsonFormatter.prettyPrint("""{"a":"{ } [ ] , :"}""")
        assertEquals(
            """
            {
              "a": "{ } [ ] , :"
            }
            """.trimIndent(),
            result,
        )
    }

    @Test
    fun `handles escaped quotes inside strings`() {
        val result = VRLJsonFormatter.prettyPrint("""{"a":"say \"hi\""}""")
        assertEquals(
            """
            {
              "a": "say \"hi\""
            }
            """.trimIndent(),
            result,
        )
    }

    @Test
    fun `passes through blank input unchanged`() {
        assertEquals("", VRLJsonFormatter.prettyPrint(""))
        assertEquals("", VRLJsonFormatter.prettyPrint("   "))
    }

    @Test
    fun `formats a bare scalar`() {
        assertEquals("42", VRLJsonFormatter.prettyPrint("42"))
        assertEquals("\"hello\"", VRLJsonFormatter.prettyPrint("\"hello\""))
    }
}
