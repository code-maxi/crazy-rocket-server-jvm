package server.game

import GalaxyConfigI
import SendFormat
import server.adds.math.vec
import server.adds.text.Ansi
import server.adds.text.Text
import server.data_containers.*
import server.game.objects.CrazyAsteroid
import server.game.objects.abstct.AbstractGameObject
import server.game.objects.CrazyRocket
import kotlin.math.PI
import kotlin.reflect.KClass
import kotlin.reflect.cast

data class GameConfig(
    val sendUser: (id: String, send: SendFormat) -> Unit,
    val debug: Boolean = false
)

class CrazyGame(
    config: GalaxyConfigI,
    private val gameConfig: GameConfig
) : GameClassI {
    private var objectMap = mutableMapOf<String, AbstractGameObject>()
    private var idCount = Int.MAX_VALUE

    private val logListeners = hashMapOf<String, (from: String?, text: String, fromColor: Ansi?, textColor: Ansi?) -> Unit>(
        "main" to { f, t, c1, c2 -> Text.formattedPrint(f, t, c1, c2) }
    )

    val props = GamePropsI(10, 5000, 5000)

    fun objects() = objectMap.values.toList()
    fun size() = vec(props.width, props.height)

    fun addLoggingListener(id: String, listener: (from: String?, text: String, fromColor: Ansi?, textColor: Ansi?) -> Unit) { logListeners[id] = listener }
    fun removeLoggingListener(id: String) { logListeners.remove(id) }

    private fun newID(): String {
        idCount --
        return idCount.toString()
    }

    private fun checkOtherId(id: String): String {
        if (objectMap[id] == null) return id
        else throw IdIsAlreadyInUse("Object", id)
    }

    fun onClientData(dataList: List<ClientDataRequestI>) {
        dataList.forEach { data ->
            if (data.keyboard != null) {
                val keyboard = data.keyboard
                val rocket = objectMap[data.userProps.id]?.let { it as CrazyRocket }
                rocket?.setKeyboard(keyboard)
            }
        }
    }

    fun <T : AbstractGameObject> objectsOfType(o: KClass<T>) =
        objectMap.values.filter { o.isInstance(it) }.map { o.cast(it) }

    fun killObject(id: String) { objectMap.remove(id) }

    fun addObject(obj: AbstractGameObject) {
        val id = newID()
        obj.initialize(this, id)
        objectMap[id] = obj
    }

    fun addRocket(u: UserPropsI): CrazyRocket {
        checkOtherId(u.id)

        val rocket = CrazyRocket(
            vec(props.width.toDouble(), props.height.toDouble()) * vec(Math.random(), Math.random()),
            u
        )
        rocket.initialize(this, u.id)
        objectMap[u.id] = rocket

        return rocket
    }

    fun createRandomAsteroids(howMany: Int) {
        for (i in 0..10) {
            addObject(
                CrazyAsteroid(
                    4.0 + 10.0 * Math.random(),
                    Math.random() * 0.25,
                    size() * Math.random(),
                    Math.random() * 2 * PI,
                    vec(Math.random() * 2 * PI, Math.random() * 0.1 + 0.05, true)
                )
            )
        }
    }

    override suspend fun calc(s: Double) {
        val objectList = objectMap.values.toList()
        for (i in objectList.indices) {
            objectList[i].calc(s)
        }
    }

    override fun data() = GameDataI(props, objects().map { it.data() })

    fun log(from: String? = null, text: String, fromColor: Ansi? = null, textColor: Ansi? = null) {
        try { for (i in logListeners.values) i(from, text, fromColor, textColor) }
        catch (_: ConcurrentModificationException) {}
    }

    companion object {
        val LISTINING_KEYS = listOf("ArrowUp", "ArrowRight", "ArrowLeft")
    }
}