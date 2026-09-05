package eu.bcosp.vrlintellij.playground

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage

/**
 * Where to find the `vector` executable, used by the VRL Playground tool window to run a script
 * against a sample event via its `vector vrl` subcommand (https://vector.dev/docs/reference/cli/#vrl).
 * Application-level rather than per-project since the binary is a machine-wide tool, not a
 * project dependency.
 */
@Service(Service.Level.APP)
@State(name = "VRLPlaygroundSettings", storages = [Storage("vrl-playground.xml")])
class VRLPlaygroundSettings : PersistentStateComponent<VRLPlaygroundSettings.State> {

    class State {
        var vectorBinaryPath: String = "vector"
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

    companion object {
        fun getInstance(): VRLPlaygroundSettings =
            ApplicationManager.getApplication().getService(VRLPlaygroundSettings::class.java)
    }
}
