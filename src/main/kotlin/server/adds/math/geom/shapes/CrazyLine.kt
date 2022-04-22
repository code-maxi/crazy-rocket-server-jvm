package server.adds.math.geom.shapes

import javafx.scene.canvas.GraphicsContext
import javafx.scene.paint.Color
import server.adds.CrazyGraphicStyle
import server.adds.CrazyGraphics
import server.adds.debug.DebugObjectOptions
import server.adds.math.CrazyMatrix
import server.adds.math.CrazyTransform
import server.adds.math.CrazyVector
import server.adds.debug.DebugTransform
import server.adds.math.vec
import server.data_containers.CannotCheckPointOnLine

data class CrazyLineIntersectionData(
    val intersection: CrazyVector,
    val factor1: Double,
    val factor2: Double,
    val onLine1: Boolean,
    val onLine2: Boolean,
    val collides: Boolean
)

class CrazyLine(
    val a: CrazyVector,
    val b: CrazyVector,
    config: ShapeDebugConfig? = null,
) : CrazyShape(ShapeType.LINE, config) {

    private fun lbRectCorner() = vec(if (a.x < b.x) a.x else b.x, if (a.y < b.y) a.y else b.y)
    private fun rtRectCorner() = vec(if (a.x > b.x) a.x else b.x, if (a.y > b.y) a.y else b.y)

    override fun surroundedRect() = CrazyRect(
        lbRectCorner(),
        rtRectCorner() - lbRectCorner()
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
        }
        else {
            CrazyGraphics.drawVectorArrow(g2, sa, sb - sa, crazyStyle = config.crazyStyle)
        }

        if (config.paintPoints) {
            CrazyGraphics.paintPoint(g2, sa, name = if (config.paintPointNames) "A" else null, coordinates = if (config.paintCoords) a else null)
            CrazyGraphics.paintPoint(g2, sb, name = if (config.paintPointNames) "B" else null, coordinates = if (config.paintCoords) b else null)
        }
    }

    fun drawAsVector(color: Color = Color.BLUE, lineWidth: Double = 2.0) = setConfig(shapeConfig().copy(drawLineAsVector = true, crazyStyle = ShapeDebugConfig.DEFAULT_CRAZY_STYLE.copy(fillColor = color, strokeColor = color, fillOpacity = 1.0, lineWidth = lineWidth)))

    override fun isSurroundedByCircle(circle: CrazyCircle) = circle containsPoint a && circle containsPoint b
    fun leftPoint() = if (a.x < b.x) a else b
    fun rightPoint() = if (a.x > b.x) a else b

    override fun shapeString() = "Line(a = ${a.niceString()}; b = ${b.niceString()})"

    fun rightLine() = copy(a, a + (b - a).normalRight())

    fun orthogonalLineFromPoint(p: CrazyVector, vecLength: Double = 1.0) = CrazyLine(p, p + this.delta().normalRight().e() * vecLength * (if (isPointRight(p)) -1.0 else 1.0))

    fun delta() = b - a

    fun copy(a: CrazyVector = this.a, b: CrazyVector = this.b, config: ShapeDebugConfig? = this.config) =
        CrazyLine(a, b, config)

    override fun setConfig(shapeDebugConfig: ShapeDebugConfig?): CrazyLine = CrazyLine(a, b, shapeDebugConfig)

    infix fun isPointRight(pos: CrazyVector) = (b - a).normalRight() scalar (pos - a) > 0

    infix fun normalLineFrom(pos: CrazyVector): CrazyLine {
        val rightOnLine = isPointRight(pos)
        return CrazyLine(pos, pos + delta().normalRight().e() * (if (rightOnLine) -1 else 1))
    }

    infix fun intersection(that: CrazyLine): CrazyLineIntersectionData {
        val d1 = this.b - this.a
        val d2 = that.b - that.a

        val factorVec = CrazyMatrix(
            d1.x, -d2.x,
            d1.y, -d2.y
        ).inverse() * (that.a - this.a)

        val onLine1 = factorVec.x in 0.0..1.0
        val onLine2 = factorVec.y in 0.0..1.0

        return CrazyLineIntersectionData(
            intersection = this.a + d1 * factorVec.x,
            factor1 = factorVec.x,
            factor2 = factorVec.y,
            onLine1 = onLine1,
            onLine2 = onLine2,
            collides = onLine1 && onLine2
        )
    }

    fun modifyDelta(f: (CrazyVector) -> CrazyVector) = copy(a, f(b - a))

    fun toVec() = b - a

    override fun setZIndex(i: Int): CrazyLine = super.setZIndex(i) as CrazyLine
    override fun setColor(c: Color): CrazyLine = super.setColor(c) as CrazyLine
    override fun setDebugConfig(options: DebugObjectOptions) = super.setDebugConfig(options) as CrazyLine
    override fun setCrazyStyle(style: CrazyGraphicStyle) = super.setCrazyStyle(style) as CrazyLine
}