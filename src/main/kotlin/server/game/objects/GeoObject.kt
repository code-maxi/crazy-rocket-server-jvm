package server.game.objects

import server.data_containers.GameObjectI
import server.adds.math.geom.GeoI
import server.adds.math.CrazyVector
import server.adds.math.geom.shapes.CrazyRect
import server.adds.math.vec

abstract class GeoObject(
    var pos: CrazyVector,
    var width: Double,
    var height: Double,
    var ang: Double,
    var velocity: CrazyVector,
    id: String
): GameObjectI(id) {
    //protected var effects = arrayListOf<GeoObjectEffect<T>>()

    fun setSpeed(s: Double) { velocity = velocity.e() * s }
    fun setAngle(a: Double) { velocity = vec(a, velocity.length()) }

    override suspend fun calc(s: Double) {
        pos += velocity * s
    }

    fun getGeo() = GeoI(pos, width, height, ang)
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