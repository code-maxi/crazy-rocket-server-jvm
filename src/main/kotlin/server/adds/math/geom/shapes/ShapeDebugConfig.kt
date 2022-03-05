package server.adds.math.geom.shapes

import javafx.scene.paint.Color
import javafx.scene.paint.Paint

data class ShapeDebugConfig(
    val color: Color = Color.color(1.0, 0.0, 0.0),
    val paintCoords: Boolean = false,
    val lineWidth: Double = 3.0,
    val fillOpacity: Double? = 0.3,
    val stroke: Boolean = true,
    val lineDashes: DoubleArray = doubleArrayOf(),
    val paintSurroundedRect: Boolean = false,
    val name: String? = null
)
