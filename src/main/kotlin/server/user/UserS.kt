package server.user

import ClientDataI
import ClientKeyboardI
import GalaxyAdminI
import JoinGalaxyI
import ResponseResult
import SendFormat
import UserPropsI
import com.google.gson.Gson
import io.ktor.http.cio.websocket.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.launch
import server.Ansi
import server.Logable
import server.Text.coloredLog
import server.data.*
import server.galaxy.GalaxyS
import server.game.objects.GeoObject
import server.game.objects.Rocket

class UserS(private val session: DefaultWebSocketSession) : Logable {
    val id = newID()
    var props = UserPropsI("UNDEFINED", null, id)
    private var inGame = false
    private val sendQueue = arrayListOf<SendFormat>()

    init {
        log("initialized.")
    }

    suspend fun onClose() {
        galaxy.deleteUser(this)
        log("Closed.")
    }

    private var clientData = ClientDataI(
        keyboard = ClientKeyboardI(arrayOf()),
        screenSize = VectorI.zero()
    )

    lateinit var galaxy: GalaxyS
    var myRocket: Rocket? = null

    private fun galaxyInitialized() = this::galaxy.isInitialized

    private suspend fun sendDirectly(v: SendFormat) { session.send(Frame.Text(Gson().toJson(v))) }
    suspend fun send(v: SendFormat) { if (galaxyInitialized()) sendQueue.add(v) else sendDirectly(v) }

    suspend fun onMessage(a: SendFormat) {
        //log("Receiving: $a")

        when(a.header) {
            "join-galaxy" -> {
                println()

                val result = try {
                    val join = Gson().fromJson(a.value.toString(), JoinGalaxyI::class.java)

                    log("User '${join.userName}' wants to join the galaxy '${join.galaxyName}.'")
                    log("Its data is: '$join'")

                    GalaxyS.joinGalaxy(join, this)

                    props = props.copy(name = join.userName)
                    clientData = clientData.copy(screenSize = join.screenSize)

                    ResponseResult(true, data = props)
                }
                catch (ex: ClassCastException) { WrongRequestEx(a.value).responseResult() }
                catch (ex: OwnException) { ex.responseResult() }

                log("join-galaxy-result: $result")
                println()

                sendDirectly(SendFormat(
                    "join-galaxy-result",
                    result
                ))
            }
            "start-game" -> {
                val result = try {
                    log("In start-game 1.")
                    val admin = Gson().fromJson(a.value.toString(), GalaxyAdminI::class.java)
                    log("In start-game 2.")
                    galaxy.startGame(admin.password)
                    log("In start-game 3.")
                    ResponseResult(true)
                    log("In start-game 4.")
                }
                catch (ex: ClassCastException) { WrongRequestEx(a.value).responseResult() }
                catch (ex: OwnException) { ex.responseResult() }

                log("starting-game-result: $result")

                sendDirectly(SendFormat(
                    "start-game-result",
                    result
                ))
            }
            "keyboard-data" -> {
                try {
                    val keyboard = Gson().fromJson(a.value.toString(), ClientKeyboardI::class.java)
                    galaxy.sendGame(SendFormat("user-keyboard", keyboard), this.props)
                }
                catch (ex: ClassCastException) {
                    log(WrongRequestEx(a.value).responseResult().toString())
                }
            }
        }
    }

    fun onMessageFromGame(s: SendFormat) {
        log("Data from Game: '$s'")
    }

    fun isInGame() = inGame

    private fun viewRect(): GeoI {
        val screen = this.clientData.screenSize * 1.5 * (1/this.myRocket!!.zoom)
        return GeoI(
            pos = this.myRocket!!.eye - screen/2.0,
            width = screen.x,
            height = screen.y
        )
    }

    suspend fun sendData(
        fullData: Boolean,
        settings: GameSettings,
        objectsArray: Array<GameObjectI>
    ) {
        var objects = objectsArray

        if (!fullData && myRocket != null) {
            objects = objects.filter {
                it !is GeoObject || it.getGeo() touchesRect viewRect()
            }.toTypedArray()
        }

        //log("Sending Game-Data! objects: ${objectsArray.joinToString()}")
        sendDirectly(
            SendFormat(
                "game-data",
                GameDataForSendingI(
                    settings = settings,
                    objects = objects.map { it.data() }.toTypedArray(),
                    galaxy = galaxy.data(),
                    messages = sendQueue.toTypedArray(),
                    fullData = fullData,
                    userView = this.myRocket?.userView()
                )
            )
        )
        sendQueue.clear()
    }

    fun onSuccessfullyJoined() {
        props = props.copy(galaxy = galaxy.props.name)
        log("successfully logged in!")
    }

    companion object {
        val userQueue = ArrayList<UserS>()
        val userSocketMap = HashMap<String, UserS>()

        private var idCounter = Int.MIN_VALUE
        fun newID(): String {
            idCounter ++
            if (idCounter == 0) idCounter ++
            return idCounter.toString()
        }
    }

    override fun log(str: String, color: Ansi?) {
        coloredLog("User ${props.name}", str, color, name = Ansi.GREEN)
    }
}