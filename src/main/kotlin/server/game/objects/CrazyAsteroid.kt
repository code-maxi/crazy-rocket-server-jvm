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
import server.data_containers.ClientCanvasObjectD
import server.data_containers.GameObjectTypeE
import server.game.data.*
import server.game.objects.abstct.GeoObject
import kotlin.math.PI

data class AsteroidPropsD(
    val size: Double,
    val life: Double,
)

data class AsteroidOD(
    override val id: String,
    override val props: AsteroidPropsD?,
    override val paint: List<PaintDataD>?
) : ClientCanvasObjectD {
    override val type = GameObjectTypeE.ASTEROID
}

class CrazyAsteroid(
    val radius: Double,
    private val rotation: Double,
    pos: CrazyVector,
    ang: Double,
    velocity: CrazyVector
) : GeoObject(GameObjectTypeE.ASTEROID, pos, ang, velocity), ShotVulnerableObject {
    private val startStability = getMass() * PROPORTION_STABILITY_MASS
    private var stability = startStability
    private var collisionDone = false

    override fun getMass() = radius * radius * PI

    private fun shrinkStability(energy: Double) {
        stability -= energy
    }

    override suspend fun calc(factor: Double, step: Int) {
        super.calc(factor, step)

        if (step == 1) {
            ang += rotation * factor
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

    fun sizeRect() = CrazyVector.square(radius) * 2

    override fun collider() = CrazyCircle(radius, pos)

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

    override fun propsData() = null

    override fun paintedData() = listOf(
        PaintDataD(
            PaintTypeE.IMAGE,
            this.pos,
            this.zIndex,
            ImagePropsD(
                src = "asteroid.png",
                size = sizeRect()
            )
        ),
        PaintDataD(
            PaintTypeE.PROCESS,
            this.pos,
            this.zIndex,
            ProcessPropsD(
                value = stability / startStability,
                size = CRAZY_PROCESS_SIZE_FOR_RADIUS_1 * this.radius,
                roundedCorners = 5,
                fColor1 = "255,0,0",
                bColor = "0,0,0,0.5",
                borderColor = "0,100,255"
            ),
            ignoresTrans = true
        )
    )

    override fun paintedMapIcon() = PaintMapIcon(
        zIndex,
        ClientMapIcons.POINT,
        this.pos,
        PointPropsD(
            this.radius,
            "0,100,255,0.3"
        )
    )

    override fun onShot(collisionEnergy: Double, shot: CrazyShot) {
        shrinkStability(collisionEnergy)
    }

    companion object {
        val CRAZY_PROCESS_SIZE_FOR_RADIUS_1 = vec(0.5, 0.1)
        const val ASTEROID_COLLISION_FACTOR = 1.0
        const val PROPORTION_STABILITY_MASS = 100.0
    }
}