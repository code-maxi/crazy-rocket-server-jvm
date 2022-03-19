package server.game.objects

import javafx.scene.canvas.GraphicsContext
import server.adds.math.CrazyVector
import server.adds.math.geom.debug.DebugObjectI
import server.adds.math.geom.debug.DebugObjectOptions
import server.adds.math.geom.debug.DebugTransform
import server.adds.math.geom.shapes.CrazyShape
import server.adds.math.geom.shapes.ShapeDebugConfig
import server.data_containers.GameObjectType

abstract class ColliderObject(id: String, type: GameObjectType) : GameObjectI(id, type), DebugObjectI {
    abstract fun collider(): CrazyShape

    override fun debugOptions(): DebugObjectOptions? = null
    open fun shapeDebugConfig(): ShapeDebugConfig? = null

    override fun paintDebug(g2: GraphicsContext, transform: DebugTransform, canvasSize: CrazyVector) {
        collider().setConfig(shapeDebugConfig()).paintDebug(g2, transform, canvasSize)
    }

    override fun surroundedRect() = collider().surroundedRect()
}