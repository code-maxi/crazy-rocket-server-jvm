package server.adds

import javafx.geometry.Bounds
import javafx.geometry.VPos
import javafx.scene.canvas.GraphicsContext
import javafx.scene.paint.Color
import javafx.scene.shape.ArcType
import javafx.scene.shape.Rectangle
import javafx.scene.shape.Shape
import javafx.scene.text.Font
import javafx.scene.text.Text
import javafx.scene.text.TextAlignment
import server.adds.math.CrazyVector
import kotlin.math.PI


object JavaFXGraphics {
    fun paintTextRect(
        g2: GraphicsContext,
        pos: CrazyVector,
        text: String,
        backgroundColor: Color = Color.color(0.0,0.0,0.0,0.5),
        strokeColor: Color = Color.WHITE,
        font: Font = Font.font(12.0)
    ) {
        g2.fill = backgroundColor
        g2.font = font
        g2.stroke = strokeColor
        g2.textAlign = TextAlignment.RIGHT
        g2.textBaseline = VPos.BOTTOM
        g2.lineWidth = 2.0

        val metrics = reportTextSize(text, font)
        val padding = 3.0
        val rectPos = pos - CrazyVector.square(padding)

        g2.fillRoundRect(
            rectPos.x, rectPos.y,
            metrics.width, metrics.height,
            padding/2.0, padding/2.0
        )

        g2.strokeText(text, pos.x, pos.y)
    }
    fun reportTextSize(s: String?, myFont: Font?): Bounds {
        val text = Text(s)
        text.font = myFont
        return text.boundsInLocal!!
    }
    fun paintPoint(
        g2: GraphicsContext,
        pos: CrazyVector,
        color: Color = Color.RED,
        name: String? = null,
        paintCoords: Boolean = false
    ) {
        g2.fill = color
        g2.fillArc(pos.x, pos.y, 5.0, 5.0, 0.0, PI * 2, ArcType.ROUND)

        var resText = ""
        if (name != null) resText += name
        if (name != null && paintCoords) resText += " = "
        if (paintCoords) resText += pos.niceString()

        if (resText != "") paintTextRect(g2, pos, resText)
    }
}