package server.adds.math.geom.tests

import javafx.scene.paint.Color
import server.adds.math.CrazyVector
import server.adds.math.debugString
import server.adds.math.geom.debug.*
import server.adds.math.geom.shapes.CrazyCircle
import server.adds.math.geom.shapes.CrazyLine
import server.adds.math.geom.shapes.ShapeDebugConfig
import server.adds.math.vec

class LineCircleCollisionTest : CrazyDebugger(
    GeomDebuggerConfig(
        unit = 100.0,
        transformEyeModule = TransformEyeModuleConfig(),
        timerModule = TimerModuleConfig(startStepSpeed = 20),
        debugObjectModule = DebugObjectModuleConfig()
    )
) {
    override suspend fun act(s: Double): Array<DebugObjectI> {
        val line = CrazyLine(CrazyVector.zero(), vec(3, 7))
        var circle = CrazyCircle(1.0, getMouse() ?: CrazyVector.zero())

        val line2 = CrazyLine(circle.pos, circle.pos + line.delta().normalRight().e()).drawAsVector()
        val ints = line intersection line2

        if (circle collides line) circle = circle.setCrazyStyle(ShapeDebugConfig.DEFAULT_CRAZY_STYLE.copy(fillColor = Color.RED)) as CrazyCircle

        val objectOptionsItems = mapOf(
            "Intersection Point" to ints.intersection.niceString(),
            "Intersection Factor 1" to ints.factor1.debugString(),
            "Intersection Factor 2" to ints.factor2.debugString(),
            "Intersection On Line 1" to ints.onLine1.toString(),
            "Intersection On Line 2" to ints.onLine2.toString()
        )

        return arrayOf(
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