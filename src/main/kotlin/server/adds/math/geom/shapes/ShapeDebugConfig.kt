package server.adds.math.geom.shapes

import javafx.scene.paint.Color
import server.adds.CrazyGraphicStyle
import server.adds.math.geom.debug.DebugObjectOptions

data class ShapeDebugConfig(
    val crazyStyle: CrazyGraphicStyle = DEFAULT_CRAZY_STYLE,
    val paintCoords: Boolean = false,
    val debugOptions: DebugObjectOptions? = null,
    val drawLineAsVector: Boolean = false
) {
    companion object {
        val DEFAULT_CRAZY_STYLE = CrazyGraphicStyle(
            fillColor = Color.LIGHTBLUE,
            fillOpacity = 0.3,
            strokeColor = Color.BLUE,
            lineWidth = 2.0
        )
    }
}
