package server.adds.debug

import javafx.scene.canvas.GraphicsContext
import javafx.scene.paint.Color
import server.adds.CrazyGraphicStyle
import server.adds.CrazyGraphics
import server.adds.math.CrazyVector
import server.adds.math.geom.shapes.CrazyRect

data class CrazyDebugVectorOptions(
    val size: Double = CrazyDebugVector.DEFAULT_VECTOR_SIZE,
    val paintCoords: Boolean = false,
    val debuggerConfig: DebugObjectOptions? = null,
    val color: Color = Color.RED,
    val textColor: Color = Color.WHITE,
    val extraName: String? = null
)

class CrazyDebugVector(
    val pos: CrazyVector,
    val config: CrazyDebugVectorOptions = CrazyDebugVectorOptions()
) : DebugObjectI {
    override fun paintDebug(g2: GraphicsContext, transform: DebugTransform, canvasSize: CrazyVector) {
        CrazyGraphics.paintPoint(
            g2, transform.screen(pos),
            config.extraName ?: debugOptions()?.name, config.paintCoords,
            config.size, CrazyGraphicStyle(
                fillColor = Color.RED,
                strokeColor = Color.BLACK,
                lineWidth = 2.0
            ), CrazyGraphicStyle(
                strokeColor = Color.WHITE,
                fillColor = Color.BLACK,
                fillOpacity = 0.3
            )
        )
    }

    override fun debugOptions() = config.debuggerConfig

    override fun surroundedRect(): CrazyRect {
        val s = CrazyVector.square(config.size + 6.0)
        return CrazyRect(pos - s /2.0, s)
    }

    fun setPos(nv: CrazyVector) = CrazyDebugVector(nv, config)

    companion object {
        var DEFAULT_VECTOR_SIZE = 10.0
    }
}