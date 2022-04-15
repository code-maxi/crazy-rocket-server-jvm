package server.adds.math.geom.shapes

import javafx.scene.canvas.GraphicsContext
import javafx.scene.paint.Color
import server.adds.CrazyGraphicStyle
import server.adds.CrazyGraphics
import server.adds.debug.DebugObjectOptions
import server.adds.math.CrazyCollision
import server.adds.math.CrazyTransform
import server.adds.math.CrazyVector
import server.adds.debug.DebugTransform
import server.adds.math.geom.GeoI
import server.adds.math.vec
import server.data_containers.NegativeCoordinateInSizeVector

class CrazyRect(val pos: CrazyVector, val size: CrazyVector, config: ShapeDebugConfig? = ShapeDebugConfig()) : CrazyShape(ShapeType.RECT, config) {
    init {
        if (size.x < 0.0 || size.y < 0.0) throw NegativeCoordinateInSizeVector(size)
    }

    override fun surroundedRect() = this

    override fun transform(trans: CrazyTransform): CrazyRect {
        val tlCorner = pos transformTo trans
        val brCorner = (pos + size) transformTo trans
        return CrazyRect(tlCorner, brCorner - tlCorner)
    }

    override infix fun containsPoint(point: CrazyVector) =
        point.x >= pos.x && point.y >= pos.y && point.x <= pos.x + size.x && point.y <= pos.y + size.y

    fun toPolygon() = CrazyPolygon(
        listOf(
            pos, vec(pos.x + size.x, pos.y),
            pos + size, vec(pos.x, pos.y + size.y)
        )
    )

    fun center() = pos + size / 2.0

    fun width() = size.x
    fun height() = size.y

    infix fun touchesRect(that: CrazyRect) = CrazyCollision.rectRectCollision(this, that)

    override fun paintSelf(g2: GraphicsContext, transform: DebugTransform, config: ShapeDebugConfig) {
        val screenPos = transform.screen(pos)

        if (config.crazyStyle.fillOpacity != null) g2.fillRect(screenPos.x, screenPos.y, size.x * transform.zoom, size.y * transform.zoom)
        if (config.crazyStyle.strokeOpacity != null) g2.strokeRect(screenPos.x, screenPos.y, size.x * transform.zoom, size.y * transform.zoom)

        if (config.paintPoints) {
            val paintPoint = { x: Int, y: Int ->
                val p = pos + vec(x, y) * size
                CrazyGraphics.paintPoint(
                    g2, transform.screen(p),
                    name = if (config.paintPointNames) "${if (x == 0) "L" else "R"}${if (y == 0) "T" else "B"}" else null,
                    coordinates = if (config.paintCoords) p else null
                )
            }
            paintPoint(0,0)
            paintPoint(1,0)
            paintPoint(1,1)
            paintPoint(0,1)
        }
    }

    fun copy(pos: CrazyVector = this.pos, size: CrazyVector = this.size, config: ShapeDebugConfig? = this.config) =
        CrazyRect(pos, size, config)

    override fun setConfig(shapeDebugConfig: ShapeDebugConfig?) = CrazyRect(pos, size, shapeDebugConfig)

    override fun shapeString() = "Rect(pos = $pos, size = ${size.niceString()})"

    fun toGeo(angle: Double) = GeoI(pos, size.x, size.y, angle)

    fun left() = pos.x
    fun top() = pos.y
    fun right() = pos.x + size.x
    fun bottom() = pos.y + size.y

    override fun setColor(c: Color) = super.setColor(c) as CrazyRect
    override fun setZIndex(i: Int) = super.setZIndex(i) as CrazyRect
    override fun setDebugConfig(options: DebugObjectOptions) = super.setDebugConfig(options) as CrazyRect
    override fun setCrazyStyle(style: CrazyGraphicStyle) = super.setCrazyStyle(style) as CrazyRect
}