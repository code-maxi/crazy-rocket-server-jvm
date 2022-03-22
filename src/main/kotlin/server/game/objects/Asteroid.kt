package server.game.objects

import javafx.scene.canvas.GraphicsContext
import javafx.scene.paint.Color
import server.adds.CrazyGraphicStyle
import server.data_containers.AsteroidOI
import server.adds.math.CrazyVector
import server.adds.math.geom.debug.DebugTransform
import server.adds.math.geom.shapes.CrazyCircle
import server.adds.math.geom.shapes.CrazyLine
import server.adds.math.geom.shapes.ShapeDebugConfig
import server.data_containers.GameObjectType

class Asteroid(
    val size: Double,
    private val turnSpeed: Double,
    var pos: CrazyVector,
    var ang: Double,
    private var velocity: CrazyVector,
    id: String
) : ColliderObject(id, GameObjectType.ASTEROID) {
    var live = 100.0

    override suspend fun calc(s: Double) {
        ang += turnSpeed
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

    override fun data() = TODO("Data must be initialized first.")
}