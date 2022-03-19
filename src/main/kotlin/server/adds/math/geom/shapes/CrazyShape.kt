package server.adds.math.geom.shapes

import javafx.scene.canvas.GraphicsContext
import server.adds.CrazyGraphics
import server.adds.math.CollisionDetection
import server.adds.math.CrazyTransform
import server.adds.math.CrazyVector
import server.adds.math.geom.debug.DebugTransform
import server.adds.math.geom.debug.DebugObjectI

abstract class CrazyShape(val type: ShapeType, val config: ShapeDebugConfig?) : DebugObjectI {
    infix fun collides(that: CrazyShape) = CollisionDetection.shapeShapeCollision(this, that)

    private fun shapeConfig() = config ?: ShapeDebugConfig()

    abstract fun setConfig(shapeDebugConfig: ShapeDebugConfig?): CrazyShape
    abstract fun transform(trans: CrazyTransform): CrazyShape
    abstract infix fun containsPoint(point: CrazyVector): Boolean

    override fun paintDebug(g2: GraphicsContext, transform: DebugTransform, canvasSize: CrazyVector) {
        val config = shapeConfig()
        //if (config.paintSurroundedRect) paintSurroundedRect(g2, transform, canvasSize)
        CrazyGraphics.setCrazyStyle(g2, config.crazyStyle)

        paintSelf(g2, transform, config)
    }

    abstract fun paintSelf(g2: GraphicsContext, transform: DebugTransform, config: ShapeDebugConfig)

    override fun debugOptions() = config?.debugOptions
    //protected fun trans(vec: RocketVector, trans: GeomTransform) = vec * trans.scaling + trans.pos
}