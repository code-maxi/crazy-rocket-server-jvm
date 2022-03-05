package server.adds

import javafx.geometry.VPos
import javafx.scene.canvas.GraphicsContext
import javafx.scene.paint.Color
import javafx.scene.shape.ArcType
import javafx.scene.text.Font
import javafx.scene.text.Text
import javafx.scene.text.TextAlignment
import server.adds.math.CrazyVector
import server.adds.math.vec
import kotlin.math.PI


object CrazyGraphics {
    fun paintTextRect(
        g2: GraphicsContext,
        pos: CrazyVector,
        text: String,
        backgroundColor: Color = Color.GREY,
        textColor: Color = Color.WHITE,
        font: Font = Font.font(12.0),
        padding: CrazyVector = vec(5.0, 3.0),
        center: CrazyVector = vec(0.0, 0.0),
        translate: CrazyVector = vec(0.0, 0.0),
        opacity: Double = 1.0
    ) {
        g2.fill = opacity(backgroundColor, opacity)
        g2.lineWidth = 1.0
        g2.font = font
        g2.textAlign = TextAlignment.LEFT
        g2.textBaseline = VPos.TOP

        val metrics = reportTextSize(text, font)
        val pos2 = pos - center * metrics + translate
        val rectPos = pos2 - padding

        g2.fillRoundRect(
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
        color: Color = Color.RED,
        name: String? = null,
        strokeColor: Color = Color.BLACK,
        paintCoords: Boolean = false,
        pointSize: Double = 10.0
    ) {
        g2.fill = color
        g2.stroke = strokeColor
        g2.lineWidth = 1.0
        g2.fillOval(pos.x - pointSize/2.0, pos.y - pointSize/2.0, pointSize, pointSize)
        g2.strokeOval(pos.x - pointSize/2.0, pos.y - pointSize/2.0, pointSize, pointSize)

        var resText = ""
        if (name != null) resText += name
        if (name != null && paintCoords) resText += " = "
        if (paintCoords) resText += pos.niceString()

        if (resText != "") paintTextRect(g2, pos, resText, center = vec(0.0, 0.5), translate = vec(pointSize/2.0 + 10.0, 0.0))
    }
    fun opacity(color: Color, op: Double) = Color.color(color.red, color.green, color.blue, op)
}