package server.user

import ClientDataI
import CreateNewGalaxyI
import GalaxyAdminI
import JoinGalaxyI
import SendFormat
import UserPropsI
import com.google.gson.Gson
import org.java_websocket.WebSocket
import server.LogType
import server.Logable
import server.coloredLog
import server.data.*
import server.galaxy.GalaxyS
import server.game.objects.GeoObject
import server.game.objects.Rocket


class UserS(val socket: WebSocket, id: Int) : Logable {
    var props = UserPropsI("UNDEFINED", null, id)
    private var inGame = false
    private val sendQueue = arrayListOf<SendFormat>()

    lateinit var clientData: ClientDataI
    lateinit var galaxy: GalaxyS
    lateinit var myRocket: Rocket

    init {
        userQueue.add(this)
        userSocketMap[socket] = this
    }

    private fun galaxyInitialized() = this::galaxy.isInitialized

    override fun log(str: String, type: LogType) {
        coloredLog("UserS[${props.name}, ${props.id}] logs: ", str, type)
    }
    private fun sendDirectly(v: SendFormat) { socket.send(Gson().toJson(v)) }
    fun send(v: SendFormat) { if (galaxyInitialized()) sendQueue.add(v) else sendDirectly(v) }

    fun onMessage(a: SendFormat) {
        log("Recieving: $a")

        when(a.header) {
            "create new galaxy" -> {
                val result = try { GalaxyS.createGalaxy(a.value as CreateNewGalaxyI) }
                catch (ex: ClassCastException) { "wrong request" }
                catch (ex: OwnException) { ex.toString() }

                send(SendFormat(
                    "create new galaxy result",
                    result
                ))
            }
            "join galaxy" -> {
                val result = try {
                    val join = a.value as JoinGalaxyI
                    GalaxyS.joinGalaxy(join, this)
                    props = props.copy(name = join.userName)
                }
                catch (ex: ClassCastException) { "wrong request" }
                catch (ex: OwnException) { ex.toString() }

                send(SendFormat(
                    "join galaxy result",
                    result
                ))
            }
            "start game" -> {
                val result = try {
                    val admin = a.value as GalaxyAdminI
                    galaxy.startGame(admin.password)
                }
                catch (ex: ClassCastException) { "wrong request" }
                catch (ex: OwnException) { ex.toString() }
                catch (ex: UninitializedPropertyAccessException) { "galaxy not initialized yet" }

                send(SendFormat(
                    "start game result",
                    result
                ))
            }
        }
    }

    fun onOpen() {
        log("Opened.")
    }

    fun onClose() {
        log("Closed.")
    }

    fun onError(ex: Exception) {
        println("onError::" + ex.message)
    }

    fun isInGame() = inGame

    private fun viewRect(): GeoI {
        val screen = this.clientData.screenSize * 1.5 * (1/this.myRocket.zoom)
        return GeoI(
            pos = this.myRocket.eye - screen/2.0,
            width = screen.x,
            height = screen.y
        )
    }

    fun sendData(
        fullData: Boolean,
        settings: GameSettings,
        objectsArray: Array<GameObjectI>
    ) {
        var objects = objectsArray
        if (fullData) {
            objects = objects.filter {
                it !is GeoObject || it.getGeo() touchesRect viewRect()
            }.toTypedArray()
        }
        send(
            SendFormat(
                "galaxy-data",
                GameDataForSendingI(
                    settings,
                    objects.map { it.data() }.toTypedArray(),
                    sendQueue.toTypedArray(),
                    fullData
                )
            )
        )
        sendQueue.clear()
    }

    fun onSuccessfullyJoined() {
        props = props.copy(galaxy = galaxy.props.name)
    }

    companion object {
        val userQueue = ArrayList<UserS>()
        val userSocketMap = HashMap<WebSocket, UserS>()
        fun findUser(s: WebSocket) = userSocketMap[s]!!
    }
}