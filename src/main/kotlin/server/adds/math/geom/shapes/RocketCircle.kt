package server.adds.math.geom.shapes

import server.adds.math.CrazyTransform
import server.adds.math.CrazyVector
import server.adds.math.geom.CrazyShape

class RocketCircle(val radius: Double, val pos: CrazyVector) : CrazyShape(GeomType.CIRCLE) {
    override fun sourroundedRect() = RocketRect(
        pos - CrazyVector.square(radius),
        CrazyVector.square(radius * 2)
    )

    override fun transform(trans: CrazyTransform) = RocketCircle(
        radius * trans.scale,
        pos transformTo trans
    )

    override fun containsPoint(point: CrazyVector) = point distance pos < radius
    //override fun transform(trans: GeomTransform) = RocketCircle(radius * trans.scaling, pos + trans.pos)
}