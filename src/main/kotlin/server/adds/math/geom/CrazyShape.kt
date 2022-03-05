package server.adds.math.geom

import server.adds.math.CrazyTransform
import server.adds.math.CrazyVector
import server.adds.math.geom.shapes.GeomType
import server.adds.math.geom.shapes.RocketRect

abstract class CrazyShape(val type: GeomType) {
    infix fun collides(o: CrazyShape) {

    }
    abstract fun sourroundedRect(): RocketRect
    abstract fun transform(trans: CrazyTransform): CrazyShape
    abstract infix fun containsPoint(point: CrazyVector): Boolean
    //protected fun trans(vec: RocketVector, trans: GeomTransform) = vec * trans.scaling + trans.pos
}