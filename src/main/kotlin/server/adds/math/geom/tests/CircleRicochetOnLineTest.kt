package server.adds.math.geom.tests

import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.input.MouseButton
import javafx.scene.input.MouseEvent
import javafx.scene.paint.Color
import server.adds.math.CrazyVector
import server.adds.math.debugString
import server.adds.math.geom.debug.*
import server.adds.math.geom.shapes.CrazyCircle
import server.adds.math.geom.shapes.CrazyLine
import server.adds.math.geom.shapes.ShapeDebugConfig
import server.adds.math.toDegrees
import server.adds.math.vec

class CircleRicochetOnLineTest : CrazyDebugger(
    GeomDebuggerConfig(
        unit = 100.0,
        transformEyeModule = TransformEyeModuleConfig(),
        timerModule = TimerModuleConfig(startStepSpeed = 50),
        debugObjectModule = DebugObjectModuleConfig()
    )
) {
    private var circlePos: CrazyVector? = null
    private var circleVelocity: CrazyVector? = null
    private val circleRadius = 2.0

    override suspend fun act(s: Double): Array<DebugObjectI> {
        val cPos = circlePos
        val cVelocity = circleVelocity?.times(s)
        var line = CrazyLine(vec(4, -2), vec(5, 4))

        val circle = cPos?.let { CrazyCircle(circleRadius, it) }

        if (cPos != null && cVelocity != null && circle != null) {

            val line2 = line normalLineFrom circle.pos
            val ints = line intersection line2

            val cpa = circle containsPoint line.a
            val cpb = circle containsPoint line.b

            val isPosRight = line isPointRight circle.pos
            val isVelocityRight = line.toVec().normalRight() scalar cVelocity > 0
            val isObjectRemoving = (!isVelocityRight && !isPosRight) || (isVelocityRight && isPosRight)

            val valRightOfNormal = (-line.delta()) scalar (cVelocity) > 0
            val wrongWay = (isPosRight && valRightOfNormal) || (!isPosRight && !valRightOfNormal)

            if (!isObjectRemoving) {
                if (cpa) {
                    //timerModule!!.stop()
                    circleVelocity = CrazyVector.ricochet(line.a, circle.pos, cVelocity)
                    line = line.setCrazyStyle(ShapeDebugConfig.DEFAULT_CRAZY_STYLE.copy(fillColor = Color.YELLOW)) as CrazyLine
                } else if (cpb) {
                    //timerModule!!.stop()
                    circleVelocity = CrazyVector.ricochet(line.b, circle.pos, cVelocity)
                    line = line.setCrazyStyle(ShapeDebugConfig.DEFAULT_CRAZY_STYLE.copy(fillColor = Color.GREEN)) as CrazyLine
                } else {
                    //timerModule!!.stop()
                    val orthogonalTouch = ints.onLine1 && circle containsPoint ints.intersection

                    if (orthogonalTouch) {
                        val angle = (line2.toVec() angleTo cVelocity)
                        line = line.setCrazyStyle(ShapeDebugConfig.DEFAULT_CRAZY_STYLE.copy(fillColor = Color.RED)) as CrazyLine

                        circleVelocity = (-cVelocity) rotate (2 * angle * (if (wrongWay) -1 else 1))
                    }
                }
            }

            circlePos = circlePos?.plus(cVelocity)

            val debugOptions = mapOf(
                "Contains Point A" to cpa.toString(),
                "Contains Point B" to cpb.toString(),
                "On Line" to ints.onLine1.toString(),
                "Velocity Angle vs. Normal Angle" to (line2.toVec() angleTo cVelocity).toDegrees().debugString(),
                "Is Pos Right" to isPosRight.toString(),
                "Is Object Removing" to isObjectRemoving.toString(),
                "Is Vel Right Normal" to isVelocityRight.toString(),
                "Wrong way?" to wrongWay.toString()
            )


            return arrayOf(
                circle.setDebugConfig(
                    DebugObjectOptions(
                        "Circle", "circle",
                        debugOptions
                    )
                ),
                (cVelocity*5).toLine(circle.pos).drawAsVector(),
                line2.drawAsVector(Color.BLACK),
                CrazyDebugVector(ints.intersection, CrazyDebugVectorOptions(size = 15.0, extraName = "I")),
                line.setDebugConfig(
                    DebugObjectOptions(
                        "Line", "line",
                        debugOptions
                    )
                )
            )
        }

        return if (circle != null) arrayOf(line, circle) else arrayOf(line)
    }

    override fun mouseEvent(it: MouseEvent, type: MouseEventType) {
        super.mouseEvent(it, type)
        if (type == MouseEventType.PRESS && it.button == MouseButton.SECONDARY) {
            circlePos?.let { circleVelocity = (getMouse()!! - it) / 20.0 }
                ?: run { circlePos = getMouse()!! }
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