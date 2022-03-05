package server.adds.math.geom.shapes

import server.adds.math.CrazyTransform
import server.adds.math.CrazyVector
import server.adds.math.vec
import server.data_containers.CannotCheckPointOnLine

class RocketLine(val a: CrazyVector, val b: CrazyVector) : CrazyShape(GeomType.LINE) {
    private fun ltRectCorner() = vec(if (a.x < b.x) a.x else b.x, if (a.y < b.y) a.y else b.y)
    private fun brRectCorner() = vec(if (a.x > b.x) a.x else b.x, if (a.y > b.y) a.y else b.y)

    override fun surroundedRect() = CrazyRect(
        ltRectCorner(),
        brRectCorner() - ltRectCorner()
    )

    override fun transform(trans: CrazyTransform) = RocketLine(
        a transformTo trans,
        b transformTo trans
    )

    override fun containsPoint(point: CrazyVector): Boolean {
        throw CannotCheckPointOnLine()
    }

    infix fun pointRightOnLine(point: CrazyVector): Boolean {
        val vec1 = (b - a).normalRight()
        val vec2 = point - a
        return vec1 scalar vec2 < 0.0
    }
}