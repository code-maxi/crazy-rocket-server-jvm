package server.adds.math.geom.tests

import javafx.scene.paint.Color
import server.adds.debug.*
import server.adds.math.geom.shapes.CrazyCircle
import server.adds.math.geom.shapes.CrazyLine
import server.adds.math.geom.shapes.ShapeDebugConfig
import server.adds.math.niceString
import server.adds.math.vec
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sqrt

class Test1dPhysics : CrazyDebugger(
    CrazyDebuggerConfig(
    title = "1D-Physics-Debugger",
    eyeModule = TransformEyeModuleConfig(),
    timerModule = TimerModuleConfig(startStepSpeed = 50),
    gridModule = GridModuleConfig(10.0, true),
    inspectorModule = InspectorModuleConfig(),
    unit = 20.0
)) {
    private var leftSide = -15.0
    private var rightSide = 15.0

    private var m1 = 10.0
    private var v1 = 0.3
    private var p1 = rightSide

    private var m2 = 3.0
    private var v2 = -0.5
    private var p2 = leftSide

    override suspend fun act(s: Double): List<DebugObjectI> {
        val radius1 = sqrt(m1 / PI)
        val radius2 = sqrt(m2 / PI)

        if (p1 < leftSide + radius1) v1 = abs(v1)
        if (p1 > rightSide - radius1) v1 = -abs(v1)

        if (p2 < leftSide + radius2) v2 = abs(v2)
        if (p2 > rightSide - radius2) v2 = -abs(v2)

        p1 += v1 * s
        p2 += v2 * s

        val circle1 = CrazyCircle(radius1, vec(p1, 0), ShapeDebugConfig(
            crazyStyle = ShapeDebugConfig.DEFAULT_CRAZY_STYLE.copy(fillColor = Color.RED),
            debugOptions = DebugObjectOptions("Circle 1 (m = ${m1.niceString()})", "circle-1", mapOf(
                "Mass" to m1.niceString(),
                "X-Pos" to p1.niceString(),
                "X-Velocity" to v1.niceString(),
                "Radius" to radius1.niceString()
            ))
        ))

        val circle2 = CrazyCircle(radius2, vec(p2, 0), ShapeDebugConfig(
            crazyStyle = ShapeDebugConfig.DEFAULT_CRAZY_STYLE.copy(fillColor = Color.BLUE),
            debugOptions = DebugObjectOptions("Circle 2 (m = ${m2.niceString()})", "circle-2", mapOf(
                "Mass" to m2.niceString(),
                "X-Pos" to p2.niceString(),
                "X-Velocity" to v2.niceString(),
                "Radius" to radius2.niceString()
            ))
        ))

        if (circle1 collides circle2) {
            val f = (m1*v1 + m2*v2) / (m1 + m2)
            v1 = 2.0 * f - v1
            v2 = 2.0 * f - v2
        }

        return listOf(CrazyLine(vec(leftSide, -1), vec(leftSide, 1)), CrazyLine(vec(rightSide, -1), vec(rightSide, 1)), circle1, circle2)
    }
}