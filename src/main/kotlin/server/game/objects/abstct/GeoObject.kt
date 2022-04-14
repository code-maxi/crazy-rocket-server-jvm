package server.game.objects.abstct

import server.adds.math.CrazyVector
import server.adds.math.geom.shapes.CrazyLine
import server.adds.math.geom.shapes.CrazyShape
import server.adds.math.vec
import server.data_containers.GameObjectType
import server.data_containers.GeoObjectI

abstract class GeoObject(
    type: GameObjectType,
    var pos: CrazyVector,
    var ang: Double = 0.0,
    var velocity: CrazyVector = CrazyVector.zero()
) : ColliderObject(type) {
    abstract fun getMass(): Double

    fun impulsePower() = velocity.length() * getMass()
    
    fun setSpeed(s: Double) { velocity = velocity.e() * s }
    fun setAngle(a: Double) { velocity = vec(a, velocity.length()) }
    
    fun ricochetOnLine(line: CrazyLine, coll: CrazyShape = collider()) {
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

    infix fun handleElasticCollision(that: GeoObject) {

    }

    override suspend fun calc(s: Double) {
        move()
    }

    fun move() { pos += velocity }

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