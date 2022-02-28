package server.adds

import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class VectorI(val x: Double, val y: Double) {
    operator fun plus(v: VectorI) = VectorI(this.x + v.x, this.y + v.y)
    operator fun minus(v: VectorI) = VectorI(this.x - v.x, this.y - v.y)
    operator fun times(s: Double) = VectorI(this.x * s, this.y * s)
    operator fun times(v: VectorI) = VectorI(this.x * v.x, this.y * v.y)
    operator fun div(s: Double) = VectorI(this.x / s, this.y / s)
    operator fun div(v: VectorI) = VectorI(this.x / v.x, this.y / v.y)
    operator fun unaryMinus() = this * -1.0

    fun length() = sqrt(this.x * this.x + this.y * this.y)
    fun e() = VectorI(1 / this.x, 1 / this.y)
    fun abs() = VectorI(kotlin.math.abs(this.x), kotlin.math.abs(this.y))

    infix fun distance(v: VectorI) = (v - this).length()
    infix fun sProduct(v: VectorI) = this.x * v.x + this.y * v.y

    fun addAll(vararg vs: VectorI): VectorI {
        var o = this.copy()
        vs.forEach { v -> o += v }
        return o
    }

    fun normalRight() = VectorI(this.y, -this.x)

    companion object {
        fun zero() = VectorI(0.0, 0.0)
        fun square(s: Double) = VectorI(s, s)
        fun fromAL(a: Double, l: Double) = VectorI(cos(a) * l, sin(a) * l)
    }
}

fun vec(a: Double, b: Double, al: Boolean = false) = if (al) VectorI.fromAL(a,b) else VectorI(a, b)

object RocketMath {
    fun inRange(z1: Double, z2: Double, d: Double) =
        z1 <= z2 + d/2 && z1 >= z2 - d/2
}