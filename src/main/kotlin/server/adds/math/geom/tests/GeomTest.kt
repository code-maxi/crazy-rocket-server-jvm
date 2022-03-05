package server.adds.math.geom.tests

import server.adds.math.geom.debug.GeomDebugger
import server.adds.math.geom.debug.GeomDebuggerConfig
import server.adds.math.geom.debug.GeomDebuggerObject
import tornadofx.launch

class GeomTest : GeomDebugger(GeomDebuggerConfig()) {


    override fun act(): Array<GeomDebuggerObject> {

    }

    override fun startDebugger() { launch<GeomTest>() }
}