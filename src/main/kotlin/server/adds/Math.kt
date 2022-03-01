package server.adds

import kotlin.math.*

data class RocketTransform(
    val rotate: Double? = null,
    val scale: Double = 1.0,
    val center: RocketVector? = null,
    val translate: RocketVector = RocketVector.zero()
)

data class RocketVector(val x: Double, val y: Double) {
    operator fun plus(v: RocketVector) = RocketVector(this.x + v.x, this.y + v.y)
    operator fun minus(v: RocketVector) = RocketVector(this.x - v.x, this.y - v.y)
    operator fun times(s: Double) = RocketVector(this.x * s, this.y * s)
    operator fun times(v: RocketVector) = RocketVector(this.x * v.x, this.y * v.y)
    operator fun div(s: Double) = RocketVector(this.x / s, this.y / s)
    operator fun div(v: RocketVector) = RocketVector(this.x / v.x, this.y / v.y)
    operator fun unaryMinus() = this * -1.0

    fun length() = sqrt(this.x * this.x + this.y * this.y)
    fun e() = RocketVector(1 / this.x, 1 / this.y)
    fun abs() = RocketVector(kotlin.math.abs(this.x), kotlin.math.abs(this.y))

    infix fun distance(v: RocketVector) = (v - this).length()
    infix fun scalar(v: RocketVector) = this.x * v.x + this.y * v.y

    fun addAll(vararg vs: RocketVector): RocketVector {
        var o = this.copy()
        vs.forEach { v -> o += v }
        return o
    }

    fun normalRight() = RocketVector(this.y, -this.x)

    fun rotateAroundOtherPoint(center: RocketVector, angle: Double): RocketVector {
        val x1 = this.x - center.x;
        val y1 = this.y - center.y;

        val x2 = x1 * cos(angle) - y1 * sin(angle)
        val y2 = x1 * sin(angle) + y1 * cos(angle)

        return RocketVector(
            x2 + center.x,
            y2 + center.y
        )
    }

    infix fun transformTo(trans: RocketTransform): RocketVector {
        var transPoint = this

        trans.center?.let { center ->
            transPoint -= center
            transPoint *= trans.scale

            trans.rotate?.let { transPoint = transPoint.rotateAroundOtherPoint(zero(), it) }

            transPoint += center
        }

        transPoint += trans.translate

        return transPoint
    }

    companion object {
        fun zero() = RocketVector(0.0, 0.0)
        fun square(s: Double) = RocketVector(s, s)
        fun fromAL(a: Double, l: Double) = RocketVector(cos(a) * l, sin(a) * l)
    }
}

fun vec(a: Double, b: Double, al: Boolean = false) = if (al) RocketVector.fromAL(a,b) else RocketVector(a, b)

object RocketMath {
    fun inRange(z1: Double, z2: Double, d: Double) =
        z1 <= z2 + d/2 && z1 >= z2 - d/2
}

object CollisionDetection {
    fun checkIfLineCollides(
        x1: Double, y1: Double, x2: Double, y2: Double,
        x3: Double, y3: Double, x4: Double, y4: Double
    ): RocketVector? {
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

            if(x >= min(x1, x2) && x <= max(x1,x2)
                && x >= min(x3, x4) && x <= max(x3, x4)
                && y >= min(y1, y2) && y <= max(y1, y2)
                && y >= min(y3, y4) && y <= max(y3, y4)) {
                return vec(x,y)
            }
        }
        return null
    }

    fun closestPointOnLine(
        lx1: Double, ly1: Double,
        lx2: Double, ly2: Double, x0: Double, y0: Double
    ): RocketVector {
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

    fun circleLineCollision(circle: RocketCircle, line: RocketLine): Boolean {
        val closestPoint = closestPointOnLine(
            line.a.x, line.a.y,
            line.b.x, line.b.y,
            circle.pos.x, circle.pos.y
        )
        return closestPoint distance circle.pos <= circle.radius
    }

    fun rectRectCollision(rect1: RocketRect, rect2: RocketRect) =
        (rect2.pos.x + rect2.size.x > rect1.pos.x || rect2.pos.x < rect1.pos.x + rect1.size.x)
        && (rect2.pos.y + rect2.size.y > rect1.pos.y || rect2.pos.y < rect1.pos.y + rect1.size.y)

    fun circleCircleCollision(circ1: RocketCircle, circ2: RocketCircle) =
        circ1.pos distance circ2.pos <= circ1.radius + circ2.radius

    fun circleRectCollision(circ: RocketCircle, rect: RocketRect): Boolean {
        val circSize = RocketVector.square(circ.radius)
        val rect2 = RocketRect(rect.pos - circSize, rect.size + (circSize * 2.0))
        return rect2 containsPoint circ.pos
    }

    fun circlePolygonCollision(circ: RocketCircle, polygon: RocketPolygon): Boolean {
        val popoints = polygon.getMyPoints()
        for (i in 0..(popoints.size - 2)) {
            val line = RocketLine(popoints[i], popoints[i+1])
            if (!circleLineCollision(circ, line)) return false
        }
        return true
    }

    fun polygonPolygonCollision(polygon1: RocketPolygon, polygon2: RocketPolygon): Boolean {
        for (p in polygon1.getMyPoints()) { if (polygon2 containsPoint p) return true }
        for (p in polygon2.getMyPoints()) { if (polygon1 containsPoint p) return true }
        return false
    }

    fun checkGeomRectCollision(geom1: AbstractGeom, geom2: AbstractGeom): Boolean {
        val firstCollisioncheckNecessary = (geom1 is RocketRect && geom2 is RocketRect) || (geom1 is RocketCircle && geom2 is RocketCircle)
        val firstCollisionCheck = firstCollisioncheckNecessary || rectRectCollision(geom1.sourroundedRect(), geom2.sourroundedRect())

        return firstCollisionCheck && when (geom1.type) {
            GeomType.CIRCLE -> {
                when (geom2.type) {
                    GeomType.POLYGON -> circlePolygonCollision(geom1, geom2)
                    GeomType.RECT -> circleRectCollision(geom1, geom2)
                    GeomType.LINE -> circleLineCollision(geom1, geom2)
                    GeomType.CIRCLE -> circleCircleCollision(geom1, geom2)
                }
            }
            GeomType.POLYGON -> {
                when (geom2) {
                    GeomType.CIRCLE -> circlePolygonCollision(geom2, geom1)
                    GeomType.POLYGON -> polygonPolygonCollision(geom1, geom2)
                    GeomType.RECT -> polygonPolygonCollision(geom1, geom2.toPolygon())
                    GeomType.LINE -> {}
                }
            }
            GeomType.RECT -> {
                when (geom2) {
                    GeomType.CIRCLE -> circleRectCollision(geom2, geom1)
                    GeomType.POLYGON -> polygonPolygonCollision(geom2, geom1.toPolygon())
                    GeomType.RECT -> rectRectCollision(geom1, geom2)
                    GeomType.LINE -> {

                    }
                }
            }
            GeomType.LINE -> {
                when (geom2) {
                    GeomType.CIRCLE -> circleLineCollision(geom2, geom1)
                    GeomType.POLYGON -> {

                    }
                    GeomType.RECT -> {

                    }
                    GeomType.LINE -> {

                    }
                }
            }
        }
    }
}

/*
// Java program to implement
// the above approach
object Polygon {
    // Utility function to find cross product
    // of two vectors
    private fun crossProduct(A: Array<VectorI>): Double {
        // Stores coefficient of X
        // direction of vector A[1]A[0]
        val x1 = A[1].x - A[0].x

        // Stores coefficient of Y
        // direction of vector A[1]A[0]
        val y1 = A[1].y - A[0].y

        // Stores coefficient of X
        // direction of vector A[2]A[0]
        val x2 = A[2].x - A[0].x

        // Stores coefficient of Y
        // direction of vector A[2]A[0]
        val y2 = A[2].y - A[0].y

        // Return cross product
        return x1 * y2 - y1 * x2
    }

    // Function to check if the polygon is
    // convex polygon or not
    fun isConvex(points: Array<VectorI>): Boolean {
        // Stores count of
        // edges in polygon
        val n = points.size

        // Stores direction of cross product
        // of previous traversed edges
        var prev = 0

        // Stores direction of cross product
        // of current traversed edges
        var curr = 0

        // Traverse the array
        for (i in 0 until n) {

            // Stores three adjacent edges
            // of the polygon
            val temp = arrayOf(
                points[i],
                points[(i + 1) % n],
                points[(i + 2) % n]
            )

            // Update curr
            curr = crossProduct(temp)

            // If curr is not equal to 0
            if (curr != 0) {

                // If direction of cross product of
                // all adjacent edges are not same
                prev = if (curr * prev < 0) {
                    return false
                } else {
                    // Update curr
                    curr
                }
            }
        }
        return true
    }

    // Driver code
    @JvmStatic
    fun main(args: Array<String>) {
        val points = arrayOf(intArrayOf(0, 0), intArrayOf(0, 1), intArrayOf(1, 1), intArrayOf(1, 0))
        if (isConvex(points)) {
            println("Yes")
        } else {
            println("No")
        }
    }
}

// This code is contributed by chitranayal
 */