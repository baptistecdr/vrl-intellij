package eu.bcosp.vrlintellij.playground

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage

/**
 * Where to find the `vector` executable and whether to use it for live diagnostics, both shared
 * by the two features built on `vector vrl` (https://vector.dev/docs/reference/cli/#vrl): the VRL
 * Playground tool window (run a script against a sample event) and
 * [eu.bcosp.vrlintellij.diagnostics.VRLExternalAnnotator] (real compiler errors as you type).
 * Application-level rather than per-project since the binary is a machine-wide tool, not a
 * project dependency.
 */
@Service(Service.Level.APP)
@State(name = "VRLPlaygroundSettings", storages = [Storage("vrl-playground.xml")])
class VRLPlaygroundSettings : PersistentStateComponent<VRLPlaygroundSettings.State> {

    class State {
        var vectorBinaryPath: String = "vector"

        // Opt-in, not opt-out: unlike the Playground (only ever runs when the user presses Run),
        // this spawns a `vector` process automatically on every edit once turned on - something a
        // user should choose deliberately in Settings rather than discover happening on its own.
        var externalDiagnosticsEnabled: Boolean = false
    }

    private var state = State()

    override fun getState(): State = state

    override fun loadState(state: State) {
        this.state = state
    }

    var vectorBinaryPath: String
        get() = state.vectorBinaryPath
        set(value) {
            state.vectorBinaryPath = value
        }

    var externalDiagnosticsEnabled: Boolean
        get() = state.externalDiagnosticsEnabled
        set(value) {
            state.externalDiagnosticsEnabled = value
        }

    companion object {
        fun getInstance(): VRLPlaygroundSettings =
            ApplicationManager.getApplication().getService(VRLPlaygroundSettings::class.java)
    }
}
