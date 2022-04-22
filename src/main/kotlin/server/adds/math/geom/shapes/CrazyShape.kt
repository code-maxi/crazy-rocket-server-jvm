package server.adds.math.geom.shapes

import javafx.scene.canvas.GraphicsContext
import javafx.scene.paint.Color
import server.adds.CrazyGraphicStyle
import server.adds.CrazyGraphics
import server.adds.math.CrazyCollision
import server.adds.math.CrazyTransform
import server.adds.math.CrazyVector
import server.adds.debug.DebugTransform
import server.adds.debug.DebugObjectI
import server.adds.debug.DebugObjectOptions

abstract class CrazyShape(val type: ShapeType, val config: ShapeDebugConfig?) : DebugObjectI {
    override fun zIndex() = config?.zIndex ?: 0

    infix fun collides(that: CrazyShape) = CrazyCollision.shapeShapeCollision(this, that)

    fun shapeConfig() = config ?: ShapeDebugConfig()

    override fun toString() = shapeString()

    abstract fun shapeString(): String
    abstract fun setConfig(shapeDebugConfig: ShapeDebugConfig?): CrazyShape
    abstract fun transform(trans: CrazyTransform): CrazyShape
    abstract infix fun containsPoint(point: CrazyVector): Boolean
    abstract infix fun isSurroundedByCircle(circle: CrazyCircle): Boolean

    open fun setDebugConfig(options: DebugObjectOptions) = setConfig(shapeConfig().copy(debugOptions = options))
    open fun setCrazyStyle(style: CrazyGraphicStyle) = setConfig(shapeConfig().copy(crazyStyle = style))

    open fun setColor(c: Color) = setCrazyStyle(ShapeDebugConfig.DEFAULT_CRAZY_STYLE.copy(fillColor =  c, strokeColor = c))
    open fun setZIndex(i: Int) = setConfig(shapeConfig().copy(zIndex = i))

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