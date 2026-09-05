package eu.bcosp.vrlintellij.playground

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.RoamingType
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.project.Project

/**
 * Remembers the sample event typed into the VRL Playground for each `.vrl` file (keyed by file
 * URL, so it survives across editor tabs and IDE restarts) - otherwise switching tabs to check
 * another script would silently wipe out the event the user was testing with.
 */
@Service(Service.Level.PROJECT)
@State(name = "VRLPlaygroundState", storages = [Storage(value = "vrl-playground.xml", roamingType = RoamingType.DISABLED)])
class VRLPlaygroundState : PersistentStateComponent<VRLPlaygroundState.State> {

    class State {
        var sampleEventsByFileUrl: MutableMap<String, String> = mutableMapOf()
    }

    private var state = State()

    override fun getState(): State = state

    override fun loadState(state: State) {
        this.state = state
    }

    fun sampleEventFor(fileUrl: String): String = state.sampleEventsByFileUrl[fileUrl] ?: ""

    fun setSampleEventFor(fileUrl: String, event: String) {
        if (event.isBlank()) {
            state.sampleEventsByFileUrl.remove(fileUrl)
        } else {
            state.sampleEventsByFileUrl[fileUrl] = event
        }
    }

    companion object {
        fun getInstance(project: Project): VRLPlaygroundState =
            project.getService(VRLPlaygroundState::class.java)
    }
}
