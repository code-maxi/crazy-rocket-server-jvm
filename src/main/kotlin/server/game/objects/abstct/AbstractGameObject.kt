package server.game.objects.abstct

import server.adds.debug.DebugObjectI
import server.adds.text.Ansi
import server.adds.text.Text
import server.data_containers.GameClassI
import server.data_containers.GameObjectType
import server.data_containers.AbstractGameObjectI
import server.game.CrazyGame

abstract class AbstractGameObject(val type: GameObjectType) : GameClassI, DebugObjectI {
    private var game: CrazyGame? = null
    private var id: String? = null

    var zIndex = type.defaultZIndex
    override fun zIndex() = zIndex

    protected fun getGame() = game ?: error("Game Object (${stringDescription()} has not been initialized yet. So you can not access the property Game.")
    fun getID() = id ?: error("Game Object (${stringDescription()} has not been initialized yet. So you can not access the property ID.")

    fun initialize(g: CrazyGame, id: String) {
        if (id != null || game != null) error("A Game Object (${stringDescription()}) can't be initialized twice!")
        this.id = id
        game = g
    }

    fun killMe() { getGame().killObject(getID()) }

    protected fun addObject(o: AbstractGameObject) { getGame().addObject(o) }

    fun log(text: String, color: Ansi? = null) {

    }

    open fun stringDescription(): String { return type.id }

    abstract override fun data(): AbstractGameObjectI
}