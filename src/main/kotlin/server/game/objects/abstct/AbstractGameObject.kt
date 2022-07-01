package server.game.objects.abstct

import server.adds.debug.DebugObjectI
import server.adds.text.Ansi
import server.game.AbstractGame
import server.game.CrazyGame
import server.game.data.DataMemory

/**
 * This class is the abstract origin for the game object. Every game object must extend this class to be used in the game.
 */

abstract class AbstractGameObject(val type: GameObjectTypeE) : DebugObjectI {
    // the game instance
    private var game: AbstractGame? = null

    // the id of an object - they all differ
    private var id: String? = null

    private var initialized = false

    // It's the map you can save properties to the object from outside using the methods below.
    private var properties = hashMapOf<String, Any>()

    // a DataMemory instance to offer a comparison.
    private var dataMemory = DataMemory(CrazyGame.MAX_DATA_MEMORY_DEPTH)

    var zIndex = type.defaultZIndex
    override fun zIndex() = zIndex

    /**
     * Get the game instance of the object. It's important
     */
    protected open fun getGame() =
        game ?: error("Game Object ($this has not been initialized yet. So you can not access the property Game.")

    /**
     * Get the ID of the object. Of course, they all differ.
     */
    fun getID() = id ?: error("Game Object ($this has not been initialized yet. So you can not access the property ID.")

    /**
     * Get whether an object is initialized.
     */
    fun isInitialized() = initialized

    /**
     * Initializes an object. It's important that you never call it on your self. It's called automatically by adding an object from the game instance.
     * @param g The game the object is initialized in.
     * @param id The ID of the object.
    */
    @Synchronized
    fun initialize(g: AbstractGame, id: String) {
        if (isInitialized()) error("A Game Object ($this) can't be initialized twice!")
        initialized = true
        this.id = id
        this.game = g
        onInitialize()
    }

    /**
     * A callback function you can overwrite that is called after the object was initialized.
     */
    protected open fun onInitialize() {}

    /**
     * Kills the object and removes it from the world.
     */
    fun suicide() { getGame().killObject(getID()) }

    /**
     * Sets a property to the object.
     * @param id the id of the property
     * @param value the value of the property
     */
    fun setProp(id: String, value: Any?) {
        if (value == null) properties.remove(id)
        else properties[id] = value
    }

    /**
     * Get a property from the object.
     * @param id the id of the property
     */
    fun readProp(id: String) = properties[id]
    fun isPropEmpty(id: String) = readProp(id) == null

    /**
     * Removes a property from the object.
     * @param id the id of the property that you want to remove
     */
    fun removeProp(id: String) { properties.remove(id) }
    fun propsArray() = properties.toList().map { it.first to it.second.toString() }.toTypedArray()

    /**
     * Add an object to the world, identical to CrazyGame.addObject.
     */
    protected fun addObject(o: AbstractGameObject) { getGame().addObject(o) }

    /**
     * Prints a text to the console using the object-print-mode.
     * @param color an optional parameter to specify an ANSI color
     */
    fun log(text: String = "", color: Ansi? = null) {
        game?.log(toString(), text, Ansi.YELLOW, color)
    }

    /**
     * Returns a string representation of an object.
     */
    override fun toString() = "${type.id} ($id)"

    // the method returning the data map for an object that must be overwritten
    protected open fun data() = mapOf<String, Any?>(
        "id" to getID(),
        "type" to type
    )

    protected fun updateData() { dataMemory.updateData(data()) }

    /**
     * Returns the data map comparison of an object.
     * @param depth defines the comparison depth
     */
    fun getDataMap(depth: Int) = dataMemory.getChange(depth)

    /**
     * Calculates one object step.
     * @param factor the speed of the step
     * @param step
     */
    open suspend fun calc(factor: Double, step: Int) {
        if (step == getGame().CALCULATION_TIMES) { // if it's the last time
            updateData()
        }
    }
}