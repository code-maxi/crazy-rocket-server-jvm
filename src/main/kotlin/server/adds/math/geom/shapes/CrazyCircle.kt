package server.adds.math.geom.shapes

import javafx.scene.canvas.GraphicsContext
import server.adds.JavaFXGraphics
import server.adds.math.CrazyTransform
import server.adds.math.CrazyVector

class CrazyCircle(
    val radius: Double,
    val pos: CrazyVector,
    config: ShapeDebugConfig = ShapeDebugConfig()
) : CrazyShape(GeomType.CIRCLE, config) {
    override fun surroundedRect() = CrazyRect(
        pos - CrazyVector.square(radius),
        CrazyVector.square(radius * 2)
    )

    override fun transform(trans: CrazyTransform) = CrazyCircle(
        radius * trans.scale,
        pos transformTo trans
    )

    override fun containsPoint(point: CrazyVector) = point distance pos < radius

    override fun paintDebug(g2: GraphicsContext) {
        super.paintDebug(g2)

        g2.fillOval(pos.x - radius, pos.y - radius, radius*2, radius*2)
        g2.strokeOval(pos.x - radius, pos.y - radius, radius*2, radius*2)
        JavaFXGraphics.paintPoint(g2, pos, color = config.color, name = "M", paintCoords = config.paintCoords)
    }
    //override fun transform(trans: GeomTransform) = RocketCircle(radius * trans.scaling, pos + trans.pos)
}