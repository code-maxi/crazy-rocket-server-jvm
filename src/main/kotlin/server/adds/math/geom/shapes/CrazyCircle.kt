package server.adds.math.geom.shapes

import javafx.scene.canvas.GraphicsContext
import server.adds.CrazyGraphics
import server.adds.math.CrazyTransform
import server.adds.math.CrazyVector
import server.adds.math.geom.debug.DebugTransform

class CrazyCircle(
    val radius: Double,
    val pos: CrazyVector,
    config: ShapeDebugConfig? = null
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

    override fun paintSelf(g2: GraphicsContext, transform: DebugTransform, config: ShapeDebugConfig) {
        val screenPos = transform.screen(pos)
        val screenRadius = radius * transform.zoom

        g2.fillOval(screenPos.x - screenRadius, screenPos.y - screenRadius, screenRadius*2, screenRadius*2)
        g2.strokeOval(screenPos.x - screenRadius, screenPos.y - screenRadius, screenRadius*2, screenRadius*2)

        CrazyGraphics.paintPoint(g2, screenPos, paintCoords = config.paintCoords)
    }

    override fun setConfig(shapeDebugConfig: ShapeDebugConfig) = CrazyCircle(radius, pos, shapeDebugConfig)
    //override fun transform(trans: GeomTransform) = RocketCircle(radius * trans.scaling, pos + trans.pos)
}