package server.adds.math

import server.adds.math.geom.shapes.*

object CollisionDetection {
    private fun circleLineCollision(circle: CrazyCircle, line: CrazyLine): Boolean {
        val line2 = CrazyLine(circle.pos, circle.pos + line.delta().normalRight().e())
        val ints = line intersection line2
        return ints.onLine1
    }

    fun rectRectCollision(rect1: CrazyRect, rect2: CrazyRect) =
        (rect2.pos.x + rect2.size.x > rect1.pos.x || rect2.pos.x < rect1.pos.x + rect1.size.x)
        && (rect2.pos.y + rect2.size.y > rect1.pos.y || rect2.pos.y < rect1.pos.y + rect1.size.y)

    private fun circleCircleCollision(circle1: CrazyCircle, circle2: CrazyCircle) =
        circle1.pos distance circle2.pos <= circle1.radius + circle2.radius

    private fun circleRectCollision(circle1: CrazyCircle, rect1: CrazyRect): Boolean {
        val circleSize = CrazyVector.square(circle1.radius)
        val rect2 = CrazyRect(rect1.pos - circleSize, rect1.size + (circleSize * 2.0))
        return rect2 containsPoint circle1.pos
    }

    fun circlePolygonCollision(circ1: CrazyCircle, polygon: CrazyPolygon): Boolean {
        val poPoints = polygon.pointsWithEnd()
        for (i in 0..(poPoints.size - 2)) {
            val line = CrazyLine(poPoints[i], poPoints[i + 1])
            if (!circleLineCollision(circ1, line)) return false
        }
        return true
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
        val checkNotNecessary = (shape1 is CrazyRect && shape2 is CrazyRect) || (shape1 is CrazyCircle && shape2 is CrazyCircle)
        val firstCollisionCheck = checkNotNecessary || rectRectCollision(shape1.surroundedRect(), shape2.surroundedRect())

        return firstCollisionCheck && when (shape1) {
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
                    is CrazyRect -> rectRectCollision(shape1, shape2)
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
        }!!
    }
}