package server.user

import server.data.ClientDataI
import server.data.ClientKeyboardI
import GalaxyAdminI
import JoinGalaxyI
import ResponseResult
import SendFormat
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import io.ktor.http.cio.websocket.*
import server.Ansi
import server.Logable
import server.Text.coloredLog
import server.data.*
import server.galaxy.GalaxyS
import server.game.Game
import server.game.objects.GeoObject
import server.game.objects.Rocket

class UserS(private val session: DefaultWebSocketSession) : Logable {
    val id = newID()
    var props = UserPropsI("UNDEFINED", null, id)
    private var inGame = false
    private val sendQueue = arrayListOf<SendFormat>()
    private var sendWholeDataCount = 0
    private var dataRequest: ClientDataRequestI? = null

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
                    val admin = Gson().fromJson(a.value.toString(), GalaxyAdminI::class.java)
                    galaxy.startGame(admin.password)
                    ResponseResult(
                        true,
                        data = GameStartI(Game.LISTINING_KEYS)
                    )
                }
                catch (ex: ClassCastException) { WrongRequestEx(a.value).responseResult() }
                catch (ex: OwnException) { ex.responseResult() }

                log("starting-game-result: $result")

                sendDirectly(SendFormat(
                    "start-game-result",
                    result
                ))
            }
            "client-data-request" -> {
                try {
                    dataRequest = Gson().fromJson(a.value.toString(), ClientDataRequestI::class.java)
                    dataRequest?.let {
                        if (galaxyInitialized()) galaxy.registerClientData(it)
                    }
                }
                catch (ex: JsonSyntaxException) {
                    JsonParseEx(a.value.toString(), "ClientDataRequestI").printError()
                }
            }
        }
    }

    fun onMessageFromGame(s: SendFormat) {
        log("Data from Game: '$s'")
    }

    suspend fun onGameCalculated(
        settings: GameSettings,
        objectsArray: Array<GameObjectI>
    ) {
        if (dataRequest != null) {
            if (sendWholeDataCount < WHOLE_DATA_INTERVAL) sendWholeDataCount ++
            else sendWholeDataCount = 0

            var objects = objectsArray
            val fullData = sendWholeDataCount == WHOLE_DATA_INTERVAL

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
                        userView = this.myRocket?.userView(),
                        yourUserProps = props
                    )
                )
            )
            sendQueue.clear()

            dataRequest = null
        }
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

    /*suspend fun sendData(
        fullData: Boolean,
        settings: GameSettings,
        objectsArray: Array<GameObjectI>
    ) {

    }*/

    fun onSuccessfullyJoined() {
        props = props.copy(galaxy = galaxy.props.name)
        log("successfully logged in!")
    }

    companion object {
        private var idCounter = Int.MIN_VALUE
        const val WHOLE_DATA_INTERVAL = 20

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