package server.adds.math

import server.adds.math.geom.shapes.CrazyCircle
import server.adds.math.geom.shapes.CrazyLine
import server.adds.math.geom.shapes.CrazyPolygon
import server.adds.math.geom.shapes.CrazyRect
import kotlin.math.max
import kotlin.math.min

object CollisionDetection {
    fun checkIfLineCollides(
        x1: Double, y1: Double, x2: Double, y2: Double,
        x3: Double, y3: Double, x4: Double, y4: Double
    ): CrazyVector? {
        val a1 = y2-y1
        val b1 = x1-x2
        val c1 = a1*x1 + b1*y1
        val a2 = y4-y3
        val b2 = x3-x4;
        val c2 = a2*x3 + b2*y3
        val det = a1*b2-a2*b1

        if(det != 0.0){
            val x = (b2*c1 - b1*c2)/det
            val y = (a1*c2 - a2*c1)/det

            if(x >= min(x1, x2) && x <= max(x1, x2)
                && x >= min(x3, x4) && x <= max(x3, x4)
                && y >= min(y1, y2) && y <= max(y1, y2)
                && y >= min(y3, y4) && y <= max(y3, y4)
            ) {
                return vec(x, y)
            }
        }
        return null
    }

    fun closestPointOnLine(
        lx1: Double, ly1: Double,
        lx2: Double, ly2: Double, x0: Double, y0: Double
    ): CrazyVector {
        val a1 = ly2 - ly1
        val b1 = lx1 - lx2
        val c1 = ((ly2 - ly1) * lx1 + (lx1 - lx2) * ly1)
        val c2 = (-b1 * x0 + a1 * y0)
        val det = (a1 * a1 - -b1 * b1)
        val cx: Double
        val cy: Double

        if (det != 0.0) {
            cx = ((a1 * c1 - b1 * c2) / det).toFloat().toDouble()
            cy = ((a1 * c2 - -b1 * c1) / det).toFloat().toDouble()
        } else {
            cx = x0
            cy = y0
        }

        return vec(cx, cy)
    }

    fun circleLineCollision(circle: CrazyCircle, line: CrazyLine): Boolean {
        val closestPoint = closestPointOnLine(
            line.a.x, line.a.y,
            line.b.x, line.b.y,
            circle.pos.x, circle.pos.y
        )
        return closestPoint distance circle.pos <= circle.radius
    }

    fun rectRectCollision(rect1: CrazyRect, rect2: CrazyRect) =
        (rect2.pos.x + rect2.size.x > rect1.pos.x || rect2.pos.x < rect1.pos.x + rect1.size.x)
        && (rect2.pos.y + rect2.size.y > rect1.pos.y || rect2.pos.y < rect1.pos.y + rect1.size.y)

    fun circleCircleCollision(circ1: CrazyCircle, circ2: CrazyCircle) =
        circ1.pos distance circ2.pos <= circ1.radius + circ2.radius

    fun circleRectCollision(circ: CrazyCircle, rect: CrazyRect): Boolean {
        val circSize = CrazyVector.square(circ.radius)
        val rect2 = CrazyRect(rect.pos - circSize, rect.size + (circSize * 2.0))
        return rect2 containsPoint circ.pos
    }

    fun circlePolygonCollision(circ: CrazyCircle, polygon: CrazyPolygon): Boolean {
        val popoints = polygon.getMyPoints()
        for (i in 0..(popoints.size - 2)) {
            val line = CrazyLine(popoints[i], popoints[i + 1])
            if (!circleLineCollision(circ, line)) return false
        }
        return true
    }

    fun polygonPolygonCollision(polygon1: CrazyPolygon, polygon2: CrazyPolygon): Boolean {
        for (p in polygon1.getMyPoints()) { if (polygon2 containsPoint p) return true }
        for (p in polygon2.getMyPoints()) { if (polygon1 containsPoint p) return true }
        return false
    }

    /*fun checkGeomRectCollision(geom1: AbstractGeom, geom2: AbstractGeom): Boolean {
        val firstCollisioncheckNecessary = (geom1 is RocketRect && geom2 is RocketRect) || (geom1 is RocketCircle && geom2 is RocketCircle)
        val firstCollisionCheck = firstCollisioncheckNecessary || rectRectCollision(geom1.sourroundedRect(), geom2.sourroundedRect())

        return firstCollisionCheck && when (geom1) {
            is RocketCircle -> {
                when (geom2) {
                    is RocketPolygon -> circlePolygonCollision(geom1, geom2)
                    is RocketRect -> circleRectCollision(geom1, geom2)
                    is RocketLine -> circleLineCollision(geom1, geom2)
                    is RocketCircle -> circleCircleCollision(geom1, geom2)
                    else -> null
                }!!
            }
            is RocketPolygon -> {
                when (geom2) {
                    is RocketCircle -> circlePolygonCollision(geom2, geom1)
                    is RocketPolygon -> polygonPolygonCollision(geom1, geom2)
                    is RocketRect -> polygonPolygonCollision(geom1, geom2.toPolygon())
                    is RocketLine -> {}
                    else -> null
                }!!
            }
            is RocketRect -> {
                when (geom2) {
                    is RocketCircle -> circleRectCollision(geom2, geom1)
                    is RocketPolygon -> polygonPolygonCollision(geom2, geom1.toPolygon())
                    is RocketRect -> rectRectCollision(geom1, geom2)
                    is RocketLine -> {

                    }
                    else -> null
                }!!
            }
            is RocketLine -> {
                when (geom2) {
                    is RocketCircle -> circleLineCollision(geom2, geom1)
                    is RocketPolygon -> {

                    }
                    is RocketRect -> {

                    }
                    is RocketLine -> {

                    }
                    else -> null
                }!!
            }
        }
    }*/
}