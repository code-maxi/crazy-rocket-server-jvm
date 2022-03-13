package server.adds.math.geom.shapes

import javafx.scene.canvas.GraphicsContext
import server.adds.CrazyGraphics
import server.adds.math.CrazyTransform
import server.adds.math.CrazyVector
import server.adds.math.geom.debug.DebugTransform
import server.adds.math.vec
import server.data_containers.CannotCheckPointOnLine

class CrazyLine(val a: CrazyVector, val b: CrazyVector, config: ShapeDebugConfig? = null) : CrazyShape(GeomType.LINE, config) {

    private fun ltRectCorner() = vec(if (a.x < b.x) a.x else b.x, if (a.y < b.y) a.y else b.y)
    private fun brRectCorner() = vec(if (a.x > b.x) a.x else b.x, if (a.y > b.y) a.y else b.y)

    override fun surroundedRect() = CrazyRect(
        ltRectCorner(),
        brRectCorner() - ltRectCorner()
    )

    override fun transform(trans: CrazyTransform) = CrazyLine(
        a transformTo trans,
        b transformTo trans
    )

    override fun containsPoint(point: CrazyVector): Boolean {
        throw CannotCheckPointOnLine()
    }

    infix fun pointRightOnLine(point: CrazyVector): Boolean {
        val vec1 = (b - a).normalRight()
        val vec2 = point - a
        return vec1 scalar vec2 < 0.0
    }

    override fun paintSelf(g2: GraphicsContext, transform: DebugTransform, config: ShapeDebugConfig) {
        val sa = transform.screen(a)
        val sb = transform.screen(b)

        if (!config.drawLineAsVector) {
            g2.strokeLine(sa.x, sa.y, sb.x, sb.y)

            CrazyGraphics.paintPoint(g2, sa, name = "A", paintCoords = config.paintCoords)
            CrazyGraphics.paintPoint(g2, sb, name = "B", paintCoords = config.paintCoords)
        }
        else {
            CrazyGraphics.drawVectorArrow(g2, sa, sb - sa)
        }
    }

    override fun setConfig(shapeDebugConfig: ShapeDebugConfig) = CrazyLine(a, b, shapeDebugConfig)
}