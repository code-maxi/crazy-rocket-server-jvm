package server.game

import GalaxyConfigI
import SendFormat
import server.adds.math.vec
import server.data_containers.*
import server.game.objects.Asteroid
import server.game.objects.Rocket

data class GameConfig(
    val sendUser: (id: String, send: SendFormat) -> Unit,
    val onRocketCreated: (rocket: Rocket) -> Unit
)

class Game(
    config: GalaxyConfigI,
    private val gameConfig: GameConfig
) : GameClassI {
    private val objects = hashMapOf<String, GameObjectI>()
    private var idCount = Int.MAX_VALUE
    lateinit var settings: GamePropsI

    fun objectList() = objects.values.toTypedArray()

    private fun newID(): String {
        idCount --
        return idCount.toString()
    }

    private fun checkOtherId(id: String): String {
        if (objects[id] == null) return id
        else throw IdIsAlreadyInUse("Object", id)
    }

    private fun addObject(objF: (id: String) -> GameObjectI) {
        val id = newID()

        val obj = objF(id)
        obj.setGame(this)

        objects[id] = obj
    }

    fun onClientData(data: ClientDataRequestI) {
        if (data.keyboard != null) {
            val keyboard = data.keyboard
            val rocket = objects[data.userProps.id]?.let { it as Rocket }
            rocket?.setKeyboard(keyboard)
        }
    }

    fun killObject(id: String) { objects.remove(id) }

    private fun addRocket(u: UserPropsI): Rocket {
        checkOtherId(u.id)

        val rocket = Rocket(
            vec(settings.width.toDouble(), settings.height.toDouble()) * vec(Math.random(), Math.random()),
            u, u.id
        )
        objects[u.id] = rocket

        return rocket
    }
    fun addUserRocket(u: UserPropsI, onFinish: (r: Rocket) -> Unit) {
        val r = addRocket(u)
        onFinish(r)
    }

    fun loadLevel(l: Int) {
        settings = GamePropsI(l, 5000, 5000)
        for (i in 0..10) {
            addObject {
                Asteroid(
                    (Math.random() * 3.0).toInt() + 1,
                    vec(settings.width.toDouble(), settings.height.toDouble()) * vec(Math.random(), Math.random()),
                    Math.PI*2 * Math.random(),
                    vec(Math.PI*2 * Math.random(), Math.random() * 2.0 + 1.0),
                    it
                )
            }
        }
    }

    override suspend fun calc(s: Double) {
        objects.forEach { it.value.calc(s) }
    }

    override fun data() = GameDataI(settings, objectList().map { it.data() }.toTypedArray())

    companion object {
        val LISTINING_KEYS = arrayOf("ArrowUp", "ArrowRight", "ArrowLeft")
    }
}