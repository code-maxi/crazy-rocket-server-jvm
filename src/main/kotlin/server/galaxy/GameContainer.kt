package server.galaxy

import CreateNewGameI
import GameConfigI
import GamePasswordI
import GameContainerPropsI
import GameContainerI
import JoinGameContainerI
import TeamColor
import TeamI
import TeamPropsI
import kotlinx.coroutines.*
import server.adds.text.Ansi
import server.adds.text.Text
import server.adds.text.Text.formattedPrint
import server.data_containers.*
import server.game.CrazyGame
import server.user.UserS

class GameContainer(
    name: String,
    private val config: GameConfigI
) {
    private var props = GameContainerPropsI(name, "queue")

    private val users = hashMapOf<String, UserS>()
    private var game: CrazyGame? = null

    private val teams = hashMapOf<TeamColor, ArrayList<String>>(
        TeamColor.RED to arrayListOf(),
        TeamColor.BLUE to arrayListOf(),
        TeamColor.GREEN to arrayListOf()
    )

    private fun userList() = users.values.toList()

    private fun teamData() = teams.map {
        TeamI(
            TeamPropsI(
                props.name,
                it.key.name,
                it.key.color
            ),
            it.value
        )
    }

    fun data() = GameContainerI(
        userList().map { it.getProps() },
        props, teamData()
    )

    init {
        log("created with password '${getPassword(props.name)}'!")
    }

    private fun log(text: String, color: Ansi? = null) {
        formattedPrint("Galaxy [${props.name}]", text, color, name = Ansi.CYAN)
    }

    suspend fun joinUser(u: UserS, joinData: JoinGameContainerI) {
        Text.checkValidName(joinData.userName, "user name", 3, 20)

        if (users.values.any { it.getProps().name == joinData.userName })
            throw NameAlreadyExistsEx(joinData.userName)

        val teamColor = TeamColor.stringToTeamColor(joinData.teamColor)

        if (!teams.containsKey(teamColor))
            throw TeamColorDoesNotExistEx(joinData.teamColor)

        if (teams[teamColor]!!.size >= config.maxUsersInTeam)
            throw TeamIsFull(teamColor.teamName, config.maxUsersInTeam)

        users[u.getProps().id] = u
        teams[teamColor]!!.add(u.id)

        u.onSuccessfullyJoined(this, joinData)

        sendPreviewDataToClients()
    }

    suspend fun closeUser(u: UserS) {
        users.remove(u.getProps().id)
        u.getProps().teamColor?.let {
            val teamColor = TeamColor.stringToTeamColor(it)
            teams[teamColor]?.remove(u.getProps().id)
        }
        sendPreviewDataToClients()
    }

    private suspend fun sendPreviewDataToClients() {
        users.forEach { it.value.sendGalaxyData(this) }
        UserS.userQueue.forEach {
            if (it.value.prevGalaxy == props.name) it.value.sendGalaxyData(this)
        }
    }

    private suspend fun setupGame(password: String) {
        checkMyPassword(password)

        game = CrazyGame(config, GameConfig(
            onRocketMessage = { id, sendFormat -> users[id]?.onMessageFromGame(sendFormat) },
        ))
        props = props.copy(state = "running")
        sendPreviewDataToClients()

        log("Game setup! 2")
    }

    fun startGame(password: String) {
        checkMyPassword(password)
        if (game == null) {
            GlobalScope.launch {
                setupGame(password)
                gameLoop()
            }
        } else throw GameIsAlreadyRunning()
    }

    private suspend fun gameLoop() {
        log("Starting Game Loop...")
        log("")

        val debugspeed: Int? = null

        val estimatedTime = debugspeed ?: 30

        var oldTimestamp: Long
        var measuredTime = estimatedTime

        while (game != null) {
            oldTimestamp = System.nanoTime()

            val factor = (measuredTime.toDouble() / estimatedTime.toDouble())

            game!!.onClientData(clientDataList.toList())
            game!!.calc(factor)

            userList().forEach {
                it.onGameCalculated(
                    game!!.props,
                    game!!.objectList()
                )
            }

            delay(debugspeed?.toLong() ?: 10)

            measuredTime = ((System.nanoTime() - oldTimestamp) / 100000).toInt()
        }
    }


    fun joinGame(user: UserS) {
        if (users.containsKey(user.id)) {

            //user.myRocket = getGame().addRocket(user.props, TODO())
        }
    }

    fun registerClientData(data: ClientDataRequestI) {
        //clientAnswerChannel.send(data)
        clientDataList.add(data)
    }

    private fun checkMyPassword(password: String) {
        if (getPassword(props.name) != password)
            throw InvalidPasswordEx(props.name)
    }

    companion object {

        private var gameContainers = hashMapOf<String, GameContainer>()
        private var galaxyPasswords = hashMapOf<String, String>()

        private fun gameContainers() = gameContainers.values
        fun getPassword(galaxy: String) = galaxyPasswords[galaxy]

        fun log(text: String, color: Ansi? = null) {
            formattedPrint("Galaxy Static", text, color, name = Ansi.PURPLE)
        }
        fun create(g: CreateNewGameI) {
            return if (!gameContainers.contains(g.name)) {
                gameContainers[g.name] = GameContainer(g.name, g.config)
                galaxyPasswords[g.name] = g.password

                log("Galaxy \"${g.name}\" successfully created.")
                //saveGalaxyState()
            }
            else throw NameAlreadyExistsEx(g.name)
        }

        fun delete(g: GamePasswordI) {
            if (gameContainers[g.name] != null) {
                if (galaxyPasswords[g.name] == g.password) {
                    gameContainers.remove(g.name)
                    galaxyPasswords.remove(g.name)
                    //saveGalaxyState()
                } else throw InvalidPasswordEx(g.name)
            } else throw GalaxyDoesNotExist(g.name)
        }

        suspend fun join(join: JoinGameContainerI, user: UserS) {
            return try { gameContainers[join.galaxyName]!!.joinUser(user, join) }
              catch (ex: NullPointerException) { throw GalaxyDoesNotExist(join.userName) }
        }

        fun getGameContainers() = gameContainers().map { it.data() }

        fun getGameContainer(key: String) = gameContainers[key] ?: throw GalaxyDoesNotExist(key)

        /*
        private fun saveGalaxyState() {
            val home = File(System.getProperty("user.home"))
            val confFolder = createIf(home, ".config", "d")
            val crazyRocketFolder = createIf(confFolder, "crazy-rocket", "d")
            val galaxyFile = createIf(crazyRocketFolder, "galaxies.json", "f")

            val ob = GalaxyPasswordArrI(
                galaxyList().map { it.data().props }.toTypedArray(),
                galaxyPasswords.map { GalaxyPasswordI(it.key, it.value) }.toTypedArray()
            )

            val toJ = Gson().toJson(ob)
            galaxyFile.writeText(toJ)
        }

        fun readGalaxyState() {
            val home = System.getProperty("user.home")
            val galaxyFile = File(file(home, ".config", "crazy-rocket"), "galaxies.json")
            val text = galaxyFile.readText()

            val galaxyPassword = try {
                Gson().fromJson(text, GalaxyPasswordArrI::class.java)
            } catch (ex: Exception) {
                GalaxyPasswordArrI(arrayOf(), arrayOf())
            }

            galaxyPassword.passwords.forEach { galaxyPasswords[it.name] = it.password }

            galaxyPassword.items.map { GalaxyI(arrayOf(), it, "frozen") }.forEach {
                if (galaxies[it.props.name] == null) galaxies[it.props.name] = GalaxyS(it.props)
                else throw NameAlreadyExistsEx(it.props.name)
            }
        }
        */
    }
}