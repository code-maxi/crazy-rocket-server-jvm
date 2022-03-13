package server.adds.math.geom.shapes

import javafx.scene.canvas.GraphicsContext
import server.adds.CrazyGraphics
import server.adds.math.CrazyTransform
import server.adds.math.CrazyVector
import server.adds.math.geom.debug.DebugTransform
import server.data_containers.TooLittlePointsInPolygonEx

open class CrazyPolygon(
    private val points: Array<CrazyVector>,
    config: ShapeDebugConfig? = null
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
        CrazyPolygon(points.map { it transformTo trans }.toTypedArray())

    override fun containsPoint(point: CrazyVector): Boolean {
        for (i in 0..(getMyPoints().size-2)) {
            val line = CrazyLine(getMyPoints()[i], getMyPoints()[i + 1])
            if (!(line pointRightOnLine point)) return false
        }
        return true
    }

    override fun paintSelf(g2: GraphicsContext, transform: DebugTransform, config: ShapeDebugConfig) {
        g2.beginPath()

        points.forEachIndexed { i, p ->
            if (i == 0) g2.moveTo(p.x, p.y)
            else g2.lineTo(p.x, p.y)
        }

        g2.closePath()
        if (config.crazyStyle.fillOpacity != null) g2.fill()
        if (config.crazyStyle.strokeOpacity != null) g2.stroke()

        points.forEach {
            CrazyGraphics.paintPoint(
                g2, transform.screen(it),
                paintCoords = config.paintCoords
            )
        }
    }

    override fun setConfig(shapeDebugConfig: ShapeDebugConfig) = CrazyPolygon(points, shapeDebugConfig)
}