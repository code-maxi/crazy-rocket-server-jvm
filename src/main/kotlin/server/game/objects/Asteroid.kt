package server.game.objects

import javafx.scene.canvas.GraphicsContext
import javafx.scene.paint.Color
import server.adds.CrazyGraphicStyle
import server.data_containers.AsteroidOI
import server.adds.math.CrazyVector
import server.adds.math.geom.debug.DebugTransform
import server.adds.math.geom.shapes.CrazyCircle
import server.adds.math.geom.shapes.CrazyLine
import server.adds.math.geom.shapes.CrazyShape
import server.adds.math.geom.shapes.ShapeDebugConfig

class Asteroid(
    val size: Int,
    pos: CrazyVector,
    ang: Double,
    velocity: CrazyVector,
    id: String
) : GeoObject(pos, 0.0, 0.0, ang, velocity, id) {
    var live = 1.0
    val turnSpeed = Math.random() * 0.03 + 0.01

    init {
        val rSize = size * 50.0
        width = rSize
        height= rSize
    }

    override suspend fun calc(s: Double) {
        ang += turnSpeed

        super.calc(s)
    }

    override fun collider() = CrazyCircle(size * 50.0, pos)

    override fun paintDebug(g2: GraphicsContext, transform: DebugTransform, canvasSize: CrazyVector) {
        collider().setConfig(ShapeDebugConfig(
            paintSurroundedRect = true,
            debugOptions = debugOptions()
        )).paintDebug(g2, transform, canvasSize)

        CrazyLine(pos, pos + velocity * 10.0, ShapeDebugConfig(
            CrazyGraphicStyle(strokeColor = Color.GREEN, lineWidth = 2.0),
            drawLineAsVector = true
        )).paintDebug(g2, transform, canvasSize)
    }

    override fun data() = AsteroidOI(
        live, size, id, getGeo()
    )
}