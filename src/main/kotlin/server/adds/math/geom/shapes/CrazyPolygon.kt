package server.adds.math.geom.shapes

import javafx.geometry.Point2D
import javafx.scene.canvas.GraphicsContext
import javafx.scene.paint.Color
import javafx.scene.shape.Polygon
import server.adds.CrazyGraphicStyle
import server.adds.CrazyGraphics
import server.adds.debug.DebugObjectOptions
import server.adds.math.CrazyTransform
import server.adds.math.CrazyVector
import server.adds.debug.DebugTransform
import server.data_containers.TooLittlePointsInPolygonEx

open class CrazyPolygon(
    val points: List<CrazyVector>,
    config: ShapeDebugConfig? = null,
    surroundedRectP: CrazyRect? = null
) : CrazyShape(ShapeType.POLYGON, config) {

    private val surroundedRect: CrazyRect

    init {
        if (points.size < 3) throw TooLittlePointsInPolygonEx(points.size)

        if (surroundedRectP != null) surroundedRect = surroundedRectP
        else {
            var lowestXCoordinate: Double? = null
            var lowestYCoordinate: Double? = null

            var highestXCoordinate: Double? = null
            var highestYCoordinate: Double? = null

            points.forEach {
                if (lowestXCoordinate == null || it.x < lowestXCoordinate!!) lowestXCoordinate = it.x
                if (lowestYCoordinate == null || it.y < lowestYCoordinate!!) lowestYCoordinate = it.y
                if (highestXCoordinate == null || it.x > highestXCoordinate!!) highestXCoordinate = it.x
                if (highestYCoordinate == null || it.y > highestYCoordinate!!) highestYCoordinate = it.y
            }

            val ltRectCorner = CrazyVector(lowestXCoordinate!!, lowestYCoordinate!!)
            val brRectCorner = CrazyVector(highestXCoordinate!!, highestYCoordinate!!)

            surroundedRect = CrazyRect(
                ltRectCorner,
                brRectCorner - ltRectCorner
            )
        }

    }

    open fun getMyPoints() = points

    fun pointsWithEnd() = this.points + this.points.first()

    override fun surroundedRect() = surroundedRect

    override fun shapeString() = "Polygon(points = [${points.joinToString(", ") { it.niceString() }}])"

    override fun transform(trans: CrazyTransform) =
        CrazyPolygon(points.map { it transformTo trans }, config)

    override fun containsPoint(point: CrazyVector): Boolean {
        var arr = doubleArrayOf()
        points.forEach { arr += it.x; arr += it.y }
        return Polygon(*arr).contains(Point2D(point.x, point.y))
    }

    override fun paintSelf(gc: GraphicsContext, transform: DebugTransform, config: ShapeDebugConfig) {
        gc.beginPath()

        points.forEachIndexed { i, pp ->
            val p = transform.screen(pp)
            if (i == 0) gc.moveTo(p.x, p.y)
            else gc.lineTo(p.x, p.y)
        }

        gc.closePath()

        gc.fill()
        gc.stroke()

        if (config.paintPoints) points.forEachIndexed { i, it ->
            CrazyGraphics.paintPoint(
                gc, transform.screen(it),
                coordinates = if (config.paintCoords) it else null,
                name = if (config.paintPointNames) i.toString() else null
            )
        }
    }

    override fun setConfig(shapeDebugConfig: ShapeDebugConfig?) = CrazyPolygon(points, shapeDebugConfig)

    fun copy(points: List<CrazyVector> = this.points, config: ShapeDebugConfig? = this.config) =
        CrazyPolygon(points, config)

    fun convert(f: (CrazyVector) -> CrazyVector) = copy(points = points.map { f(it) })

    override fun setZIndex(i: Int) = super.setZIndex(i) as CrazyPolygon
    override fun setColor(c: Color) = super.setColor(c) as CrazyPolygon
    override fun setDebugConfig(options: DebugObjectOptions) = super.setDebugConfig(options) as CrazyPolygon
    override fun setCrazyStyle(style: CrazyGraphicStyle) = super.setCrazyStyle(style) as CrazyPolygon
}