package server.adds.math.geom.shapes

import server.adds.math.CrazyTransform
import server.adds.math.CrazyVector
import server.adds.math.geom.CrazyShape
import server.adds.math.vec
import server.data_containers.NegativeCoordinateInSizeVector

class RocketRect(val pos: CrazyVector, val size: CrazyVector) : CrazyShape(GeomType.RECT) {
    init {
        if (pos.x < 0.0 && pos.x < 0.0) throw NegativeCoordinateInSizeVector(size)
    }

    override fun sourroundedRect() = this

    override fun transform(trans: CrazyTransform): CrazyShape {
        val tlCorner = pos transformTo trans
        val brCorner = (pos + size) transformTo trans
        return RocketRect(tlCorner, brCorner - tlCorner)
    }

    override infix fun containsPoint(point: CrazyVector) =
        point.x >= pos.x && point.y >= pos.y && point.x <= pos.x + size.x && point.y <= pos.y + size.y

    fun toPolygon() = RocketPolygon(
        arrayOf(
            pos, vec(pos.x + size.x, pos.y),
            pos + size, vec(pos.x, pos.y + size.y)
        )
    )

    fun width() = size.x
    fun height() = size.y
}