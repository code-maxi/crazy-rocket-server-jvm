package server.game.objects

import server.adds.math.geom.debug.DebugObjectI
import server.adds.text.Ansi
import server.adds.text.Text
import server.data_containers.GameClassI
import server.data_containers.GameObjectType
import server.data_containers.TypeObjectI
import server.game.Game

abstract class GameObjectI(val id: String, val type: GameObjectType) : GameClassI, DebugObjectI {
    private lateinit var game: Game

    protected fun getGame() = game
    fun setGame(g: Game) { game = g }

    fun log(text: String, color: Ansi? = null) {
        Text.coloredLog("GO ($id) ${data().type}", text, color = color, name = Ansi.PURPLE, maxSize = 25)
    }

    abstract override fun data(): TypeObjectI
}