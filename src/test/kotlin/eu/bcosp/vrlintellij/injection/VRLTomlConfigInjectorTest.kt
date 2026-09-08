package eu.bcosp.vrlintellij.injection

import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import eu.bcosp.vrlintellij.VRL

class VRLTomlConfigInjectorTest : BasePlatformTestCase() {

    private fun injectedElementAt(text: String, needle: String) = run {
        myFixture.configureByText("vector.toml", text)
        val offset = myFixture.file.text.indexOf(needle)
        InjectedLanguageManager.getInstance(myFixture.project).findInjectedElementAt(myFixture.file, offset)
    }

    fun testInjectsIntoRemapSource() {
        val injected = injectedElementAt(
            "[transforms.parse]\ntype = \"remap\"\nsource = \".foo = 1\"\n",
            ".foo",
        )
        assertNotNull(injected)
        assertEquals(VRL, injected!!.containingFile.language)
    }

    fun testDoesNotInjectSourceOfNonRemapTransform() {
        val injected = injectedElementAt(
            "[transforms.parse]\ntype = \"lua\"\nsource = \"print('hi')\"\n",
            "print",
        )
        assertNull(injected)
    }

    fun testInjectsIntoPlainStringCondition() {
        val injected = injectedElementAt(
            "[transforms.drop]\ntype = \"filter\"\ncondition = \".status == 200\"\n",
            ".status",
        )
        assertNotNull(injected)
        assertEquals(VRL, injected!!.containingFile.language)
    }

    fun testDoesNotInjectTableFormCondition() {
        val injected = injectedElementAt(
            "[transforms.drop]\ntype = \"filter\"\ncondition = { type = \"datadog_search\", source = \"*stack\" }\n",
            "*stack",
        )
        assertNull(injected)
    }

    fun testDoesNotInjectUnrelatedKeys() {
        val injected = injectedElementAt(
            "[transforms.parse]\ntype = \"remap\"\ntimezone = \"UTC\"\n",
            "UTC",
        )
        assertNull(injected)
    }

    fun testInjectsIntoVrlCodecDecodingSourceTableHeaderForm() {
        // The exact shape from https://vector.dev/docs/reference/configuration/sources/http_server/#decoding.vrl.source
        val injected = injectedElementAt(
            "[sources.my_source_id]\ntype = \"http_server\"\naddress = \"0.0.0.0:80\"\n\n" +
                "[sources.my_source_id.decoding]\ncodec = \"vrl\"\n\n" +
                "[sources.my_source_id.decoding.vrl]\nsource = \". |= parse_json!\"\ntimezone = \"America/New_York\"\n",
            "parse_json",
        )
        assertNotNull(injected)
        assertEquals(VRL, injected!!.containingFile.language)
    }

    fun testInjectsIntoVrlCodecSourceInlineTableForm() {
        val injected = injectedElementAt(
            "[sources.my_source_id.decoding]\ncodec = \"vrl\"\nvrl = { source = \". |= parse_json!\" }\n",
            "parse_json",
        )
        assertNotNull(injected)
        assertEquals(VRL, injected!!.containingFile.language)
    }

    fun testInjectsIntoVrlCodecSourceDottedKeyForm() {
        val injected = injectedElementAt(
            "[sources.my_source_id]\ndecoding.codec = \"vrl\"\ndecoding.vrl.source = \". |= parse_json!\"\n",
            "parse_json",
        )
        assertNotNull(injected)
        assertEquals(VRL, injected!!.containingFile.language)
    }

    fun testDoesNotInjectSourceOfNonVrlCodec() {
        val injected = injectedElementAt(
            "[sources.my_source_id.decoding]\ncodec = \"json\"\n\n" +
                "[sources.my_source_id.decoding.json]\nsource = \"not vrl\"\n",
            "not vrl",
        )
        assertNull(injected)
    }

    // The fully-specified condition form, in each of TOML's three spellings for it. All three are
    // accepted by `vector validate` against a real filter transform.
    fun testInjectsIntoVrlConditionInlineTableForm() {
        val injected = injectedElementAt(
            "[transforms.drop]\ntype = \"filter\"\ncondition = { type = \"vrl\", source = \".status == 200\" }\n",
            ".status",
        )
        assertNotNull(injected)
        assertEquals(VRL, injected!!.containingFile.language)
    }

    fun testInjectsIntoVrlConditionDottedKeyForm() {
        val injected = injectedElementAt(
            "[transforms.drop]\ntype = \"filter\"\ncondition.type = \"vrl\"\ncondition.source = \".status == 200\"\n",
            ".status",
        )
        assertNotNull(injected)
        assertEquals(VRL, injected!!.containingFile.language)
    }

    fun testInjectsIntoVrlConditionTableHeaderForm() {
        val injected = injectedElementAt(
            "[transforms.drop]\ntype = \"filter\"\n\n[transforms.drop.condition]\ntype = \"vrl\"\nsource = \".status == 200\"\n",
            ".status",
        )
        assertNotNull(injected)
        assertEquals(VRL, injected!!.containingFile.language)
    }

    // Regression guard for the dotted form's prefix matching: `condition.source` has to pair with
    // `condition.type`, not with the enclosing transform's own `type = "filter"` sitting in the
    // same table. Matching the first `type` entry regardless of prefix would read "filter" here.
    fun testDottedConditionSourcePairsWithConditionTypeNotComponentType() {
        val injected = injectedElementAt(
            "[transforms.drop]\ntype = \"filter\"\ncondition.type = \"datadog_search\"\ncondition.source = \"*stack\"\n",
            "*stack",
        )
        assertNull(injected)
    }

    // `type = "vrl"` identifies a condition wherever it appears, so the other condition-shaped
    // fields (`starts_when`/`ends_when`/`exclude`/`flush_when`/`forward_when`, and the route
    // transform's arbitrarily-named outputs) come along without naming any of them.
    fun testInjectsIntoVrlConditionUnderReduceStartsWhen() {
        val injected = injectedElementAt(
            "[transforms.r]\ntype = \"reduce\"\nstarts_when = { type = \"vrl\", source = \".start == true\" }\n",
            ".start",
        )
        assertNotNull(injected)
        assertEquals(VRL, injected!!.containingFile.language)
    }

    fun testInjectsIntoVrlConditionUnderArbitrarilyNamedRouteOutput() {
        val injected = injectedElementAt(
            "[transforms.r]\ntype = \"route\"\nroute.important = { type = \"vrl\", source = \".severity == \\\"high\\\"\" }\n",
            ".severity",
        )
        assertNotNull(injected)
        assertEquals(VRL, injected!!.containingFile.language)
    }
}
