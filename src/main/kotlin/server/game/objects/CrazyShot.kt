package server.game.objects

import javafx.scene.paint.Color
import javafx.scene.shape.StrokeLineCap
import server.adds.math.CrazyCollision
import server.adds.math.CrazyVector
import server.adds.math.geom.shapes.CrazyLine
import server.adds.math.geom.shapes.ShapeDebugConfig
import server.adds.math.vec
import server.data_containers.AbstractGameObjectI
import server.data_containers.GameObjectType
import server.game.objects.abstct.GeoObject

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
    val mass: Double,
    val stability: Double,
    val shrinkLinear: Boolean,
    val explosionImpulsePower: Double
) {
    SIMPLE_SHOT(
        "simple-shot",
        0.05,
        2.0,
        1.0,
        Color.RED,
        2.0,
        0.5,
        5.0,
        true,
        10.0
    )
}

interface ShotVulnerableObject {
    fun onShot(collisionEnergy: Double, shot: CrazyShot)
}

class CrazyShot(
    pos: CrazyVector, val angle: Double, teamColor: String?, speed: Double,
    startVelocity: CrazyVector, val shotType: CrazyShotType
) : GeoObject(GameObjectType.SIMPLE_SHOT, pos, velocity = startVelocity + vec(angle, speed, true)) {
    private val startStartStability = shotType.stability
    private var stability = startStartStability
    private var lineCollider = makeCollider()

    private fun life() = stability / startStartStability
    private fun shotLength() = (shotType.startLength - shotType.endLength) * life() + shotType.endLength

    override fun getMass() = shotType.mass * (if (shotType.shrinkLinear) life() else 1.0)

    override fun collider() = lineCollider

    private fun makeCollider() =
        CrazyLine(pos, pos + vec(angle, shotLength(), true))
            .setConfig(ShapeDebugConfig(
                paintCoords = false,
                paintPoints = false,
                crazyStyle = ShapeDebugConfig.DEFAULT_CRAZY_STYLE.copy(
                    strokeColor = shotType.color,
                    lineWidth = shotType.thickness * life(),
                    lineCap = StrokeLineCap.ROUND
                )
            ))

    override suspend fun calc(factor: Double, step: Int) {
        if (step == 1) {
            stability -= shotType.lifetime * factor
            move(factor)
            lineCollider = makeCollider()
        }
        else if (step == 2) {
            val objects = getGame().geoObjects()

            for (that in objects) {
                if (that !== this && that is ShotVulnerableObject) {
                    if (this collides that) {
                        val collisionResult = CrazyCollision.partiallyElasticCollision2Dv2(
                            this.getMass(), this.velocity, this.pos,
                            that.getMass(), that.velocity, that.pos,
                            SHOT_ASTEROID_COLLISION_FACTOR,
                            this.velocity.e() * shotType.explosionImpulsePower * (if (shotType.shrinkLinear) life() else 1.0)
                        )

                        if (collisionResult != null) {
                            that.velocity = collisionResult.nv2
                            that.onShot(collisionResult.energyLost/2.0 + kinEnergy(), this)
                            suicide()
                        }
                    }
                }
            }
        }
        else if (step == 3 && stability < 0) {
            suicide()
        }
    }

    override fun data(): AbstractGameObjectI {
        TODO("Not yet implemented")
    }

    companion object {
        const val SHOT_ASTEROID_COLLISION_FACTOR = 0.5
    }
}