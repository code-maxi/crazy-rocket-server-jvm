package server.adds.math.geom.shapes

import javafx.scene.canvas.GraphicsContext
import server.adds.CrazyGraphics
import server.adds.math.CrazyTransform
import server.adds.math.CrazyVector
import server.adds.math.geom.debug.DebugTransform

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

    override fun paintDebug(g2: GraphicsContext, transform: DebugTransform) {
        super.paintDebug(g2, transform)

        val screenPos = transform.screen(pos)
        val screenRadius = radius * transform.zoom

        if (config.fillOpacity != null) g2.fillOval(screenPos.x - screenRadius, screenPos.y - screenRadius, screenRadius*2, screenRadius*2)
        if (config.stroke) g2.strokeOval(screenPos.x - screenRadius, screenPos.y - screenRadius, screenRadius*2, screenRadius*2)

        CrazyGraphics.paintPoint(g2, screenPos, color = config.color, name = "M", paintCoords = config.paintCoords)

        if (config.paintSurroundedRect) paintSurroundedRect(g2, transform)
    }
    //override fun transform(trans: GeomTransform) = RocketCircle(radius * trans.scaling, pos + trans.pos)
}