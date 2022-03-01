package server.adds

import server.data.CannotCheckPointOnLine
import server.data.NegativeCoordinateInSizeVector
import server.data.TooLittlePointsInPolygonEx
import java.util.Vector

data class GeoI(
    val pos: RocketVector = RocketVector.zero(),
    val width: Double = 0.0,
    val height: Double = 0.0,
    val ang: Double = 0.0
) {
    fun size() = vec(width, height)
    infix fun touchesRect(that: GeoI): Boolean {
        return
    }
}

data class GeomTransform(val scaling: Double, val pos: RocketVector)

enum class GeomType {
    CIRCLE,
    POLYGON,
    RECT,
    LINE
}

abstract class AbstractGeom(val type: GeomType) {
    infix fun collides(o: AbstractGeom) {

    }
    abstract fun sourroundedRect(): RocketRect
    abstract fun transform(trans: RocketTransform): AbstractGeom
    abstract infix fun containsPoint(point: RocketVector): Boolean
    //protected fun trans(vec: RocketVector, trans: GeomTransform) = vec * trans.scaling + trans.pos
}

class RocketCircle(val radius: Double, val pos: RocketVector) : AbstractGeom(GeomType.CIRCLE) {
    override fun sourroundedRect() = RocketRect(
        pos - RocketVector.square(radius),
        RocketVector.square(radius*2)
    )

    override fun transform(trans: RocketTransform) = RocketCircle(
        radius * trans.scale,
        pos transformTo trans
    )

    override fun containsPoint(point: RocketVector) = point distance pos < radius
    //override fun transform(trans: GeomTransform) = RocketCircle(radius * trans.scaling, pos + trans.pos)
}

class RocketLine(val a: RocketVector, val b: RocketVector) : AbstractGeom(GeomType.LINE) {
    private fun ltRectCorner() = vec(if (a.x < b.x) a.x else b.x, if (a.y < b.y) a.y else b.y)
    private fun brRectCorner() = vec(if (a.x > b.x) a.x else b.x, if (a.y > b.y) a.y else b.y)

    override fun sourroundedRect() = RocketRect(
        ltRectCorner(),
        brRectCorner() - ltRectCorner()
    )

    override fun transform(trans: RocketTransform) = RocketLine(
        a transformTo trans,
        b transformTo trans
    )

    override fun containsPoint(point: RocketVector): Boolean {
        throw CannotCheckPointOnLine()
    }

    infix fun pointRightOnLine(point: RocketVector): Boolean {
        val vec1 = (b - a).normalRight()
        val vec2 = point - a
        return vec1 scalar vec2 < 0.0
    }
}

class RocketRect(val pos: RocketVector, val size: RocketVector) : AbstractGeom(GeomType.RECT) {
    init {
        if (pos.x < 0.0 && pos.x < 0.0) throw NegativeCoordinateInSizeVector(size)
    }

    override fun sourroundedRect() = this

    override fun transform(trans: RocketTransform): AbstractGeom {
        val tlCorner = pos transformTo trans
        val brCorner = (pos + size) transformTo trans
        return RocketRect(tlCorner, brCorner - tlCorner)
    }

    override infix fun containsPoint(point: RocketVector) =
        point.x >= pos.x && point.y >= pos.y && point.x <= pos.x + size.x && point.y <= pos.y + size.y

    fun toPolygon() = RocketPolygon(arrayOf(
        pos,        vec(pos.x + size.x, pos.y),
        pos + size, vec(pos.x, pos.y + size.y)
    ))

    fun width() = size.x
    fun height() = size.y
}

open class RocketPolygon(protected val points: Array<RocketVector>) : AbstractGeom(GeomType.POLYGON) {
    val ltRectCorner: RocketVector
    val brRectCorner: RocketVector

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

        ltRectCorner = RocketVector(lowestXCoordinate!!, lowestYCoordinate!!)
        brRectCorner = RocketVector(highestXCoordinate!!, highestYCoordinate!!)
    }

    open fun getMyPoints() = points

    override fun sourroundedRect() = RocketRect(
        ltRectCorner,
        brRectCorner - ltRectCorner
    )

    override fun transform(trans: RocketTransform) =
        RocketPolygon(points.map { it transformTo trans }.toTypedArray())

    override fun containsPoint(point: RocketVector): Boolean {
        for (i in 0..(getMyPoints().size-2)) {
            val line = RocketLine(getMyPoints()[i], getMyPoints()[i+1])
            if (!(line pointRightOnLine point)) return false
        }
        return true
    }
}

class RocketRelativePolygon(relativePoints: Array<RocketVector>) : RocketPolygon(relativePoints) {
    var currentRelativePoints = relativePoints.copyOf()
    var currentLtRectCorner = ltRectCorner
    var currentBrRectCorner = brRectCorner

    override fun getMyPoints() = currentRelativePoints
    override fun sourroundedRect() = RocketRect(currentLtRectCorner, currentBrRectCorner - currentLtRectCorner)

    fun relativeTransform(trans: RocketTransform) {
        currentRelativePoints = points.map { it transformTo trans }.toTypedArray()
        currentLtRectCorner = ltRectCorner transformTo trans
        currentLtRectCorner = ltRectCorner transformTo trans
    }

    fun simplePolygon() = RocketPolygon(points)
}