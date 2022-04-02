package server.adds.debug

import javafx.scene.canvas.GraphicsContext
import server.adds.math.CrazyVector
import server.adds.math.geom.shapes.CrazyRect

interface DebugObjectI {
    fun zIndex(): Int
    fun paintDebug(g2: GraphicsContext, transform: DebugTransform, canvasSize: CrazyVector)
    fun debugOptions(): DebugObjectOptions?
    fun surroundedRect(): CrazyRect
}