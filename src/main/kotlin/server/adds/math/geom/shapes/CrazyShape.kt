package server.adds.math.geom.shapes

import javafx.scene.canvas.GraphicsContext
import javafx.scene.paint.Color
import server.adds.CrazyGraphics
import server.adds.math.CrazyTransform
import server.adds.math.CrazyVector
import server.adds.math.geom.debug.DebugTransform
import server.adds.math.geom.debug.GeomDebuggerObject
import server.adds.math.vec

abstract class CrazyShape(val type: GeomType, val config: ShapeDebugConfig) : GeomDebuggerObject {
    infix fun collides(o: CrazyShape) {

    }

    abstract fun surroundedRect(): CrazyRect
    abstract fun transform(trans: CrazyTransform): CrazyShape
    abstract infix fun containsPoint(point: CrazyVector): Boolean

    override fun paintDebug(g2: GraphicsContext, transform: DebugTransform) {
        g2.lineWidth = config.lineWidth
        g2.setLineDashes(*config.lineDashes)
        val c = config.color
        if (config.fillOpacity != null) g2.fill = CrazyGraphics.opacity(c, config.fillOpacity)
        g2.stroke = c
    }

    fun paintSurroundedRect(g2: GraphicsContext, transform: DebugTransform) {
        val surroundedRect = surroundedRect()

        CrazyRect(surroundedRect.pos, surroundedRect.size, ShapeDebugConfig(
            color = Color.GREEN,
            lineWidth = 1.0,
            lineDashes = doubleArrayOf(5.0, 5.0),
            fillOpacity = null
        )).paintDebug(g2, transform)

        config.name?.let {
            CrazyGraphics.paintTextRect(
                g2, transform.screen(surroundedRect.pos),
                it, backgroundColor = Color.GREEN,
                center = vec(0.0, 1.1)
            )
        }
    }
    //protected fun trans(vec: RocketVector, trans: GeomTransform) = vec * trans.scaling + trans.pos
}