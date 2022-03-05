package server.adds.math.geom.shapes

import server.adds.math.CrazyTransform
import server.adds.math.CrazyVector
import server.adds.math.geom.CrazyShape
import server.data_containers.TooLittlePointsInPolygonEx

open class RocketPolygon(protected val points: Array<CrazyVector>) : CrazyShape(GeomType.POLYGON) {
    val ltRectCorner: CrazyVector
    val brRectCorner: CrazyVector

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

        ltRectCorner = CrazyVector(lowestXCoordinate!!, lowestYCoordinate!!)
        brRectCorner = CrazyVector(highestXCoordinate!!, highestYCoordinate!!)
    }

    open fun getMyPoints() = points

    override fun sourroundedRect() = RocketRect(
        ltRectCorner,
        brRectCorner - ltRectCorner
    )

    override fun transform(trans: CrazyTransform) =
        RocketPolygon(points.map { it transformTo trans }.toTypedArray())

    override fun containsPoint(point: CrazyVector): Boolean {
        for (i in 0..(getMyPoints().size-2)) {
            val line = RocketLine(getMyPoints()[i], getMyPoints()[i + 1])
            if (!(line pointRightOnLine point)) return false
        }
        return true
    }
}