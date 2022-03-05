package server.adds.math.geom.shapes

import javafx.scene.canvas.GraphicsContext
import server.adds.JavaFXGraphics
import server.adds.math.CrazyTransform
import server.adds.math.CrazyVector
import server.data_containers.TooLittlePointsInPolygonEx

open class RocketPolygon(
    private val points: Array<CrazyVector>,
    config: ShapeDebugConfig = ShapeDebugConfig()
) : CrazyShape(GeomType.POLYGON, config) {

    private val surroundedRect: CrazyRect

    init {
        if (points.size < 3) throw TooLittlePointsInPolygonEx(points.size)

        var lowestXCoordinate: Double? = null
        var lowestYCoordinate: Double? = null

        var highestXCoordinate: Double? = null
        var highestYCoordinate: Double? = null

        points.forEach {
            if (lowestXCoordinate == null || it.x < lowestXCoordinate!!) lowestXCoordinate = it.x
            if (lowestYCoordinate == null || it.y < lowestYCoordinate!!) lowestYCoordinate = it.y
            if (highestXCoordinate == null || it.x < highestXCoordinate!!) highestXCoordinate = it.x
            if (highestYCoordinate == null || it.y < highestYCoordinate!!) highestYCoordinate = it.y
        }

        val ltRectCorner = CrazyVector(lowestXCoordinate!!, lowestYCoordinate!!)
        val brRectCorner = CrazyVector(highestXCoordinate!!, highestYCoordinate!!)

        surroundedRect = CrazyRect(
            ltRectCorner,
            brRectCorner - ltRectCorner
        )
    }

    open fun getMyPoints() = points

    override fun surroundedRect() = surroundedRect

    override fun transform(trans: CrazyTransform) =
        RocketPolygon(points.map { it transformTo trans }.toTypedArray())

    override fun containsPoint(point: CrazyVector): Boolean {
        for (i in 0..(getMyPoints().size-2)) {
            val line = RocketLine(getMyPoints()[i], getMyPoints()[i + 1])
            if (!(line pointRightOnLine point)) return false
        }
        return true
    }

    override fun paintDebug(g2: GraphicsContext) {
        super.paintDebug(g2)

        g2.beginPath()

        points.forEachIndexed { i, p ->
            if (i == 0) g2.moveTo(p.x, p.y)
            else g2.lineTo(p.x, p.y)
        }

        g2.closePath()
        g2.fill()
        g2.stroke()

        points.forEach {
            JavaFXGraphics.paintPoint(g2, it, color = config.color, paintCoords = config.paintCoords)
        }
    }
}