package server.game

import kotlinx.coroutines.yield
import server.adds.saveForEach
import server.adds.text.Ansi
import server.adds.text.Text
import server.data_containers.ClientDataRequest
import server.data_containers.UserPropsI
import server.game.objects.abstct.AbstractGameObject
import kotlin.reflect.KClass

data class GameObjectWantsToJoin(
    val id: String,
    val gameObject: AbstractGameObject,
    val callback: ((go: AbstractGameObject) -> Unit)?
)

data class GameObjectWantsToSuicide(
    val id: String,
    val callback: (() -> Unit)?
)

abstract class AbstractGame {
    private var iterationTime = 0L

    abstract val CALCULATION_TIMES: Int
    private var idCount = 0

    // the object map where all the objects are saved
    protected val objectMap = mutableMapOf<String, AbstractGameObject>()
    // the list of the team classes which cover the team

    private var currentObjects = listOf<AbstractGameObject>()
    protected var userList = listOf<UserPropsI>()

    private val deletingObjects = arrayListOf<GameObjectWantsToSuicide>()
    private val addingObjects = arrayListOf<GameObjectWantsToJoin>()

    private val logListeners = hashMapOf<String, (from: String?, text: String, fromColor: Ansi?, textColor: Ansi?) -> Unit>(
        "main" to { f, t, c1, c2 -> Text.formattedPrint(f, t, c1, c2) }
    )

    fun addLoggingListener(id: String, listener: (from: String?, text: String, fromColor: Ansi?, textColor: Ansi?) -> Unit) { logListeners[id] = listener }
    fun removeLoggingListener(id: String) { logListeners.remove(id) }

    private fun newID(): String {
        idCount ++
        return "GO$idCount"
    }

    /**
     * Returns a list of all objects from the specified class.
     * @param objectClass
     */
    fun <T : AbstractGameObject> objectsOfType(objectClass: KClass<T>) =
        currentObjects.filterIsInstance(objectClass.java)

    /**
     * Returns the current objects in the game.
     */
    fun objectList() = currentObjects

    /**
     * Returns the object with the specified id.
     * @param id
     */
    fun getObject(id: String) = objectMap[id]

    /**
     * Kills an object.
     * @param id The if of the object that should be deleted.
     * @param callback An optional callback that is called after the deletion.
     */
    fun killObject(id: String, callback: (() -> Unit)? = null) {
        if (deletingObjects.toList().none { it.id == id }) {
            deletingObjects.add(GameObjectWantsToSuicide(id, callback))
        }
    }

    /**
     * Adds an object.
     * @param obj the game object that should be added
     * @param id an optional parameter to specify the id of the object. The default is self-generated
     * @param callback an optional callback that is called after the object was added
     */
    fun addObject(obj: AbstractGameObject, id: String = newID(), callback: ((go: AbstractGameObject) -> Unit)? = null) {
        addingObjects.add(GameObjectWantsToJoin(id, obj, callback))
    }

    /**
     * Calculates one game step. Returns a list including the data of the game for the clients.
     * @param factor the speed of the step
     * @param users a list of all user data
     */
    open suspend fun calc(
        factor: Double,
        users: List<UserPropsI>,
        clientDataRequests: Map<String, ClientDataRequest>
    ): Map<String, Map<String, Any?>> {
        yield()

        users.forEach {
            if (!userList.contains(it))
                whenNewUserJoined(it)
        }
        userList.forEach {
            if (!users.contains(it))
                whenUserLeft(it)
        }

        userList = users
        currentObjects = objectMap.values.toList()

        for (step in 1..CALCULATION_TIMES) {
            for (i in currentObjects.indices) {
                try { currentObjects[i].calc(factor, step) }
                catch (_: java.lang.IndexOutOfBoundsException) {}
            }
            yield()
        }

        handleAddingAndDeletingObjects()

        iterationTime ++

        yield()

        return clientDataRequests.map { it.key to dataForUser(it.key) }.toMap()
    }

    abstract fun dataForUser(userId: String): Map<String, Any?>

    open fun whenNewUserJoined(user: UserPropsI) {}
    open fun whenUserLeft(user: UserPropsI) {}

    /**
     * Returns how often the function calc() was called.
     */
    fun getCurrentIterationTime() = iterationTime

    private fun handleAddingAndDeletingObjects() {
        deletingObjects.saveForEach { d ->
            objectMap.remove(d.id)
            d.callback?.let { it() }
        }
        deletingObjects.clear()

        addingObjects.saveForEach { a ->
            if (!a.gameObject.isInitialized()) {
                a.gameObject.initialize(this, a.id)
                objectMap[a.id] = a.gameObject
                a.callback?.let { it(a.gameObject) }
            }
        }
        addingObjects.clear()
    }

    fun log(from: String? = null, text: String, fromColor: Ansi? = null, textColor: Ansi? = null) {
        try { for (i in logListeners.values) i(from, text, fromColor, textColor) }
        catch (_: ConcurrentModificationException) {}
    }
}