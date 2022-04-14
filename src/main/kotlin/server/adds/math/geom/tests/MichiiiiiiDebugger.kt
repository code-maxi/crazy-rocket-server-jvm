package server.adds.math.geom.tests

import javafx.scene.paint.Color
import server.adds.CrazyGraphicStyle
import server.adds.debug.*
import server.adds.math.CrazyVector
import server.adds.math.geom.shapes.CrazyCircle
import server.adds.math.geom.shapes.CrazyLine
import server.adds.math.vec

class MichiiiiiiDebugger : CrazyDebugger(GeomDebuggerConfig(
    "MichiiiiiiDebugger",
    50.0,
    eyeModule = TransformEyeModuleConfig(),
    gridModule = GridModuleConfig(5.0, true),
    inspectorModule = InspectorModuleConfig(),
    timerModule = TimerModuleConfig()
)) {
    var position = CrazyVector(1.0, 1.0)
    var i=vec(0.05, 0.03)

    override suspend fun act(s: Double): List<DebugObjectI> {
        val mouse = getMousePos()
        position += i
        var circle = CrazyCircle(1.0, position)
        var circle2 = CrazyCircle(2.0, mouse ?: vec(0,0))

        val collision = (circle.pos - circle2.pos).length() <= circle.radius + circle2.radius

        if (collision) {
            circle = circle.setColor(Color.RED)
            circle2 = circle2.setColor(Color.RED)
        }

        circle = circle.setDebugConfig(
            DebugObjectOptions(
                "Kreis 1", "kreis-1",
                mapOf("collision?" to collision.toString())
            )
        )

        return listOf(circle, circle2, CrazyLine(position, position + i * 40).drawAsVector())
    }
}