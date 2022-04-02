package server.user

import server.data_containers.ClientDataI
import server.data_containers.KeyboardI
import GalaxyAdminI
import GalaxyPrevI
import JoinGalaxyI
import PrevGalaxyRequestI
import ResponseResult
import SendFormat
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import io.ktor.http.cio.websocket.*
import server.adds.text.Ansi
import server.adds.Error.resultCatch
import server.adds.text.Logable
import server.adds.text.Text.coloredLog
import server.adds.math.geom.GeoI
import server.adds.math.CrazyVector
import server.data_containers.*
import server.galaxy.GalaxyS
import server.game.CrazyGame
import server.game.objects.AbstractGameObject
import server.game.objects.GeoObject
import server.game.objects.CrazyRocket

class UserS(private val session: DefaultWebSocketSession) : Logable {
    val id = newID()

    var props = UserPropsI("UNDEFINED", id)

    private val sendQueue = arrayListOf<SendFormat>()
    private var sendWholeDataCount = 0
    private var dataRequest: ClientDataRequestI? = null

    var prevGalaxy: String? = null

    private var clientData = ClientDataI(
        keyboard = KeyboardI(listOf()),
        screenSize = CrazyVector.zero()
    )

    private var galaxy: GalaxyS? = null
    var myRocket: CrazyRocket? = null

    init {
        log("initialized.")
        userQueue[id] = this
    }

    private fun galaxyInitialized() = this.galaxy != null

    private fun getMyGalaxy() = galaxy ?: throw GalaxyHasNotBeenInitializedEx()

    private suspend fun sendDirectly(v: SendFormat) { session.send(Frame.Text(Gson().toJson(v))) }
    suspend fun send(v: SendFormat) { if (galaxyInitialized()) sendQueue.add(v) else sendDirectly(v) }


    suspend fun catchOwnExAndSend(callback: () -> Unit) {
        try { callback() }
        catch (ex: OwnException) { send(SendFormat("error-occurred", ex.exceptionData)) }
    }

    private suspend fun sendResultCatch(
        header: String,
        callback: suspend () -> ResponseResult,
        onError: Map<String, OwnException> = mapOf()
    ) {
        val res = resultCatch(header, callback, onError)
        send(SendFormat(header, res))
    }

    suspend fun sendGalaxyData(gal: GalaxyS) {
        sendDirectly(SendFormat("prev-galaxy-data", GalaxyPrevI(props, gal.data())))
    }

    private suspend fun setPrevGalaxy(prev: String) {
        sendResultCatch("prev-galaxy-result", {
            val prevData = GalaxyPrevI(props, GalaxyS.getGalaxy(prev).data())
            prevGalaxy = prev
            ResponseResult(true, data = prevData)
        })
    }

    fun onSuccessfullyJoined(gal: GalaxyS, join: JoinGalaxyI) {
        galaxy = gal
        props = props.copy(
            name = join.userName,
            teamColor = join.teamColor,
            galaxy = join.galaxyName
        )
        clientData = clientData.copy(screenSize = join.screenSize)
    }

    suspend fun onMessage(a: SendFormat) {
        //log("Receiving: $a")

        when(a.header) {
            "prev-galaxy" -> {
                val prev = Gson().fromJson(a.value.toString(), PrevGalaxyRequestI::class.java)
                setPrevGalaxy(prev.galaxy)
            }
            "join-galaxy" -> {
                val result = resultCatch("join-galaxy-result", {
                    val join = Gson().fromJson(a.value.toString(), JoinGalaxyI::class.java)

                    log("User '${join.userName}' wants to join the galaxy '${join.galaxyName}.'")
                    log("Its data is: '$join'")

                    GalaxyS.joinGalaxy(join, this)

                    log("User successfully joined!")

                    ResponseResult(true, data = props)
                }, mapOf("json-syntax" to WrongRequestEx(a.value)), true)

                log("Join galaxy result: $result")

                sendDirectly(SendFormat(
                    "join-galaxy-result",
                    result
                ))
            }
            "start-game" -> {

                log("Want to start game...")

                val result = resultCatch("start-game-result", {
                    val admin = Gson().fromJson(a.value.toString(), GalaxyAdminI::class.java)
                    getMyGalaxy().startGame(admin.password)

                    ResponseResult(
                        true,
                        data = GameStartI(CrazyGame.LISTINING_KEYS)
                    )
                }, mapOf("class-cast" to WrongRequestEx(a.value)), true)

                log("start game result...")
                log(result.toString())
                log("")

                sendDirectly(SendFormat(
                    "start-game-result",
                    result
                ))
            }
            "join-game" -> {
                val result = resultCatch("start-game-result", {
                    getMyGalaxy().joinGame(this)
                    ResponseResult(true)
                }, print = true)

                sendDirectly(SendFormat(
                    "join-game-result",
                    result
                ))
            }
            "client-data-request" -> {
                try {
                    dataRequest = Gson().fromJson(a.value.toString(), ClientDataRequestI::class.java)
                    dataRequest?.let { getMyGalaxy().registerClientData(it) }
                }
                catch (ex: JsonSyntaxException) { JsonParseEx(a.value.toString(), "ClientDataRequestI").printError() }
                catch (ex: OwnException) { ex.printError() }
            }
            "close-connection" -> {
                this.onClose()
            }
        }
    }

    fun onMessageFromGame(s: SendFormat) {
        log("Data from Game: '$s'")
    }

    suspend fun onGameCalculated(
        settings: GamePropsI,
        objectsArray: List<AbstractGameObject>
    ) {
        if (dataRequest != null && galaxy != null) {
            if (sendWholeDataCount < WHOLE_DATA_INTERVAL) sendWholeDataCount ++
            else sendWholeDataCount = 0

            var objects = objectsArray
            val fullData = sendWholeDataCount == WHOLE_DATA_INTERVAL

            if (!fullData && myRocket != null) {
                objects = objects.filter {
                    it !is GeoObject || it.getGeo().rect() touchesRect viewRect().rect()
                }
            }

            sendDirectly(
                SendFormat(
                    "game-data",
                    GameDataForSendingI(
                        props = settings,
                        objects = objects.map { it.data() },
                        galaxy = getMyGalaxy().data(),
                        messages = sendQueue.toList(),
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

    private fun viewRect(): GeoI {
        val screen = this.clientData.screenSize * 1.5 * (1/this.myRocket!!.zoom)
        return GeoI(
            pos = this.myRocket!!.eye - screen/2.0,
            width = screen.x,
            height = screen.y
        )
    }

    suspend fun onClose() {
        log("Closing Connection.")
        galaxy?.closeUser(this)
        userQueue.remove(props.id)
    }

    override fun log(str: String, color: Ansi?) {
        coloredLog("User ${props.name}", str, color, name = Ansi.GREEN)
    }

    companion object {
        private var idCounter = Int.MIN_VALUE
        const val WHOLE_DATA_INTERVAL = 20
        val userQueue = hashMapOf<String, UserS>()

        fun newID(): String {
            idCounter ++
            if (idCounter == 0) idCounter ++
            return idCounter.toString()
        }
    }
}