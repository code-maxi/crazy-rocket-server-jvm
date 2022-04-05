package server.game.objects.abstct

import server.adds.debug.DebugObjectI
import server.adds.text.Ansi
import server.adds.text.Text
import server.data_containers.GameClassI
import server.data_containers.GameObjectType
import server.data_containers.AbstractGameObjectI
import server.game.CrazyGame

abstract class AbstractGameObject(val type: GameObjectType) : GameClassI, DebugObjectI {
    private lateinit var game: CrazyGame
    private lateinit var id: String

    var zIndex = type.defaultZIndex
    override fun zIndex() = zIndex

    protected fun getGame() = game
    fun getID() = id

    fun initialize(g: CrazyGame, id: String) {
        if (this::id.isInitialized || this::game.isInitialized) error("I can't be initialized twice!")
        this.id = id; game = g
    }

    fun killMe() { getGame().killObject(id) }

    protected fun addObject(o: AbstractGameObject) { getGame().addObject(o) }

    fun log(text: String, color: Ansi? = null) {
        Text.coloredLog("GO ($id) ${type.id}", text, color = color, name = Ansi.PURPLE, maxSize = 25)
    }

    abstract override fun data(): AbstractGameObjectI
}