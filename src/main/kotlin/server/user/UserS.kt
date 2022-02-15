package server.user

import ClientDataI
import ClientKeyboardI
import GalaxyAdminI
import JoinGalaxyI
import ResponseResult
import SendFormat
import UserPropsI
import com.google.gson.Gson
import com.google.protobuf.Message
import server.LogType
import server.Logable
import server.coloredLog
import server.data.*
import server.galaxy.GalaxyS
import server.game.objects.GeoObject
import server.game.objects.Rocket
import javax.websocket.*
import javax.websocket.DecodeException
import javax.websocket.EncodeException
import javax.websocket.server.ServerEndpoint


@ServerEndpoint(
    "/",
    decoders = [MessageDecoder::class],
    encoders = [MessageEncoder::class]
)
class UserS : Logable {
    val id = newID()
    var props = UserPropsI("UNDEFINED", null, id)
    private var inGame = false
    private val sendQueue = arrayListOf<SendFormat>()
    lateinit var socket: Session

    @OnOpen
    fun onOpen(session: Session) {
        log("New User Recieved...")
        socket = session
        userSocketMap[socket.id] = this
        userQueue.add(this)
    }

    @OnMessage
    fun onMessage(session: Session, message: SendFormat) {
        userSocketMap[session.id]?.onMessage(message)
    }

    @OnClose
    fun onClose(session: Session) {
        galaxy.deleteUser(this)
        log("Closed.")
    }

    @OnMessage
    fun onError(session: Session, throwable: Throwable) {
        log("Websocket Error!")
        throwable.printStackTrace()
    }

    private var clientData = ClientDataI(
        keyboard = ClientKeyboardI(arrayOf()),
        screenSize = VectorI.zero()
    )
    lateinit var galaxy: GalaxyS
    var myRocket: Rocket? = null

    private fun galaxyInitialized() = this::galaxy.isInitialized

    override fun log(str: String, type: LogType) {
        coloredLog("UserS[${props.name}, ${props.id}] logs: ", str, type)
    }
    private fun sendDirectly(v: SendFormat) { socket.basicRemote.sendText(Gson().toJson(v)) }
    fun send(v: SendFormat) { if (galaxyInitialized()) sendQueue.add(v) else sendDirectly(v) }

    private fun onMessage(a: SendFormat) {
        log("Receiving: $a")

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
                log("Starting game...")

                val result = try {
                    val admin = Gson().fromJson(a.value.toString(), GalaxyAdminI::class.java)
                    galaxy.startGame(admin.password)
                    ResponseResult(true)
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

    fun sendData(
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
}

class MessageEncoder : Encoder.Text<SendFormat?> {
    @Throws(EncodeException::class)
    override fun encode(message: SendFormat?): String {
        return gson.toJson(message)
    }

    override fun init(endpointConfig: EndpointConfig?) {
        // Custom initialization logic
    }

    override fun destroy() {
        // Close resources
    }

    companion object {
        private val gson = Gson()
    }
}

class MessageDecoder : Decoder.Text<SendFormat?> {
    @Throws(DecodeException::class)
    override fun decode(s: String?): SendFormat {
        return Companion.gson.fromJson(s, SendFormat::class.java)
    }

    override fun willDecode(s: String?): Boolean {
        return s != null
    }

    override fun init(endpointConfig: EndpointConfig?) {
        // Custom initialization logic
    }

    override fun destroy() {
        // Close resources
    }

    companion object {
        private val gson = Gson()
    }
}