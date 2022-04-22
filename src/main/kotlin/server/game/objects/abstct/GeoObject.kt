package server.game.objects.abstct

import server.adds.math.CrazyCollision
import server.adds.math.CrazyVector
import server.adds.math.geom.shapes.CrazyLine
import server.adds.math.geom.shapes.CrazyShape
import server.adds.math.vec
import server.data_containers.GameObjectType

abstract class GeoObject(
    type: GameObjectType,
    var pos: CrazyVector,
    var ang: Double = 0.0,
    var velocity: CrazyVector = CrazyVector.zero()
) : ColliderObject(type) {
    abstract fun getMass(): Double

    fun impulse() = velocity * getMass()

    fun kinEnergy(): Double {
        val velLength = velocity.length()
        return (velLength * velLength * getMass()) / 2.0
    }

    
    fun setSpeed(s: Double) { velocity = velocity.e() * s }
    fun setAngle(a: Double) { velocity = vec(a, velocity.length()) }
    
    suspend fun ricochetOnLine(line: CrazyLine, coll: CrazyShape = collider()) {
        if (line.surroundedRect() touchesRect coll.surroundedRect()) {
            val cpa = coll containsPoint line.a
            val cpb = coll containsPoint line.b

            if (cpa) {
                velocity = velocity.ricochetVelocity((pos - line.a).normalRight(), false)
            }
            else if (cpb) {
                velocity = velocity.ricochetVelocity((pos - line.b).normalRight(), false)
            }
            else {
                val posRightOfThat = line isPointRight pos
                val ints = line intersection (line normalLineFrom pos)
                val orthogonalTouch = ints.onLine1 && coll containsPoint ints.intersection

                if (orthogonalTouch) {
                    velocity = velocity.ricochetVelocity(line.toVec(), posRightOfThat)
                }
            }
        }
    }

    suspend fun handlePartiallyElasticCollision(that: GeoObject, k: Double = 1.0) =
        CrazyCollision.partiallyElasticCollision2Dv2(this.getMass(), this.velocity, this.pos, that.getMass(), that.velocity, that.pos, k)

    infix fun movingAwayFrom(that: GeoObject) = (that.pos + that.velocity - this.pos - this.velocity).length() > (that.pos - this.pos).length()

    override suspend fun calc(factor: Double, step: Int) {
        if (step == 1) move(factor)
    }

    fun move(factor: Double) { pos += velocity.stepSpeed() * factor }

    fun getGeo() = collider().surroundedRect().toGeo(ang)
}

/*
abstract class GeoObjectEffectI(
    val setValue: (d: Double) -> Unit,
    val getValue: () -> Double
) {
    lateinit var killMe: () -> Unit
    abstract fun effect(s: Double)
}

class GeoObjectLinearEffect(
    setValue: (d: Double) -> Unit,
    getValue: () -> Double,
    val target: Double,
    val speed: Double,
    val yoyo: Boolean = false
) : GeoObjectEffectI(setValue, getValue) {
    override fun effect(s: Double) {
        val value = getValue()
        if (inRange(value, target, range ?: speed)) target
        else if (value > target && linear) value - speed
        else (value < target && linear) value + speed
    }
}

data class GeoObjectEffect<T : TypeObjectI>(
    val targetValue: Double,
    val getValue: GeoObjectI<T>.() -> Double,
    val setValue: GeoObjectI<T>.(v: Double) -> Unit
)
*/