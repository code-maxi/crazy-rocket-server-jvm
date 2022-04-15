package server.game.objects

import javafx.scene.canvas.GraphicsContext
import javafx.scene.paint.Color
import server.adds.CrazyGraphicStyle
import server.adds.debug.DebugObjectOptions
import server.adds.math.CrazyVector
import server.adds.debug.DebugTransform
import server.adds.math.niceString
import server.adds.math.geom.shapes.CrazyCircle
import server.adds.math.geom.shapes.CrazyLine
import server.adds.math.geom.shapes.ShapeDebugConfig
import server.adds.math.vec
import server.data_containers.GameObjectType
import server.game.objects.abstct.GeoObject
import kotlin.math.PI

class CrazyAsteroid(
    val size: Double,
    private val rotation: Double,
    pos: CrazyVector,
    ang: Double,
    velocity: CrazyVector
) : GeoObject(GameObjectType.ASTEROID, pos, ang, velocity), ShotVulnerableObject {
    private val startStability = getMass() * PROPORTION_STABILITY_MASS
    private var stability = startStability
    var collisionDone = false

    override fun getMass() = size * size * PI

    fun shrinkStability(energy: Double) {
        stability -= energy
    }

    override suspend fun calc(factor: Double, step: Int) {
        super.calc(factor, step)

        if (step == 1) {
            ang += rotation * factor
            if (pos.x < 0) pos = pos.copy(x = getGame().size().x)
            if (pos.y < 0) pos = pos.copy(y = getGame().size().y)
            if (pos.x > getGame().size().x) pos = pos.copy(x = 0.0)
            if (pos.y > getGame().size().y) pos = pos.copy(y = 0.0)
            collisionDone = false
        }

        else if (step == 2 && !collisionDone) {
            val asteroids = getGame().objectsOfType(CrazyAsteroid::class)

            for (that in asteroids) {
                if (that !== this && !that.collisionDone) {
                    if (this collides that) {
                        val collisionResult = this.handlePartiallyElasticCollision(that, ASTEROID_COLLISION_FACTOR)

                        if (collisionResult != null) {
                            this.velocity = collisionResult.nv1
                            that.velocity = collisionResult.nv2

                            this.shrinkStability(collisionResult.energyLost/2.0)
                            that.shrinkStability(collisionResult.energyLost/2.0)

                            /*log("Collision between $this and $that.")
                            log(elasticCollision.toString())
                            log()*/

                            that.collisionDone = true

                            break
                        }
                    }
                }
            }

            collisionDone = true
        }

        else if (step == 3) {
            if (stability <= 0) {
                suicide()
                log("I killed Myself!")
            }
        }
    }

    override fun collider() = CrazyCircle(size, pos)

    override fun shapeDebugConfig() = ShapeDebugConfig()

    override fun paintDebug(g2: GraphicsContext, transform: DebugTransform, canvasSize: CrazyVector) {
        super.paintDebug(g2, transform, canvasSize)

        val s = if (stability < 0) 0.0 else stability
        val progressSize = vec(40, 10)
        val progressBarSize = progressSize mulX (s / startStability)
        val progressPos = transform.screen(pos) - progressSize/2 addY 10

        g2.stroke = Color.BLACK
        g2.lineWidth = 0.5

        g2.fill = Color.WHITE
        g2.fillRect(progressPos.x, progressPos.y, progressSize.x, progressSize.y)

        g2.fill = Color.YELLOW
        g2.fillRect(progressPos.x, progressPos.y, progressBarSize.x, progressBarSize.y)
        g2.strokeRect(progressPos.x, progressPos.y, progressBarSize.x, progressBarSize.y)

        g2.strokeRect(progressPos.x, progressPos.y, progressSize.x, progressSize.y)

        CrazyLine(pos, pos + velocity / 2.0, ShapeDebugConfig(
            CrazyGraphicStyle(strokeColor = Color.GREEN, lineWidth = 2.0),
            drawLineAsVector = true
        )).paintDebug(g2, transform, canvasSize)
    }

    override fun debugOptions() = DebugObjectOptions(
        "Asteroid (${getID()})", getID(),
        mapOf(
            "Stability" to stability.niceString(),
            "Stability/Start Stability" to (stability / startStability).niceString(),
            "Mass" to getMass().niceString()
        )
    )

    override fun data() = TODO("Not yet implemented.")

    companion object {
        const val ASTEROID_COLLISION_FACTOR = 1.0
        const val PROPORTION_STABILITY_MASS = 10.0
    }

    override fun onShot(collisionEnergy: Double, shot: CrazyShot) {
        shrinkStability(collisionEnergy)
    }
}