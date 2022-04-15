package server.adds.math

import server.adds.math.geom.shapes.*

object CrazyCollision {
    fun circleLineCollision(circle: CrazyCircle, line: CrazyLine): Boolean {
        if (circle containsPoint line.a || circle containsPoint line.b) return true

        val line2 = CrazyLine(circle.pos, circle.pos + line.delta().normalRight().e())
        val ints = line intersection line2
        return ints.onLine1 && ints.intersection distance circle.pos < circle.radius
    }

    fun rectRectCollision(rect1: CrazyRect, rect2: CrazyRect) =
        (rect2.pos.x + rect2.size.x > rect1.pos.x || rect2.pos.x < rect1.pos.x + rect1.size.x)
        && (rect2.pos.y + rect2.size.y > rect1.pos.y || rect2.pos.y < rect1.pos.y + rect1.size.y)

    fun circleCircleCollision(circle1: CrazyCircle, circle2: CrazyCircle) =
        circle1.pos distance circle2.pos <= circle1.radius + circle2.radius

    fun circleRectCollision(circle1: CrazyCircle, rect1: CrazyRect): Boolean {
        val circleSize = CrazyVector.square(circle1.radius)
        val rect2 = CrazyRect(rect1.pos - circleSize, rect1.size + (circleSize * 2.0))
        return rect2 containsPoint circle1.pos
    }

    fun circlePolygonCollision(circle: CrazyCircle, polygon: CrazyPolygon): Boolean {
        val popoints = polygon.pointsWithEnd()

        if (polygon containsPoint circle.pos) return true

        for (i in popoints.indices) {
            if (circle containsPoint popoints[i]) return true
            if (i < popoints.size - 1) {
                val line1 = CrazyLine(popoints[i], popoints[i+1])
                val line2 = line1.orthogonalLineFromPoint(circle.pos)
                val intersection = line1 intersection line2

                if (intersection.onLine1) {
                    if (circle.pos distance intersection.intersection < circle.radius) return true
                }
            }
            //if (touchesPolygon != null) continue
        }

        return false
    }

    fun polygonLineCollision(polygon1: CrazyPolygon, line1: CrazyLine): Boolean {
        if (polygon1 containsPoint line1.a) return true
        else if (polygon1 containsPoint line1.b) return true
        else {
            val poPoints = polygon1.pointsWithEnd()
            for (i in 0..(poPoints.size-2)) {
                val line2 = CrazyLine(poPoints[i], poPoints[i+1])
                if ((line1 intersection line2).collides) return true
            }
            return false
        }
    }

    fun rectLineCollision(rect1: CrazyRect, line1: CrazyLine): Boolean {
        val polygon1 = rect1.toPolygon()
        if (polygon1 containsPoint line1.a) return true
        else if (polygon1 containsPoint line1.b) return true
        else {
            val poPoints = polygon1.pointsWithEnd()
            for (i in 0..(poPoints.size-2)) {
                val line2 = CrazyLine(poPoints[i], poPoints[i+1])
                if ((line1 intersection line2).collides) return true
            }
            return false
        }
    }

    fun polygonPolygonCollision(polygon1: CrazyPolygon, polygon2: CrazyPolygon): Boolean {
        for (p in polygon1.getMyPoints()) { if (polygon2 containsPoint p) return true }
        for (p in polygon2.getMyPoints()) { if (polygon1 containsPoint p) return true }
        return false
    }

    fun shapeShapeCollision(shape1: CrazyShape, shape2: CrazyShape): Boolean {
        val checkNotNecessary = (shape1 is CrazyRect && shape2 is CrazyRect)// || (shape1 is CrazyCircle && shape2 is CrazyCircle)
        val firstCollisionCheck = rectRectCollision(shape1.surroundedRect(), shape2.surroundedRect())

        return firstCollisionCheck && (checkNotNecessary || when (shape1) {
            is CrazyCircle -> {
                when (shape2) {
                    is CrazyPolygon -> circlePolygonCollision(shape1, shape2)
                    is CrazyRect -> circleRectCollision(shape1, shape2)
                    is CrazyLine -> circleLineCollision(shape1, shape2)
                    is CrazyCircle -> circleCircleCollision(shape1, shape2)
                    else -> null
                }!!
            }
            is CrazyPolygon -> {
                when (shape2) {
                    is CrazyCircle -> circlePolygonCollision(shape2, shape1)
                    is CrazyPolygon -> polygonPolygonCollision(shape1, shape2)
                    is CrazyRect -> polygonPolygonCollision(shape1, shape2.toPolygon())
                    is CrazyLine -> polygonLineCollision(shape1, shape2)
                    else -> null
                }!!
            }
            is CrazyRect -> {
                when (shape2) {
                    is CrazyCircle -> circleRectCollision(shape2, shape1)
                    is CrazyPolygon -> polygonPolygonCollision(shape2, shape1.toPolygon())
                    //is CrazyRect -> rectRectCollision(shape1, shape2)
                    is CrazyLine -> rectLineCollision(shape1, shape2)
                    else -> null
                }!!
            }
            is CrazyLine -> {
                when (shape2) {
                    is CrazyCircle -> circleLineCollision(shape2, shape1)
                    is CrazyPolygon -> polygonLineCollision(shape2, shape1)
                    is CrazyRect -> rectLineCollision(shape2, shape1)
                    is CrazyLine -> (shape1 intersection shape2).collides
                    else -> null
                }!!
            }
            else -> null
        }!!)
    }

    fun partiallyElasticCollision2D(
        m1: Double, v1: CrazyVector, p1: CrazyVector, 
        m2: Double, v2: CrazyVector, p2: CrazyVector,
        k: Double = 1.0, checkMovingAway: Boolean = true
    ): PartiallyElasticCollisionData2D? {
        var result: PartiallyElasticCollisionData2D? = null
        val movingAwayFrom = (p2 + v2 - p1 - v1).length() > (p2 - p1).length()

        if (!checkMovingAway || !movingAwayFrom) {
            val nv1 = (v1*m1 + v2*m2 - (v1 - v2) * m2 * k) / (m1 + m2)
            val nv2 = (v1*m1 + v2*m2 - (v2 - v1) * m1 * k) / (m1 + m2)

            val energyLost = (v1-v2).selfScalar() * ((m1 * m2) / (2 * (m1 + m2))) * (1 - k*k)

            result = PartiallyElasticCollisionData2D(nv1, nv2, energyLost)
        }

        return result
    }

    fun partiallyElasticCollision1D(
        m1: Double, v1: Double,
        m2: Double, v2: Double,
        k: Double = 1.0
    ): PartiallyElasticCollisionData1D {
        val nv1 = (v1*m1 + v2*m2 - (v1 - v2) * m2 * k) / (m1 + m2)
        val nv2 = (v1*m1 + v2*m2 - (v2 - v1) * m1 * k) / (m1 + m2)
        val vd = v1 - v2
        val energyLost = vd * vd * ((m1 * m2) / (2 * (m1 + m2))) * (1 - k*k)
        return PartiallyElasticCollisionData1D(nv1, nv2, energyLost)
    }

    fun partiallyElasticCollision2Dv2(
        m1: Double, v1: CrazyVector, p1: CrazyVector,
        m2: Double, v2: CrazyVector, p2: CrazyVector,
        k: Double = 1.0
    ): PartiallyElasticCollisionData2D? {
        var result: PartiallyElasticCollisionData2D? = null

        val ne = (p1 - p2).e()
        val te = ne.normalLeft()

        val isV1Right = ne scalar v1 > 0
        val isV2Left = ne scalar v2 < 0

        if (isV1Right && isV2Left) {
            val cn1 = ne * (ne scalar v1)
            val cn2 = ne * (ne scalar v2)

            val ct1 = te * (te scalar v1)
            val ct2 = te * (te scalar v2)

            val collision1dResult = partiallyElasticCollision1D(m1, cn1.length(), m2, cn2.length(), k)

            val nv1 = ct1 + (ne * collision1dResult.nv1)
            val nv2 = ct2 + (ne * collision1dResult.nv2)

            result = PartiallyElasticCollisionData2D(nv1, nv2, collision1dResult.energyLost)
        }

        return result
    }
}