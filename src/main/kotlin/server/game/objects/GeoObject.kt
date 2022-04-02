package server.game.objects

import javafx.scene.canvas.GraphicsContext
import server.adds.math.CrazyVector
import server.adds.debug.DebugObjectOptions
import server.adds.debug.DebugTransform
import server.adds.math.geom.shapes.CrazyShape
import server.adds.math.vec
import server.data_containers.GameObjectType
import java.text.DecimalFormat

abstract class GeoObject(
    type: GameObjectType,
    var pos: CrazyVector,
    var ang: Double = 0.0,
    var velocity: CrazyVector = CrazyVector.zero()
) : AbstractGameObject(type) {
    //protected var effects = arrayListOf<GeoObjectEffect<T>>()

    abstract fun collider(): CrazyShape

    override fun paintDebug(g2: GraphicsContext, transform: DebugTransform, canvasSize: CrazyVector) {
        collider().paintDebug(g2, transform, canvasSize)
    }

    override fun surroundedRect() = collider().surroundedRect()

    override fun debugOptions() = DebugObjectOptions(
        "Asteroid ${getID()}", getID(),
        mapOf(
            "Pos" to pos.niceString(),
            "Angle" to DecimalFormat("##.##").format(ang).toString()
        )
    )

    fun setSpeed(s: Double) { velocity = velocity.e() * s }
    fun setAngle(a: Double) { velocity = vec(a, velocity.length()) }

    override suspend fun calc(s: Double) {
        move()
    }

    fun move() {
        pos += velocity
    }

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