package server.adds.math.geom.shapes

import javafx.scene.canvas.GraphicsContext
import javafx.scene.paint.Color
import server.adds.CrazyGraphicStyle
import server.adds.CrazyGraphics
import server.adds.debug.DebugObjectOptions
import server.adds.math.CrazyTransform
import server.adds.math.CrazyVector
import server.adds.debug.DebugTransform

class CrazyCircle(
    val radius: Double,
    val pos: CrazyVector,
    config: ShapeDebugConfig? = null
) : CrazyShape(ShapeType.CIRCLE, config) {

    override fun surroundedRect() = CrazyRect(
        pos - CrazyVector.square(radius),
        CrazyVector.square(radius * 2)
    )

    override fun transform(trans: CrazyTransform) = CrazyCircle(
        radius * trans.scale.x,
        pos transformTo trans
    )

    override fun isSurroundedByCircle(circle: CrazyCircle) =
        (circle.pos distance this.pos) + this.radius < circle.radius

    override fun containsPoint(point: CrazyVector) = point distance pos < radius

    override fun paintSelf(g2: GraphicsContext, transform: DebugTransform, config: ShapeDebugConfig) {
        val screenPos = transform.screen(pos)
        val screenRadius = radius * transform.zoom

        g2.fillOval(screenPos.x - screenRadius, screenPos.y - screenRadius, screenRadius*2, screenRadius*2)
        g2.strokeOval(screenPos.x - screenRadius, screenPos.y - screenRadius, screenRadius*2, screenRadius*2)

        if (config.paintPoints) CrazyGraphics.paintPoint(g2, screenPos, name = if (config.paintPointNames) "M" else null, coordinates = if (config.paintCoords) pos else null)
    }

    fun copy(radius: Double = this.radius, pos: CrazyVector = this.pos, config: ShapeDebugConfig? = this.config) =
        CrazyCircle(radius, pos, config)

    override fun shapeString() = "Circle(radius = $radius, pos = ${pos.niceString()})"

    override fun setConfig(config: ShapeDebugConfig?) = CrazyCircle(radius, pos, config)
    //override fun transform(trans: GeomTransform) = RocketCircle(radius * trans.scaling, pos + trans.pos)

    override fun setColor(c: Color) = super.setColor(c) as CrazyCircle
    override fun setZIndex(i: Int) = super.setZIndex(i) as CrazyCircle
    override fun setDebugConfig(options: DebugObjectOptions) = super.setDebugConfig(options) as CrazyCircle
    override fun setCrazyStyle(style: CrazyGraphicStyle) = super.setCrazyStyle(style) as CrazyCircle
}