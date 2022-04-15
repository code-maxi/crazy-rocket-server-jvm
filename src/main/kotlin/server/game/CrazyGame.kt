package server.game

import GalaxyConfigI
import SendFormat
import kotlinx.coroutines.yield
import server.adds.math.vec
import server.adds.text.Ansi
import server.adds.text.Text
import server.data_containers.*
import server.game.objects.CrazyAsteroid
import server.game.objects.abstct.AbstractGameObject
import server.game.objects.CrazyRocket
import server.game.objects.abstct.GeoObject
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
    private var idCount = 0

    private val logListeners = hashMapOf<String, (from: String?, text: String, fromColor: Ansi?, textColor: Ansi?) -> Unit>(
        "main" to { f, t, c1, c2 -> Text.formattedPrint(f, t, c1, c2) }
    )
    private val deletingObjects = arrayListOf<String>()
    private val addingObjects = arrayListOf<Pair<AbstractGameObject, String>>()

    val props = GamePropsI(10, config.width, config.height)

    fun objects() = objectMap.values.toList()
    fun size() = vec(props.width, props.height)

    fun addLoggingListener(id: String, listener: (from: String?, text: String, fromColor: Ansi?, textColor: Ansi?) -> Unit) { logListeners[id] = listener }
    fun removeLoggingListener(id: String) { logListeners.remove(id) }

    private fun newID(): String {
        idCount ++
        return "G$idCount"
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
        objectMap.values.toList().filterIsInstance(o.java)

    fun geoObjects() = objectMap.values.toList().filterIsInstance(GeoObject::class.java)

    fun killObject(id: String) {
        if (deletingObjects.none { it == id }) deletingObjects.add(id)
    }

    fun addObject(obj: AbstractGameObject, id: String = newID()) {
        addingObjects.add(obj to id)
    }

    fun addRocket(u: UserPropsI): CrazyRocket {
        val rocket = CrazyRocket(
            vec(props.width.toDouble(), props.height.toDouble()) * vec(Math.random(), Math.random()),
            u
        )
        addObject(rocket, u.id)

        return rocket
    }

    fun createRandomAsteroids(howMany: Int) {
        for (i in 0..howMany) {
            addObject(
                CrazyAsteroid(
                    4.0 + 10.0 * Math.random(),
                    Math.random() * 0.25,
                    size() * Math.random(),
                    Math.random() * 2 * PI,
                    vec(Math.random() * 2 * PI, Math.random() * 10.0 + 5.0, true)
                )
            )
        }
    }

    override suspend fun calc(factor: Double) {
        yield()

        val objectList = objectMap.values.toList()

        for (step in 1..CALCULATION_TIMES) {
            for (o in objectList) o.calc(factor, step)
            yield()
        }

        handleAddingAndDeletingObjects()

        yield()
    }

    @Synchronized
    private fun handleAddingAndDeletingObjects() {
        for (d in deletingObjects) objectMap.remove(d)
        deletingObjects.clear()

        for (a in addingObjects) {
            if (!a.first.isInitialized()) {
                log(null, "${a.second} wants to join.")
                a.first.initialize(this, a.second)
                objectMap[a.second] = a.first
            }
        }
        addingObjects.clear()
    }

    override fun data() = GameDataI(props, objects().map { it.data() })

    fun log(from: String? = null, text: String, fromColor: Ansi? = null, textColor: Ansi? = null) {
        try { for (i in logListeners.values) i(from, text, fromColor, textColor) }
        catch (_: ConcurrentModificationException) {}
    }

    companion object {
        val LISTING_KEYS = listOf("ArrowUp", "ArrowRight", "ArrowLeft")
        const val CALCULATION_TIMES = 3
    }
}