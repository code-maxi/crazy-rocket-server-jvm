package server.user

import ClientDataI
import ClientKeyboardI
import CreateNewGalaxyI
import GalaxyAdminI
import JoinGalaxyI
import ResponseResult
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


class UserS(val socket: WebSocket, id: String) : Logable {
    var props = UserPropsI("UNDEFINED", null, id)
    private var inGame = false
    private val sendQueue = arrayListOf<SendFormat>()

    lateinit var clientData: ClientDataI
    lateinit var galaxy: GalaxyS
    lateinit var myRocket: Rocket

    init {
        userQueue.add(this)
        userSocketMap[socket] = this
        onOpen()
        println("User initialized.")
    }

    private fun galaxyInitialized() = this::galaxy.isInitialized

    override fun log(str: String, type: LogType) {
        coloredLog("UserS[${props.name}, ${props.id}] logs: ", str, type)
    }
    private fun sendDirectly(v: SendFormat) { socket.send(Gson().toJson(v)) }
    fun send(v: SendFormat) { if (galaxyInitialized()) sendQueue.add(v) else sendDirectly(v) }

    fun onMessage(a: SendFormat) {
        log("Receiving: $a")

        when(a.header) {
            "join-galaxy" -> {
                println("Joining Galaxy...")

                val result = try {
                    val join = a.value as JoinGalaxyI
                    GalaxyS.joinGalaxy(join, this)
                    props = props.copy(name = join.userName)
                    ResponseResult(true, data = props)
                }
                catch (ex: ClassCastException) { throw WrongRequestEx(a.value) }
                catch (ex: OwnException) { ex.responseResult() }

                println("join-galaxy-result: $result")

                send(SendFormat(
                    "join-galaxy-result",
                    result
                ))
            }
            "start game" -> {
                println("Starting game...")

                val result = try {
                    val admin = a.value as GalaxyAdminI
                    galaxy.startGame(admin.password)
                    ResponseResult(true)
                }
                catch (ex: ClassCastException) { throw WrongRequestEx(a.value) }
                catch (ex: OwnException) { ex.responseResult() }

                println("starting-game-result: $result")

                send(SendFormat(
                    "start-game-result",
                    result
                ))
            }
            "keyboard-data" -> {
                clientData = clientData.copy(keyboard = a.value as ClientKeyboardI)
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
                "game-data",
                GameDataForSendingI(
                    settings,
                    objects.map { it.data() }.toTypedArray(),
                    sendQueue.toTypedArray(),
                    fullData,
                    this.myRocket.userView()
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