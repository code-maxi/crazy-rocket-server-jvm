package server.adds.math

import server.adds.math.geom.shapes.CrazyLine
import java.text.DecimalFormat
import kotlin.math.*

data class CrazyVector(val x: Double, val y: Double) {
    operator fun plus(v: CrazyVector) = CrazyVector(this.x + v.x, this.y + v.y)
    operator fun minus(v: CrazyVector) = CrazyVector(this.x - v.x, this.y - v.y)
    operator fun times(s: Number) = CrazyVector(this.x * s.toDouble(), this.y * s.toDouble())
    operator fun times(v: CrazyVector) = CrazyVector(this.x * v.x, this.y * v.y)
    operator fun div(s: Number) = CrazyVector(this.x / s.toDouble(), this.y / s.toDouble())
    operator fun div(v: CrazyVector) = CrazyVector(this.x / v.x, this.y / v.y)
    operator fun unaryMinus() = this * -1.0

    infix fun mulX(s: Number) = CrazyVector(x*s.toDouble(), y)
    infix fun mulY(s: Number) = CrazyVector(x, y*s.toDouble())
    infix fun addX(s: Number) = CrazyVector(x+s.toDouble(), y)
    infix fun addY(s: Number) = CrazyVector(x, y+s.toDouble())

    fun square() = CrazyVector(x*x, y*y)

    fun randomPosInSquare(n: Int): CrazyVector {
        val r1 = bellRandom(n)
        val r2 = bellRandom(n)
        return CrazyVector(this.x * r1, this.y * r2)
    }

    fun length() = sqrt(this.x * this.x + this.y * this.y)
    fun angle() = atan2(y, x)
    fun e(): CrazyVector {
        val length = length()
        return CrazyVector(this.x / length, this.y / length)
    }
    fun abs() = CrazyVector(this.x.abs(), this.y.abs())

    infix fun distance(v: CrazyVector) = (v - this).length()
    infix fun scalar(v: CrazyVector) = this.x * v.x + this.y * v.y

    fun selfScalar() = this scalar this

    fun addAll(vararg vs: CrazyVector): CrazyVector {
        var o = this.copy()
        vs.forEach { v -> o += v }
        return o
    }

    fun normalRight() = CrazyVector(-this.y, this.x)
    fun normalLeft() = CrazyVector(this.y, -this.x)

    infix fun setX(xp: Number) = CrazyVector(xp.toDouble(), this.y)
    infix fun setY(yp: Number) = CrazyVector(this.x, yp.toDouble())

    infix fun rotate(angle: Double) = vec(
        x * cos(angle) - y * sin(angle),
        x * sin(angle) + y * cos(angle)
    )

    private fun rotateAroundOtherPoint(center: CrazyVector, angle: Double): CrazyVector {
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

    override fun toString() = niceString()

    fun toLine(pos: CrazyVector) = CrazyLine(pos, pos + this)

    infix fun isVecRight(that: CrazyVector) = this.normalRight() scalar that > 0

    infix fun angleTo(that: CrazyVector) = acos((this scalar that) / (this.length() * that.length()))

    fun ricochetVelocity(that: CrazyVector, posRightOfThat: Boolean): CrazyVector {
        val velocityRightOfThat = that.normalRight() scalar this > 0
        val velocityRemoving = velocityRightOfThat == posRightOfThat

        return if (!velocityRemoving) {
            val velocityRightOfNormal = -that scalar this > 0
            val angleFac = (if (velocityRightOfNormal) -1 else 1)
            val angle = (that.normalRight() angleTo this)
            (-this) rotate (2 * angle * angleFac)
        } else this
    }

    fun higherCoordinate() = if (x > y) x else y

    fun stepSpeed() = this / VELOCITY_SPEED


    companion object {
        fun zero() = CrazyVector(0.0, 0.0)
        fun square(s: Number) = CrazyVector(s.toDouble(), s.toDouble())
        fun fromAL(a: Double, l: Number) = CrazyVector(cos(a) * l.toDouble(), sin(a) * l.toDouble())

        const val VELOCITY_SPEED = 50

        fun ricochet(center: CrazyVector, pos: CrazyVector, velocity: CrazyVector): CrazyVector {
            val removing = (center - pos) scalar velocity > 0
            if (removing) return velocity
            val angle = (pos - center) angleTo velocity
            return (-velocity) rotate (-2 * angle)
        }
    }
}