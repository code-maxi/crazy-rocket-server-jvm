package server.adds.math.geom.debug

import javafx.scene.canvas.GraphicsContext

interface GeomDebuggerObject {
    fun paintDebug(g2: GraphicsContext)
}