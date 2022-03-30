package server.adds.math.geom.shapes

import javafx.scene.canvas.GraphicsContext
import server.adds.math.CollisionDetection
import server.adds.math.CrazyTransform
import server.adds.math.CrazyVector
import server.adds.math.geom.debug.DebugTransform
import server.adds.math.vec
import server.data_containers.NegativeCoordinateInSizeVector

class CrazyRect(val pos: CrazyVector, val size: CrazyVector, config: ShapeDebugConfig? = ShapeDebugConfig()) : CrazyShape(ShapeType.RECT, config) {
    init {
        if (size.x < 0.0 || size.y < 0.0) throw NegativeCoordinateInSizeVector(size)
    }

    override fun surroundedRect() = this

    override fun transform(trans: CrazyTransform): CrazyShape {
        val tlCorner = pos transformTo trans
        val brCorner = (pos + size) transformTo trans
        return CrazyRect(tlCorner, brCorner - tlCorner)
    }

    override infix fun containsPoint(point: CrazyVector) =
        point.x >= pos.x && point.y >= pos.y && point.x <= pos.x + size.x && point.y <= pos.y + size.y

    fun toPolygon() = CrazyPolygon(
        arrayOf(
            pos, vec(pos.x + size.x, pos.y),
            pos + size, vec(pos.x, pos.y + size.y)
        )
    )

    fun center() = pos + size / 2.0

    fun width() = size.x
    fun height() = size.y

    infix fun touchesRect(that: CrazyRect) = CollisionDetection.rectRectCollision(this, that)

    override fun paintSelf(g2: GraphicsContext, transform: DebugTransform, config: ShapeDebugConfig) {
        val screenPos = transform.screen(pos) - vec(0, size.y * transform.zoom)

        if (config.crazyStyle.fillOpacity != null) g2.fillRect(screenPos.x, screenPos.y, size.x * transform.zoom, size.y * transform.zoom)
        if (config.crazyStyle.strokeOpacity != null) g2.strokeRect(screenPos.x, screenPos.y, size.x * transform.zoom, size.y * transform.zoom)
    }

    fun copy(pos: CrazyVector = this.pos, size: CrazyVector = this.size, config: ShapeDebugConfig? = this.config) =
        CrazyRect(pos, size, config)

    override fun setConfig(shapeDebugConfig: ShapeDebugConfig?) = CrazyRect(pos, size, shapeDebugConfig)
}