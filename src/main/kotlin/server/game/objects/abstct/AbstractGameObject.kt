package server.game.objects.abstct

import server.adds.debug.DebugObjectI
import server.adds.text.Ansi
import server.data_containers.GameObjectType
import server.data_containers.AbstractGameObjectI
import server.game.CrazyGame

abstract class AbstractGameObject(val type: GameObjectType) : DebugObjectI {
    private var game: CrazyGame? = null
    private var id: String? = null
    private var initialized = false

    var zIndex = type.defaultZIndex
    override fun zIndex() = zIndex

    protected fun getGame() = game ?: error("Game Object (${stringDescription()} has not been initialized yet. So you can not access the property Game.")
    fun getID() = id ?: error("Game Object (${stringDescription()} has not been initialized yet. So you can not access the property ID.")

    fun isInitialized() = initialized

    @Synchronized
    fun initialize(g: CrazyGame, id: String) {
        if (isInitialized()) error("A Game Object (${stringDescription()}) can't be initialized twice!")
        initialized = true
        this.id = id
        this.game = g
        onInitialize()
    }

    open fun onInitialize() {}

    fun suicide() { getGame().killObject(getID()) }

    protected fun addObject(o: AbstractGameObject) { getGame().addObject(o) }

    fun log(text: String = "", color: Ansi? = null) {
        game?.log(stringDescription(), text, Ansi.YELLOW, color)
    }

    open fun stringDescription(): String { return "${type.id} ($id)" }
    override fun toString() = stringDescription()

    abstract suspend fun calc(factor: Double, step: Int)
    abstract fun data(): AbstractGameObjectI
}