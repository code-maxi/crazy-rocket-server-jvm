package server.adds.math.geom.shapes

import javafx.scene.canvas.GraphicsContext
import javafx.scene.paint.Color
import server.adds.math.CrazyTransform
import server.adds.math.CrazyVector
import server.adds.math.geom.debug.GeomDebuggerObject

abstract class CrazyShape(val type: GeomType, val config: ShapeDebugConfig) : GeomDebuggerObject {
    infix fun collides(o: CrazyShape) {

    }
    abstract fun surroundedRect(): CrazyRect
    abstract fun transform(trans: CrazyTransform): CrazyShape
    abstract infix fun containsPoint(point: CrazyVector): Boolean

    override fun paintDebug(g2: GraphicsContext) {
        g2.lineWidth = config.lineWidth
        g2.setLineDashes(*config.lineDashes)
        val c = config.color
        g2.fill = Color.color(c.red, c.green, c.blue, 0.3)
        g2.stroke = c
    }

    fun paintSurroundedRect(g2: GraphicsContext) {
        val surroundedRect = surroundedRect()
        CrazyRect(surroundedRect.pos, surroundedRect.size, ShapeDebugConfig(
            color = Color.GREEN,
            lineWidth = 2.0,
            lineDashes = doubleArrayOf(2.0, 2.0)
        )).paintDebug(g2)
    }
    //protected fun trans(vec: RocketVector, trans: GeomTransform) = vec * trans.scaling + trans.pos
}