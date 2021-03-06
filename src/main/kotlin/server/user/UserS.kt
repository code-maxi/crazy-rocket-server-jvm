package server.user

import server.data_containers.ClientDataI
import server.data_containers.KeyboardI
import GameAdminI
import GameContainerPrevI
import JoinGameContainerI
import SetPrevGameRequestI
import server.data_containers.ResponseResult
import server.data_containers.SendFormat
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import io.ktor.http.cio.websocket.*
import server.adds.text.Ansi
import server.adds.Error.resultCatch
import server.adds.text.Logable
import server.adds.text.Text.formattedPrint
import server.adds.math.geom.GeoI
import server.adds.math.CrazyVector
import server.data_containers.*
import server.galaxy.GameContainer
import server.game.CrazyGame
import server.game.objects.abstct.AbstractGameObject
import server.game.objects.abstct.GeoObject

class UserS(private val session: DefaultWebSocketSession) : Logable {
    val id = newID()

    private var props = UserPropsI("UNDEFINED", id)
    private var gameJoined = false

    private val sendQueue = arrayListOf<SendFormat>()
    private var sendWholeDataCount = 0
    private var dataRequest: ClientDataRequestI? = null

    var prevGalaxy: String? = null

    private var clientData = ClientDataI(
        keyboard = KeyboardI(listOf()),
        screenSize = CrazyVector.zero()
    )

    private var gameContainer: GameContainer? = null

    init {
        log("initialized.")
        userQueue[id] = this
    }

    fun getProps() = props
    private fun gameContainerOrError() = gameContainer ?: throw GameContainerHasNotBeenInitializedEx()

    private suspend fun sendDirectly(v: SendFormat) { session.send(Frame.Text(Gson().toJson(v))) }

    suspend fun sendGalaxyData(gal: GameContainer) {
        sendDirectly(
            SendFormat(
            "prev-galaxy-data",
            GameContainerPrevI(props, gal.data())
        )
        )
    }

    private suspend fun setPrevGalaxy(prev: String) {
        val header = "prev-galaxy-result"
        val res = resultCatch(header, {
            val prevData = GameContainerPrevI(props, GameContainer.getGameContainer(prev).data())
            prevGalaxy = prev
            ResponseResult(true, data = prevData)
        })
        sendDirectly(SendFormat(header, res))
    }

    fun onSuccessfullyJoined(gc: GameContainer, join: JoinGameContainerI) {
        gameContainer = gc
        props = props.copy(
            name = join.userName,
            teamColor = join.teamColor,
            galaxy = join.galaxyName
        )
        clientData = clientData.copy(screenSize = join.screenSize)
    }

    fun onSuccessfullyGameJoined() {
        gameJoined = true
    }

    suspend fun onMessage(a: SendFormat) {
        //log("Receiving: $a")

        when(a.header) {
            "prev-galaxy" -> {
                val prev = Gson().fromJson(a.value.toString(), SetPrevGameRequestI::class.java)
                setPrevGalaxy(prev.galaxy)
            }
            "join-galaxy" -> {
                val result = resultCatch("join-galaxy-result", {
                    val join = Gson().fromJson(a.value.toString(), JoinGameContainerI::class.java)

                    log("User '${join.userName}' wants to join the galaxy '${join.galaxyName}.'")
                    log("Its data is: '$join'")

                    GameContainer.join(join, this)

                    log("User successfully joined!")

                    ResponseResult(true, data = props)
                }, mapOf("json-syntax" to WrongRequestEx(a.value)), true)

                log("Join galaxy result: $result")

                sendDirectly(
                    SendFormat(
                    "join-galaxy-result",
                    result
                )
                )
            }
            "start-game" -> {
                log("Want to start game...")

                val result = resultCatch("start-or-join-game-result", {
                    val admin = Gson().fromJson(a.value.toString(), GameAdminI::class.java)
                    gameContainerOrError().startGame(admin.password)

                    ResponseResult(
                        true,
                        data = GameStartI(CrazyGame.LISTING_KEYS)
                    )
                }, mapOf("class-cast" to WrongRequestEx(a.value)), true)

                log("start game result...")
                log(result.toString())
                log("")

                sendDirectly(
                    SendFormat(
                    "start-or-join-game-result",
                    result
                )
                )
            }
            "start-or-join-game" -> {
                val result = resultCatch("start-or-join-game-result", {
                    gameContainerOrError().joinGame(this)
                    ResponseResult(true)
                }, print = true)

                sendDirectly(
                    SendFormat(
                    "join-game-result",
                    result
                )
                )
            }
            "client-data-request" -> {
                try {
                    dataRequest = Gson().fromJson(a.value.toString(), ClientDataRequestI::class.java)
                    dataRequest?.let { gameContainerOrError().registerClientData(it) }
                }
                catch (ex: JsonSyntaxException) { JsonParseEx(a.value.toString(), "ClientDataRequestI").printError() }
                catch (ex: OwnException) { ex.printError() }
            }
            "close-connection" -> {
                this.onClose()
            }
        }
    }

    suspend fun onGameCalculated(
        settings: GamePropsI,
        objectsArray: List<AbstractGameObject>
    ) {
        if (dataRequest != null && gameContainer != null) {
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
                        galaxy = gameContainerOrError().data(),
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
        gameContainer?.closeUser(this)
        userQueue.remove(props.id)
    }

    override fun log(str: String, color: Ansi?) {
        formattedPrint("User ${props.name}", str, color, name = Ansi.GREEN)
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