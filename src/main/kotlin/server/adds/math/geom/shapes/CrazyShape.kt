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

/**
 * I'm the abstract shape class.
 */
abstract class CrazyShape(val type: ShapeType, val config: ShapeDebugConfig?) : DebugObjectI {
    override fun zIndex() = config?.zIndex ?: 0

    /**
     * Returns whether this shape collides with another.
     * @param that the other shape
     */
    infix fun collides(that: CrazyShape) = CrazyCollision.shapeShapeCollision(this, that)

    fun shapeConfig() = config ?: ShapeDebugConfig()

    /**
     * Returns a string representation of this shape.
     */
    override fun toString() = shapeString()

    /**
     * Returns a string representation of this shape.
     */
    abstract fun shapeString(): String

    /**
     * Returns a clone of this shape with the specified ShapeDebugConfig.
     * @param config
     */
    abstract fun setConfig(config: ShapeDebugConfig?): CrazyShape

    /**
     * Transforms this shape.
     * @param trans the transformation object
     */
    abstract fun transform(trans: CrazyTransform): CrazyShape

    /**
     * Returns whether this shape contains a point.
     * @param point
     */
    abstract infix fun containsPoint(point: CrazyVector): Boolean

    /**
     * Returns whether this shape is surrounded by a circle.
     * @param circle
     */
    abstract infix fun isSurroundedByCircle(circle: CrazyCircle): Boolean

    /**
     * Returns a clone of this shape with the specified DebugObjectOptions.
     * @param options
     */
    open fun setDebugConfig(options: DebugObjectOptions) =
        setConfig(shapeConfig().copy(debugOptions = options))

    /**
     * Returns a clone of this shape with the specified CrazyGraphicStyle.
     * @param style
     */
    open fun setCrazyStyle(style: CrazyGraphicStyle) =
        setConfig(shapeConfig().copy(crazyStyle = style))

    /**
     * Returns a clone of this shape with the specified JavaFX Color.
     * @param c the color
     */
    open fun setColor(c: Color) = setCrazyStyle(ShapeDebugConfig.DEFAULT_CRAZY_STYLE.copy(fillColor =  c, strokeColor = c))

    /**
     * Returns a clone of this shape with the specified z index.
     * @param i the z index
     */
    open fun setZIndex(i: Int) = setConfig(shapeConfig().copy(zIndex = i))

    /**
     * Paints this shape on a JavaFX canvas.
     * @param g2 the GraphicsContext
     * @param transform the transformation of the view
     * @param canvasSize the size of the canvas in pixels
     */
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