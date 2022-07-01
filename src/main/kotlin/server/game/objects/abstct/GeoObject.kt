package server.game.objects.abstct

import server.adds.math.CrazyCollision
import server.adds.math.CrazyVector
import server.adds.math.geom.shapes.CrazyLine
import server.adds.math.geom.shapes.CrazyShape
import server.adds.math.vec

/**
 * I'm an abstract class that offers Newton physics.
 */

abstract class GeoObject(
    type: GameObjectTypeE,
    var pos: CrazyVector,
    var ang: Double = 0.0,
    var velocity: CrazyVector = CrazyVector.zero()
) : ColliderObject(type) {
    /**
     * Returns the mass of the object.
     */
    abstract fun getMass(): Double

    /**
     * Returns the impulse of the object.
     */
    fun impulse() = velocity * getMass()

    /**
     * Returns the kinetic energy of the object.
     */
    fun kinEnergy(): Double {
        val velLength = velocity.length()
        return (velLength * velLength * getMass()) / 2.0
    }

    /**
     * Sets the length of the velocity vector.
     * @param s
     */
    fun setSpeed(s: Double) { velocity = velocity.e() * s }

    /**
     * Sets the angle of the velocity vector.
     * @param a
     */
    fun setAngle(a: Double) { velocity = vec(a, velocity.length()) }

    /**
     * Ricochets the object on a line.
     * @param line the line the object is ricocheted
     * @param coll an optional parameter to define another collider
     */
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

    /**
     * Handles a partially elastic collision between itself and another object.
     * @param that another object
     * @param k the factor how much energy will be lost
     */
    fun handlePartiallyElasticCollision(that: GeoObject, k: Double = 1.0) =
        CrazyCollision.partiallyElasticCollision2Dv2(this.getMass(), this.velocity, this.pos, that.getMass(), that.velocity, that.pos, k)

    override suspend fun calc(factor: Double, step: Int) {
        if (step == 1) move(factor)
    }

    /**
     * Moves the object by its velocity vector. When the object arrives a border it is flipped to the opposite border.
     * @param factor the calc factor of the game
     */
    fun move(factor: Double) {
        pos += velocity.stepSpeed() * factor

        val gameSize = getGame().size()
        if (pos.x < 0) pos = pos setX (gameSize.x - pos.x)
        if (pos.x > gameSize.x) pos = pos setY (pos.x - gameSize.x)
        if (pos.y < 0) pos = pos setY (gameSize.y - pos.y)
        if (pos.y > gameSize.y) pos = pos setY (pos.y - gameSize.y)
    }

    fun getGeo() = collider().surroundedRect().toGeo(ang)

    override fun data() = mapOf(
        "pos" to pos,
        "ang" to ang,
        "srPos" to surroundedRect().pos,
        "srSize" to surroundedRect().size
    ) + super.data()
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