package server.game

import GalaxyConfigI
import SendFormat
import server.adds.math.vec
import server.data_containers.*
import server.game.objects.Asteroid
import server.game.objects.AbstractGameObject
import server.game.objects.CrazyRocket

data class GameConfig(
    val sendUser: (id: String, send: SendFormat) -> Unit,
    val debug: Boolean = false
)

class CrazyGame(
    config: GalaxyConfigI,
    private val gameConfig: GameConfig
) : GameClassI {
    private var objects = mutableMapOf<String, AbstractGameObject>()
    private var idCount = Int.MAX_VALUE
    lateinit var settings: GamePropsI
    private var isResortNextTime = false

    fun objectList() = objects.values.toList()

    private fun newID(): String {
        idCount --
        return idCount.toString()
    }

    private fun checkOtherId(id: String): String {
        if (objects[id] == null) return id
        else throw IdIsAlreadyInUse("Object", id)
    }

    fun addObject(obj: AbstractGameObject) {
        val id = newID()
        obj.initialize(this, id)
        objects[id] = obj
    }

    fun onClientData(dataList: List<ClientDataRequestI>) {
        dataList.forEach { data ->
            if (data.keyboard != null) {
                val keyboard = data.keyboard
                val rocket = objects[data.userProps.id]?.let { it as CrazyRocket }
                rocket?.setKeyboard(keyboard)
            }
        }
    }

    fun killObject(id: String) { objects.remove(id) }

    fun addRocket(u: UserPropsI): CrazyRocket {
        checkOtherId(u.id)

        val rocket = CrazyRocket(
            vec(settings.width.toDouble(), settings.height.toDouble()) * vec(Math.random(), Math.random()),
            u
        )
        objects[u.id] = rocket

        return rocket
    }

    fun loadLevel(l: Int) {
        settings = GamePropsI(l, 5000, 5000)
        /*for (i in 0..10) {
            addObject {
                Asteroid(
                    (Math.random() * 3.0).toInt() + 1,
                    vec(settings.width.toDouble(), settings.height.toDouble()) * vec(Math.random(), Math.random()),
                    Math.PI*2 * Math.random(),
                    vec(Math.PI*2 * Math.random(), Math.random() * 2.0 + 1.0),
                    it
                )
            }
        }*/
    }

    fun createRandomAsteroids(howMany: Int) {
        for (i in 0..howMany) {

        }
    }

    override suspend fun calc(s: Double) {
        objects.forEach { it.value.calc(s) }
    }

    override fun data() = GameDataI(settings, objectList().map { it.data() })

    companion object {
        val LISTINING_KEYS = listOf("ArrowUp", "ArrowRight", "ArrowLeft")
    }
}