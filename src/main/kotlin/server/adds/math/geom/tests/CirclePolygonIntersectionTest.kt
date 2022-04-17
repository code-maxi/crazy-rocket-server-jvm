package server.adds.math.geom.tests

import javafx.scene.paint.Color
import server.adds.debug.*
import server.adds.math.CrazyVector
import server.adds.math.geom.shapes.*
import server.adds.math.vec

class CirclePolygonIntersectionTest : CrazyDebugger(
    CrazyDebuggerConfig(
        unit = 100.0,
        eyeModule = TransformEyeModuleConfig(),
        timerModule = TimerModuleConfig(startStepSpeed = 50),
        inspectorModule = InspectorModuleConfig()
    )
) {
    override suspend fun act(s: Double): List<DebugObjectI> {
        val polygon = CrazyPolygon(
            listOf(
                vec(0, 0),
                vec(2, 1),
                vec(3, -1),
                vec (4, 2),
                vec(3, 3),
                vec(1, 1)
            )
        ).setColor(Color.RED) as CrazyPolygon

        val circle = CrazyCircle(0.5, getMousePos() ?: CrazyVector.zero())

        val popoints = polygon.pointsWithEnd()
        var paintingElements = listOf<DebugObjectI>()
        var touchesPolygon: Boolean? = null

        for (i in 0..popoints.size - 2) {
            val line1 = CrazyLine(popoints[i], popoints[i+1])
            val line2 = line1.orthogonalLineFromPoint(circle.pos)
            val intersection = line1 intersection line2

            if (intersection.onLine1) {
                paintingElements += line2.drawAsVector()
                paintingElements += CrazyDebugVector(intersection.intersection, CrazyDebugVectorOptions(size = 15.0, extraName = "I"))

                if (circle.pos distance intersection.intersection < circle.radius) touchesPolygon = true
                else if (!(line1 isPointRight circle.pos)) touchesPolygon =  false
            }

            //if (touchesPolygon != null) continue
        }

        if (touchesPolygon == null) touchesPolygon = true

        touchesPolygon = circle collides polygon

        return listOf(
            circle.setDebugConfig(DebugObjectOptions("Circle", "circle", mapOf(
                "touchesPolygon" to touchesPolygon.toString(),
                //"isPointInPolygon" to isPointInPolygon.toString()
            ))),
            polygon,
            *paintingElements.toTypedArray()
        )
    }
}