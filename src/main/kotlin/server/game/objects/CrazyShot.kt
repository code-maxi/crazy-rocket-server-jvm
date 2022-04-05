package server.game.objects

import javafx.scene.paint.Color
import server.adds.math.CrazyVector
import server.adds.math.geom.shapes.CrazyLine
import server.adds.math.geom.shapes.ShapeDebugConfig
import server.adds.math.vec
import server.data_containers.AbstractGameObjectI
import server.data_containers.GameObjectType
import server.game.objects.abstct.ColliderObject
import server.game.objects.abstct.GeoObject
import server.game.objects.abstct.VulnerableObjectI

data class CrazyRocketShotConfig(
    val shotType: CrazyShotType,
    val relativePosToRocket: CrazyVector,
    val relativeAngleToRocket: Double,
    val keyCombination: List<String>,
    val rechargingTime: Int,
    val customId: String,
    val speed: Double
)

enum class CrazyShotType(
    val id: String,
    val lifetime: Double,
    val startLength: Double,
    val endLength: Double,
    val color: Color,
    val thickness: Double,
    val mass: Double
) {
    SIMPLE_SHOT(
        "simple-shot",
        0.1,
        1.0,
        0.0,
        Color.RED,
        0.05,
        10.0
    )
}

class CrazyShot(
    pos: CrazyVector, angle: Double, teamColor: String?, speed: Double,
    val shotType: CrazyShotType
) : GeoObject(GameObjectType.SIMPLE_SHOT, pos, velocity = vec(angle, speed, true)) {
    private var life = 100.0
    private var lineCollider = makeCollider()

    override fun getMass() = shotType.mass * life

    override fun collider() = lineCollider

    private fun makeCollider() =
        CrazyLine(pos, pos + velocity.e() * ((shotType.startLength - shotType.endLength) * (life/100.0) + shotType.endLength))
            .setConfig(ShapeDebugConfig(
                paintCoords = false,
                paintPoints = false,
                crazyStyle = ShapeDebugConfig.DEFAULT_CRAZY_STYLE.copy(
                    strokeColor = shotType.color,
                    lineWidth = shotType.thickness * (life/100.0)
                )
            ))

    override suspend fun calc(s: Double) {
        life -= shotType.lifetime * s
        lineCollider = makeCollider()

        super.calc(s)

        for (it in getGame().objects()) {
            if (
                it is VulnerableObjectI &&
                it is ColliderObject &&
                this collides it &&
                !it.ignoredObjectTypes().contains(this.type)
            ) {
                it.onShot(impulsePower(), this)
                killMe()
                break
            }
        }
    }

    override fun data(): AbstractGameObjectI {
        TODO("Not yet implemented")
    }
}