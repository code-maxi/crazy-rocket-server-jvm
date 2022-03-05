package server.adds.math.geom.shapes

import javafx.scene.canvas.GraphicsContext
import server.adds.CrazyGraphics
import server.adds.math.CrazyTransform
import server.adds.math.CrazyVector
import server.adds.math.geom.debug.DebugTransform
import server.adds.math.vec
import server.data_containers.CannotCheckPointOnLine

class CrazyLine(p1: CrazyVector, p2: CrazyVector, config: ShapeDebugConfig = ShapeDebugConfig()) : CrazyShape(GeomType.LINE, config) {
    val a: CrazyVector
    val b: CrazyVector

    private fun ltRectCorner() = vec(if (a.x < b.x) a.x else b.x, if (a.y < b.y) a.y else b.y)
    private fun brRectCorner() = vec(if (a.x > b.x) a.x else b.x, if (a.y > b.y) a.y else b.y)

    init {
        if (p1.x < p2.x) {
            a = p1
            b = p2
        }
        else {
            a = p2
            b = p1
        }
    }

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

    override fun paintDebug(g2: GraphicsContext, transform: DebugTransform) {
        super.paintDebug(g2, transform)

        val sa = transform.screen(a)
        val sb= transform.screen(b)

        g2.strokeLine(sa.x, sa.y, sb.x, sb.y)

        CrazyGraphics.paintPoint(g2, sa, name = "A", paintCoords = config.paintCoords)
        CrazyGraphics.paintPoint(g2, sb, name = "B", paintCoords = config.paintCoords)

        if (config.paintSurroundedRect) paintSurroundedRect(g2, transform)
    }
}