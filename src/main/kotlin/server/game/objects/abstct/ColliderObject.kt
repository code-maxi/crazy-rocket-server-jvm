package server.game.objects.abstct

import javafx.scene.canvas.GraphicsContext
import server.adds.math.CrazyVector
import server.adds.debug.DebugObjectI
import server.adds.debug.DebugObjectOptions
import server.adds.debug.DebugTransform
import server.adds.math.geom.shapes.CrazyShape
import server.adds.math.geom.shapes.ShapeDebugConfig
import server.game.CrazyGame
import kotlin.reflect.KClass

/**
 * I'm an abstract object class that offers collision detection with other "ColliderObject" instances.
 */

abstract class ColliderObject(type: GameObjectTypeE) : AbstractGameObject(type), DebugObjectI {
    // a function standing for the collider shape, it must be overwritten
    /**
     * Returns the collider of an object shown by a CrazyShape.
     */
    abstract fun collider(): CrazyShape
    override fun debugOptions(): DebugObjectOptions? = null
    open fun shapeDebugConfig(): ShapeDebugConfig? = null
    override fun getGame() = super.getGame() as CrazyGame

    /**
     * Returns if the collider of an object collides its collider.
     * @param that another game object
     */
    infix fun collides(that: ColliderObject) = this.collider() collides that.collider()

    /**
     * Returns a list of the objects that collides itself of the given class.
     * @param type
     */
    fun <T : ColliderObject> getCollidingObjects(type: KClass<T>) =
        getGame().objectList().filter { type.isInstance(it) && this collides (it as ColliderObject) }

    //
    override fun paintDebug(g2: GraphicsContext, transform: DebugTransform, canvasSize: CrazyVector) {
        collider().paintDebug(g2, transform, canvasSize)
    }

    override fun surroundedRect() = collider().surroundedRect()
}