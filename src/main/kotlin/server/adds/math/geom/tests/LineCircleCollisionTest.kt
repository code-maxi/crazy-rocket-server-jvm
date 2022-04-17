package server.adds.math.geom.tests

import javafx.scene.paint.Color
import server.adds.math.CrazyVector
import server.adds.math.niceString
import server.adds.debug.*
import server.adds.math.geom.shapes.CrazyCircle
import server.adds.math.geom.shapes.CrazyLine
import server.adds.math.geom.shapes.ShapeDebugConfig
import server.adds.math.vec

class LineCircleCollisionTest : CrazyDebugger(
    CrazyDebuggerConfig(
        unit = 100.0,
        eyeModule = TransformEyeModuleConfig(),
        timerModule = TimerModuleConfig(startStepSpeed = 20),
        inspectorModule = InspectorModuleConfig()
    )
) {
    override suspend fun act(s: Double): List<DebugObjectI> {
        val line = CrazyLine(CrazyVector.zero(), vec(3, 7))
        var circle = CrazyCircle(1.0, getMousePos() ?: CrazyVector.zero())

        val line2 = CrazyLine(circle.pos, circle.pos + line.delta().normalRight().e()).drawAsVector()
        val ints = line intersection line2

        if (circle collides line) circle = circle.setCrazyStyle(ShapeDebugConfig.DEFAULT_CRAZY_STYLE.copy(fillColor = Color.RED)) as CrazyCircle

        val objectOptionsItems = mapOf(
            "Intersection Point" to ints.intersection.niceString(),
            "Intersection Factor 1" to ints.factor1.niceString(),
            "Intersection Factor 2" to ints.factor2.niceString(),
            "Intersection On Line 1" to ints.onLine1.toString(),
            "Intersection On Line 2" to ints.onLine2.toString()
        )

        return listOf(
            line2,

            CrazyDebugVector(ints.intersection, CrazyDebugVectorOptions(size = 15.0, extraName = "I")),

            circle.setDebugConfig(
                DebugObjectOptions(
                    "Circle", "circle",
                    objectOptionsItems
                )
            ),

            line.setDebugConfig(
                DebugObjectOptions(
                    "Line", "line",
                    objectOptionsItems
                )
            )
        )
    }
}