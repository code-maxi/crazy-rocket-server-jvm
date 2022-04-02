package server.adds.math.geom.tests

import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.input.MouseButton
import javafx.scene.input.MouseEvent
import javafx.scene.paint.Color
import server.adds.math.CrazyVector
import server.adds.math.debugString
import server.adds.debug.*
import server.adds.math.geom.shapes.CrazyCircle
import server.adds.math.geom.shapes.CrazyLine
import server.adds.math.geom.shapes.ShapeDebugConfig
import server.adds.math.toDegrees
import server.adds.math.vec

class CircleRicochetOnLineTest : CrazyDebugger(
    GeomDebuggerConfig(
        unit = 100.0,
        eyeModule = TransformEyeModuleConfig(),
        timerModule = TimerModuleConfig(startStepSpeed = 50),
        inspectorModule = DebugObjectModuleConfig()
    )
) {
    private var circlePos: CrazyVector? = null
    private var circleVelocity: CrazyVector? = null
    private val circleRadius = 2.0

    override suspend fun act(s: Double): List<DebugObjectI> {
        val cPos = circlePos
        val cVelocity = circleVelocity?.times(s)
        var line = CrazyLine(vec(5, -2), vec(4, 4))

        val circle = cPos?.let { CrazyCircle(circleRadius, it) }

        if (cPos != null && cVelocity != null && circle != null) {

            val line2 = line normalLineFrom circle.pos
            val ints = line intersection line2

            val cpa = circle containsPoint line.a
            val cpb = circle containsPoint line.b

            val posRightOfThat = line isPointRight circle.pos

            val lineVec = line.toVec()

            val velocityRightOfThat = lineVec.normalRight() scalar cVelocity > 0
            val velocityRemoving = velocityRightOfThat == posRightOfThat
            val velocityRightOfNormal = -lineVec scalar cVelocity > 0

            val angleFac = (if (posRightOfThat) -1 else 1) * (if (velocityRightOfNormal) -1 else 1)
            val angle = (line2.toVec() angleTo cVelocity) * angleFac

            //if (!velocityRemoving) {
                if (cpa) {
                    circleVelocity = cVelocity.ricochetMyVelocity((circle.pos - line.a).normalRight(), false)
                    line = line.setCrazyStyle(ShapeDebugConfig.DEFAULT_CRAZY_STYLE.copy(fillColor = Color.YELLOW)) as CrazyLine
                }
                else if (cpb) {
                    circleVelocity = cVelocity.ricochetMyVelocity((circle.pos - line.b).normalRight(), false)
                    line = line.setCrazyStyle(ShapeDebugConfig.DEFAULT_CRAZY_STYLE.copy(fillColor = Color.GREEN)) as CrazyLine
                }
                else {
                    val orthogonalTouch = ints.onLine1 && circle containsPoint ints.intersection

                    if (orthogonalTouch && !velocityRemoving) {

                        line = line.setCrazyStyle(ShapeDebugConfig.DEFAULT_CRAZY_STYLE.copy(fillColor = Color.RED)) as CrazyLine

                        //circleVelocity = (-cVelocity) rotate (2 * angle * (if (wrongWay) -1 else 1))
                        circleVelocity = cVelocity.ricochetMyVelocity(line.toVec(), posRightOfThat)
                        //circleVelocity = -cVelocity rotate (2 * angle)
                    }
                }
            //}

            circlePos = circlePos?.plus(cVelocity)

            val debugOptions = mapOf(
                "Contains Point A" to cpa.toString(),
                "Contains Point B" to cpb.toString(),
                "On Line" to ints.onLine1.toString(),
                "Velocity Angle vs. Normal Angle" to (line2.toVec() angleTo cVelocity).toDegrees().debugString(),
                "Is Pos Right" to posRightOfThat.toString(),
                "Is Vel Right Of Line" to velocityRightOfThat.toString(),
                "Is Vel Right of Normal" to velocityRightOfNormal.toString(),
                "Is Object Removing" to velocityRemoving.toString(),
                "Angle-Fac" to angleFac.toString(),
                "Angle" to angle.toDegrees().debugString()
            )


            return listOf(
                circle.setDebugConfig(
                    DebugObjectOptions(
                        "Circle", "circle",
                        debugOptions
                    )
                ),
                (cVelocity*20).toLine(circle.pos).drawAsVector(),
                line2.drawAsVector(Color.BLACK),
                CrazyDebugVector(ints.intersection, CrazyDebugVectorOptions(size = 15.0, extraName = "I")),
                line.setDebugConfig(
                    DebugObjectOptions(
                        "Line", "line",
                        debugOptions
                    )
                ),
                line.rightLine().drawAsVector(Color.GREEN),
            )
        }

        return if (circle != null) listOf(line, circle) else listOf(line)
    }

    override fun mouseEvent(it: MouseEvent, type: MouseEventType) {
        super.mouseEvent(it, type)
        if (type == MouseEventType.PRESS && it.button == MouseButton.SECONDARY) {
            circlePos?.let { circleVelocity = (getMousePos()!! - it) / 20.0 }
                ?: run { circlePos = getMousePos()!! }
        }
    }

    override fun onKeyPressed(it: KeyEvent) {
        super.onKeyPressed(it)
        if (it.code == KeyCode.ESCAPE) {
            circlePos = null
            circleVelocity = null
        }
    }
}