package server.game

import GalaxyConfigI
import SendFormat
import TeamColor
import kotlinx.coroutines.yield
import server.adds.math.vec
import server.adds.saveForEach
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
    val onRocketMessage: (id: String, send: SendFormat) -> Unit,
    val availableTeams: List<TeamColor>
)

data class GameObjectWantsToJoin(
    val id: String,
    val gameObject: AbstractGameObject,
    val callback: ((go: AbstractGameObject) -> Unit)?
)

data class GameObjectWantsToSuicide(
    val id: String,
    val callback: (() -> Unit)?
)

class CrazyGame(
    config: GalaxyConfigI,
    val gameConfig: GameConfig
) : GameClassI {
    private val objectMap = mutableMapOf<String, AbstractGameObject>()
    private val teams = mutableMapOf<TeamColor, CrazyTeam>()
    private var currentObjects = listOf<AbstractGameObject>()
    private var idCount = 0

    private val logListeners = hashMapOf<String, (from: String?, text: String, fromColor: Ansi?, textColor: Ansi?) -> Unit>(
        "main" to { f, t, c1, c2 -> Text.formattedPrint(f, t, c1, c2) }
    )
    private val deletingObjects = arrayListOf<GameObjectWantsToSuicide>()
    private val addingObjects = arrayListOf<GameObjectWantsToJoin>()

    val props = GamePropsI(10, config.width, config.height)

    init {
        for (c in gameConfig.availableTeams) {
            teams[c] = CrazyTeam(c, this, this.size().randomPosInSquare(2))
        }
    }

    fun objects() = currentObjects
    fun size() = vec(props.width, props.height)

    fun getTeam(c: TeamColor) = teams[c] ?: throw TeamNotInUse(c)

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
        currentObjects.filterIsInstance(o.java)

    fun getObject(id: String) = objectMap[id]
    fun <T : AbstractGameObject> castedObject(id: String, klass: KClass<T>) = objectMap[id]?.let { klass.cast(it) }

    fun geoObjects() = currentObjects.filterIsInstance(GeoObject::class.java)

    fun killObject(id: String, callback: (() -> Unit)? = null) {
        if (deletingObjects.toList().none { it.id == id }) {
            deletingObjects.add(GameObjectWantsToSuicide(id, callback))
        }
    }

    fun addObject(obj: AbstractGameObject, id: String = newID(), callback: ((go: AbstractGameObject) -> Unit)? = null) {
        addingObjects.add(GameObjectWantsToJoin(id, obj, callback))
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

        currentObjects = objectMap.values.toList()

        for (step in 1..CALCULATION_TIMES) {
            for (i in currentObjects.indices) {
               try { currentObjects[i].calc(factor, step) }
               catch (_: java.lang.IndexOutOfBoundsException) {}
            }
            yield()
        }

        handleAddingAndDeletingObjects()

        yield()
    }

    @Synchronized
    private fun handleAddingAndDeletingObjects() {
        deletingObjects.saveForEach { d ->
            objectMap.remove(d.id)
            d.callback?.let { it() }
        }
        deletingObjects.clear()

        addingObjects.saveForEach { a ->
            if (!a.gameObject.isInitialized()) {
                log(null, "${a.id} wants to join.")
                a.gameObject.initialize(this, a.id)
                objectMap[a.id] = a.gameObject
                a.callback?.let { it(a.gameObject) }
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