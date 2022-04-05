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
import server.data_containers.GameObjectType
import server.game.objects.abstct.GeoObject
import server.game.objects.abstct.VulnerableObjectI

class CrazyAsteroid(
    val size: Double,
    private val rotation: Double,
    pos: CrazyVector,
    ang: Double,
    velocity: CrazyVector
) : GeoObject(GameObjectType.ASTEROID, pos, ang, velocity), VulnerableObjectI {
    var stability: Double = size

    override fun getMass() = size

    override fun onShot(shotEnergy: Double, shot: CrazyShot) {
        stability -= shotEnergy + impulsePower()
        log("On Shot!")
    }

    override fun ignoredObjectTypes() = listOf<GameObjectType>()

    override suspend fun calc(s: Double) {
        ang += rotation * s
        super.calc(s)

        if (pos.x < 0) pos = pos.copy(x = getGame().size().x)
        if (pos.y < 0) pos = pos.copy(y = getGame().size().y)
        if (pos.x > getGame().size().x) pos = pos.copy(x = 0.0)
        if (pos.y > getGame().size().y) pos = pos.copy(y = 0.0)
    }

    override fun collider() = CrazyCircle(size, pos)

    override fun shapeDebugConfig() = ShapeDebugConfig()

    override fun paintDebug(g2: GraphicsContext, transform: DebugTransform, canvasSize: CrazyVector) {
        super.paintDebug(g2, transform, canvasSize)

        CrazyLine(pos, pos + velocity * 10.0, ShapeDebugConfig(
            CrazyGraphicStyle(strokeColor = Color.GREEN, lineWidth = 2.0),
            drawLineAsVector = true
        )).paintDebug(g2, transform, canvasSize)
    }

    override fun debugOptions() = DebugObjectOptions(
        "Asteroid", getID(),
        mapOf(
            "Stability" to stability.niceString()
        )
    )

    override fun data() = TODO("Not yet implemented.")
}