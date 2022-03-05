package server.adds.math.geom.tests

import javafx.scene.paint.Color
import server.adds.math.CrazyVector
import server.adds.math.geom.debug.GeomDebugger
import server.adds.math.geom.debug.GeomDebuggerConfig
import server.adds.math.geom.debug.GeomDebuggerObject
import server.adds.math.geom.shapes.CrazyCircle
import server.adds.math.geom.shapes.CrazyLine
import server.adds.math.geom.shapes.ShapeDebugConfig
import server.adds.math.vec
import tornadofx.launch

class GeomTest : GeomDebugger(GeomDebuggerConfig()) {
    var circlePos = CrazyVector.zero()

    override fun act(s: Double): Array<GeomDebuggerObject> {
        //circlePos += vec(1.0, 1.0) * s
        //getMouse()?.let { circlePos = it }

        return arrayOf(
            CrazyCircle(
                50.0, circlePos,
                ShapeDebugConfig(
                    paintCoords = true,
                    paintSurroundedRect = true,
                    name ="Circle"
                )
            ),
            CrazyLine(
                vec(100.0, 100.0),
                vec(300.0, -200.0),
                ShapeDebugConfig(
                    paintCoords = true,
                    paintSurroundedRect = true,
                    name ="Line",
                    color = Color.BLUE
                )
            )
        )
    }
}