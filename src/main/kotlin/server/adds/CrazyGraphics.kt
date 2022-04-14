package server.adds

import javafx.geometry.VPos
import javafx.scene.canvas.GraphicsContext
import javafx.scene.paint.Color
import javafx.scene.shape.StrokeLineCap
import javafx.scene.text.Font
import javafx.scene.text.Text
import javafx.scene.text.TextAlignment
import server.adds.math.CrazyVector
import server.adds.math.geom.shapes.CrazyRect
import server.adds.math.vec

data class CrazyGraphicStyle(
    val fillColor: Color? = null,
    val fillOpacity: Double? = 1.0,
    val strokeColor: Color? = null,
    val strokeOpacity: Double? = 1.0,
    val lineDash: DoubleArray? = doubleArrayOf(),
    val lineWidth: Double? = null,
    val lineCap: StrokeLineCap? = StrokeLineCap.ROUND,
    val font: Font? = null
)

object CrazyGraphics {
    fun setCrazyStyle(g2: GraphicsContext, style: CrazyGraphicStyle?) {
        if (style != null) {
            if (style.fillColor != null) g2.fill = opacity(style.fillColor, style.fillOpacity ?: 0.0)
            if (style.strokeColor != null) g2.stroke = opacity(style.strokeColor, style.strokeOpacity ?: 0.0)
            if (style.lineDash != null) g2.setLineDashes(*style.lineDash)
            if (style.lineWidth != null) g2.lineWidth = style.lineWidth
            if (style.lineCap != null) g2.lineCap = style.lineCap
            if (style.font != null) g2.font = style.font
        }
    }

    fun paintTextRect(
        g2: GraphicsContext,
        pos: CrazyVector,
        text: String,
        padding: CrazyVector = vec(5.0, 3.0),
        center: CrazyVector = vec(0.0, 0.0),
        translate: CrazyVector = vec(0.0, 0.0),
        textColor: Color = Color.BLACK,
        style: CrazyGraphicStyle? = null
    ) {
        g2.lineWidth = 1.0
        g2.textAlign = TextAlignment.LEFT
        g2.textBaseline = VPos.TOP
        setCrazyStyle(g2, style)

        val metrics = reportTextSize(text, style?.font ?: g2.font)
        val pos2 = pos - center * metrics + translate
        val rectPos = pos2 - padding

        if (style?.fillOpacity != 0.0) g2.fillRoundRect(
            rectPos.x, rectPos.y,
            metrics.x + padding.x*2.0, metrics.y + padding.y*2.0,
            padding.x*2.0, padding.y*2.0
        )

        g2.fill = textColor
        g2.fillText(text, pos2.x, pos2.y)
    }
    fun reportTextSize(s: String?, myFont: Font?): CrazyVector {
        val text = Text(s)
        text.font = myFont
        val bounds = text.boundsInLocal
        return vec(bounds.width, bounds.height)
    }
    fun paintPoint(
        g2: GraphicsContext,
        pos: CrazyVector,
        name: String? = null,
        coordinates: CrazyVector? = null,
        pointSize: Double = 10.0,
        pointStyle: CrazyGraphicStyle? = CrazyGraphicStyle(
            fillColor = Color.RED,
            strokeColor = Color.BLACK,
            lineWidth = 2.0
        ),
        textStyle: CrazyGraphicStyle? = CrazyGraphicStyle(
            strokeColor = Color.WHITE
        )
    ) {
        setCrazyStyle(g2, pointStyle)
        g2.fillOval(pos.x - pointSize/2.0, pos.y - pointSize/2.0, pointSize, pointSize)
        g2.strokeOval(pos.x - pointSize/2.0, pos.y - pointSize/2.0, pointSize, pointSize)

        var resText = ""
        if (name != null) resText += name
        if (name != null && coordinates != null) resText += " = "
        if (coordinates != null) resText += pos.niceString()

        if (resText != "") {
            paintTextRect(
                g2, pos, resText,
                center = vec(0.0, 0.5),
                translate = vec(pointSize/2.0 + 10.0, 0.0),
                style = textStyle
            )
        }
    }

    fun opacity(color: Color, op: Double) = Color.color(color.red, color.green, color.blue, op)

    fun paintCornersAroundRect(g2: GraphicsContext, rect: CrazyRect, maxCornerSize: Double) {
        val mcs = (if (rect.size.x > rect.size.y) rect.size.y else rect.size.x) / 2.5
        val cornerSize = if (mcs < maxCornerSize) mcs else maxCornerSize

        val paintEdge = { v: CrazyVector, v1: CrazyVector, v2: CrazyVector ->
            val p2 = rect.pos + v * rect.size
            val p1 = p2 + v1 * cornerSize
            val p3 = p2 + v2 * cornerSize
            g2.beginPath()
            g2.moveTo(p1.x, p1.y)
            g2.lineTo(p2.x, p2.y)
            g2.lineTo(p3.x, p3.y)
            g2.stroke()
        }

        paintEdge(vec(0.0, 0.0), vec(0.0, 1.0), vec(1.0, 0.0))
        paintEdge(vec(1.0, 0.0), vec(-1.0, 0.0), vec(0.0, 1.0))
        paintEdge(vec(1.0, 1.0), vec(0.0, -1.0), vec(-1.0, 0.0))
        paintEdge(vec(0.0, 1.0), vec(1.0, 0.0), vec(0.0, -1.0))
    }

    fun drawVectorArrow(
        g2: GraphicsContext,
        pos: CrazyVector,
        vector: CrazyVector,
        arrowSize: Double = 8.0,
        crazyStyle: CrazyGraphicStyle? = null
    ) {
        setCrazyStyle(g2, crazyStyle)

        g2.fill = Color.BLACK

        val angle = vector.angle() * (180.0/Math.PI)
        val len = vector.length()

        g2.save()

        g2.translate(pos.x, pos.y)
        g2.rotate(angle)

        g2.strokeLine(0.0, 0.0, len - arrowSize, 0.0)
        g2.fillPolygon(
            doubleArrayOf(len, len - arrowSize, len - arrowSize, len),
            doubleArrayOf(0.0, -arrowSize, arrowSize, 0.0),
            4
        )

        g2.restore()
    }

}