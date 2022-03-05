package server.adds.math

import server.adds.math.CrazyVector

data class CrazyTransform(
    val rotate: Double? = null,
    val scale: Double = 1.0,
    val center: CrazyVector? = null,
    val translate: CrazyVector = CrazyVector.zero()
)

fun vec(a: Double, b: Double, al: Boolean = false) = if (al) CrazyVector.fromAL(a,b) else CrazyVector(a, b)

object RocketMath {
    fun inRange(z1: Double, z2: Double, d: Double) =
        z1 <= z2 + d/2 && z1 >= z2 - d/2
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