package server.adds.math.geom.shapes

import javafx.scene.canvas.GraphicsContext
import server.adds.math.CrazyTransform
import server.adds.math.CrazyVector
import server.adds.math.vec
import server.data_containers.NegativeCoordinateInSizeVector

class CrazyRect(val pos: CrazyVector, val size: CrazyVector, config: ShapeDebugConfig = ShapeDebugConfig()) : CrazyShape(GeomType.RECT, config) {
    init {
        if (pos.x < 0.0 && pos.x < 0.0) throw NegativeCoordinateInSizeVector(size)
    }

    override fun surroundedRect() = this

    override fun transform(trans: CrazyTransform): CrazyShape {
        val tlCorner = pos transformTo trans
        val brCorner = (pos + size) transformTo trans
        return CrazyRect(tlCorner, brCorner - tlCorner)
    }

    override infix fun containsPoint(point: CrazyVector) =
        point.x >= pos.x && point.y >= pos.y && point.x <= pos.x + size.x && point.y <= pos.y + size.y

    fun toPolygon() = RocketPolygon(
        arrayOf(
            pos, vec(pos.x + size.x, pos.y),
            pos + size, vec(pos.x, pos.y + size.y)
        )
    )

    fun width() = size.x
    fun height() = size.y

    override fun paintDebug(g2: GraphicsContext) {
        super.paintDebug(g2)

        g2.fillRect(pos.x, pos.y, size.x, size.y)
        g2.strokeRect(pos.x, pos.y, size.x, size.y)
    }
}