package server.adds.math

import server.adds.math.geom.shapes.CrazyLine
import java.text.DecimalFormat
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class CrazyVector(val x: Double, val y: Double) {
    operator fun plus(v: CrazyVector) = CrazyVector(this.x + v.x, this.y + v.y)
    operator fun minus(v: CrazyVector) = CrazyVector(this.x - v.x, this.y - v.y)
    operator fun times(s: Number) = CrazyVector(this.x * s.toDouble(), this.y * s.toDouble())
    operator fun times(v: CrazyVector) = CrazyVector(this.x * v.x, this.y * v.y)
    operator fun div(s: Double) = CrazyVector(this.x / s, this.y / s)
    operator fun div(v: CrazyVector) = CrazyVector(this.x / v.x, this.y / v.y)
    operator fun unaryMinus() = this * -1.0

    fun length() = sqrt(this.x * this.x + this.y * this.y)
    fun angle() = atan2(y, x)
    fun e() = CrazyVector(1 / this.x, 1 / this.y)
    fun abs() = CrazyVector(kotlin.math.abs(this.x), kotlin.math.abs(this.y))

    infix fun distance(v: CrazyVector) = (v - this).length()
    infix fun scalar(v: CrazyVector) = this.x * v.x + this.y * v.y

    fun addAll(vararg vs: CrazyVector): CrazyVector {
        var o = this.copy()
        vs.forEach { v -> o += v }
        return o
    }

    fun normalRight() = CrazyVector(this.y, -this.x)

    fun rotateAroundOtherPoint(center: CrazyVector, angle: Double): CrazyVector {
        val x1 = this.x - center.x;
        val y1 = this.y - center.y;

        val x2 = x1 * cos(angle) - y1 * sin(angle)
        val y2 = x1 * sin(angle) + y1 * cos(angle)

        return CrazyVector(
            x2 + center.x,
            y2 + center.y
        )
    }

    infix fun transformTo(trans: CrazyTransform): CrazyVector {
        var transPoint = this

        trans.translateBefore?.let { transPoint += it }

        trans.center?.let { center ->
            transPoint -= center
            transPoint *= trans.scale

            trans.rotate?.let { transPoint = transPoint.rotateAroundOtherPoint(zero(), it) }

            transPoint += center
        }

        trans.translateAfter?.let { transPoint += it }

        return transPoint
    }

    fun niceString(): String {
        val format = DecimalFormat("#.##")
        return "(${format.format(x)} | ${format.format(y)})"
    }

    fun toLine(pos: CrazyVector) = CrazyLine(pos, pos + this)

    companion object {
        fun zero() = CrazyVector(0.0, 0.0)
        fun square(s: Number) = CrazyVector(s.toDouble(), s.toDouble())
        fun fromAL(a: Double, l: Double) = CrazyVector(cos(a) * l, sin(a) * l)
    }
}