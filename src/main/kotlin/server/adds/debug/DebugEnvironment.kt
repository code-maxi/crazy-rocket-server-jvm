package server.adds.debug

import javafx.stage.Stage
import tornadofx.App

/*
class DebugEnvironment : App() {
    override fun start(stage: Stage) {
        super.start(stage)
        environment = this

        println(debuggerQueue.size)

        for (debugger in debuggerQueue) registerDebugger(debugger)
        debuggerQueue.clear()
    }
    fun registerDebugger(debugger: CrazyDebugger) {
        println("Registering Debugger...")

        val stage = Stage()
        stage.scene = debugger.initialize()
        stage.title = debugger.configData.title
        stage.show()
    }
    companion object {
        var environment: DebugEnvironment? = null
        var debuggerQueue = arrayListOf<CrazyDebugger>()

        fun startEnvironment() {
            tornadofx.launch<DebugEnvironment>()
        }
        fun registerDebugger(debugger: CrazyDebugger) {
            println("Registering debugger in companion...")

            environment?.registerDebugger(debugger)
                ?: run { debuggerQueue.add(debugger) }
        }
    }
}
*/