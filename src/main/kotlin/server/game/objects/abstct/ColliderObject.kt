package server.game.objects.abstct

import javafx.scene.canvas.GraphicsContext
import server.adds.math.CrazyVector
import server.adds.debug.DebugObjectI
import server.adds.debug.DebugObjectOptions
import server.adds.debug.DebugTransform
import server.adds.math.geom.shapes.CrazyShape
import server.adds.math.geom.shapes.ShapeDebugConfig
import server.data_containers.GameObjectType
import kotlin.reflect.KClass

abstract class ColliderObject(type: GameObjectType) : AbstractGameObject(type), DebugObjectI {
    abstract fun collider(): CrazyShape

    override fun debugOptions(): DebugObjectOptions? = null
    open fun shapeDebugConfig(): ShapeDebugConfig? = null

    infix fun collides(that: ColliderObject) = this.collider() collides that.collider()

    fun <T : ColliderObject> getCollidingObjects(type: KClass<T>) =
        getGame().objects().filter { type.isInstance(it) && this collides (it as ColliderObject) }

    override fun paintDebug(g2: GraphicsContext, transform: DebugTransform, canvasSize: CrazyVector) {
        collider().setConfig(shapeDebugConfig()).paintDebug(g2, transform, canvasSize)
    }

    override fun surroundedRect() = collider().surroundedRect()
}