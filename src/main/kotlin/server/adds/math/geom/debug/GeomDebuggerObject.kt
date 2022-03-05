package server.adds.math.geom.debug

import javafx.scene.canvas.GraphicsContext
import server.adds.math.CrazyVector

interface GeomDebuggerObject {
    fun paintDebug(g2: GraphicsContext, transform: DebugTransform)
}