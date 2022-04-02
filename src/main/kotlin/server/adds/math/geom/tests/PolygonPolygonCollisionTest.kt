package server.adds.math.geom.tests

import javafx.scene.paint.Color
import server.adds.debug.*
import server.adds.math.CrazyVector
import server.adds.math.geom.shapes.CrazyPolygon
import server.adds.math.vec

class PolygonPolygonCollisionTest : CrazyDebugger(
    GeomDebuggerConfig(
        unit = 100.0,
        eyeModule = TransformEyeModuleConfig(),
        timerModule = TimerModuleConfig(startStepSpeed = 50),
        inspectorModule = DebugObjectModuleConfig()
    )
) {
    override suspend fun act(s: Double): List<DebugObjectI> {
        val polygon = CrazyPolygon(
            listOf(
                vec(-1, 0),
                vec(2, 1),
                vec(3, -1),
                vec (4, 2),
                vec(3, 4),
                vec(1, 1)
            )
        )

        val polygon2 = CrazyPolygon(
            listOf(
                vec(-1, 0),
                vec(2, 1),
                vec(3, -1),
                vec (4, 2),
                vec(3, 4),
                vec(1, 1)
            )
        ).convert { it * 2 + (getMousePos() ?: CrazyVector.zero()) }

        val collides = polygon collides polygon2

        return listOf(
            polygon.setColor(if (collides) Color.RED else Color.YELLOW) as CrazyPolygon,
            polygon2
        )
    }
}