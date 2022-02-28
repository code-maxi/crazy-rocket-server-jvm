package server.galaxy

import CreateNewGalaxyI
import GalaxyConfigI
import GalaxyPasswordI
import GalaxyPropsI
import GalaxyI
import JoinGalaxyI
import SendFormat
import TeamColor
import TeamI
import TeamPropsI
import kotlinx.coroutines.*
import server.Ansi
import server.Text
import server.Text.coloredLog
import server.data.*
import server.game.Game
import server.game.GameConfig
import server.user.UserS
import stringToTeamColor

class GalaxyS(
    name: String,
    private val config: GalaxyConfigI
) {
    var props = GalaxyPropsI(name, "queue")
    private val users = hashMapOf<String, UserS>()
    private var game: Game? = null
    private val teams = hashMapOf<TeamColor, ArrayList<String>>(
        TeamColor.RED to arrayListOf(),
        TeamColor.BLUE to arrayListOf(),
        TeamColor.GREEN to arrayListOf()
    )

    //val clientAnswerChannel = Channel<ClientAnswerI>()

    private val sendGameQueue = arrayListOf<Pair<SendFormat, UserPropsI>>()
    private fun userList() = users.values.toTypedArray()

    fun getGame() = game ?: throw GameNotInitializedEx(props.name)

    private fun teamData() = teams.map {
        TeamI(
            TeamPropsI(
                props.name,
                it.key.name,
                it.key.color
            ),
            it.value.toTypedArray()
        )
    }.toTypedArray()

    fun data() = GalaxyI(
        userList().map { it.props }.toTypedArray(),
        props, teamData()
    )

    init {
        log("created with password '${galaxyPassword(props.name)}'!")
    }

    private fun log(text: String, color: Ansi? = null) {
        coloredLog("Galaxy [${props.name}]", text, color, name = Ansi.CYAN)
    }

    suspend fun joinUser(u: UserS, joinData: JoinGalaxyI) {
        Text.checkValidName(joinData.userName, "user name", 3, 20)

        if (users.values.any { it.props.name == joinData.userName })
            throw NameAlreadyExistsEx(joinData.userName)

        val teamColor = stringToTeamColor(joinData.teamColor)

        if (!teams.containsKey(teamColor))
            throw TeamColorDoesNotExistEx(joinData.teamColor)

        if (teams[teamColor]!!.size >= config.maxUsersInTeam)
            throw TeamIsFull(teamColor.teamName, config.maxUsersInTeam)

        users[u.props.id] = u
        teams[teamColor]!!.add(u.id)
        u.onSuccessfullyJoined(this, joinData)

        sendGalaxyDataToClients()
    }

    fun sendGame(message: SendFormat, u: UserPropsI) { sendGameQueue.add(message to u) }

    suspend fun closeUser(u: UserS) {
        users.remove(u.props.id)
        u.props.teamColor?.let {
            val teamColor = stringToTeamColor(it)
            teams[teamColor]?.remove(u.props.id)
        }
        sendGalaxyDataToClients()
    }

    private suspend fun sendGalaxyDataToClients() {
        users.forEach { it.value.sendGalaxyData(this) }
        UserS.userQueue.forEach {
            if (it.value.prevGalaxy == props.name) it.value.sendGalaxyData(this)
        }
    }

    private suspend fun setupGame(password: String) {
        log("Game setup 1!")

        checkMyPassword(password)

        game = Game(config, GameConfig(
            sendUser = { id, sendFormat -> users[id]?.onMessageFromGame(sendFormat) },
            onRocketCreated = { rocket -> users[rocket.userProps.id]?.myRocket = rocket }
        ))

        props = props.copy(state = "running")

        sendGalaxyDataToClients()

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

    fun joinGame(user: UserS) {
        if (users.containsKey(user.id)) {
            getGame().addUserRocket(user.props) { user.myRocket = it }
        }
    }

    fun registerClientData(data: ClientDataRequestI) {
        //clientAnswerChannel.send(data)
        game?.onClientData(data)
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

            game!!.calc(factor)

            userList().forEach {
                it.onGameCalculated(
                    game!!.settings,
                    game!!.objectList()
                )
            }

            delay(debugspeed?.toLong() ?: 10)

            measuredTime = ((System.nanoTime() - oldTimestamp) / 100000).toInt()
        }
    }

    private fun checkMyPassword(password: String) {
        if (!checkGalaxyPassword(props.name, password))
            throw InvalidPasswordEx(props.name)
    }

    private suspend fun sendAllClients(s: SendFormat) {
        users.forEach { it.value.send(s) }
    }

    companion object {

        private var galaxies = hashMapOf<String, GalaxyS>()
        private var galaxyPasswords = hashMapOf<String, String>()

        fun galaxyList() = galaxies.values

        fun checkGalaxyPassword(galaxy: String, password: String) = galaxyPasswords[galaxy] == password
        fun galaxyPassword(galaxy: String) = galaxyPasswords[galaxy]

        fun log(text: String, color: Ansi? = null) {
            coloredLog("Galaxy Static", text, color, name = Ansi.PURPLE)
        }

        fun createGalaxy(g: CreateNewGalaxyI) {
            return if (!galaxies.contains(g.name)) {
                galaxies[g.name] = GalaxyS(g.name, g.config)
                galaxyPasswords[g.name] = g.password

                log("Galaxy \"${g.name}\" successfully created.")
                //saveGalaxyState()
            }
            else throw NameAlreadyExistsEx(g.name)
        }

        fun deleteGalaxy(g: GalaxyPasswordI) {
            if (galaxies[g.name] != null) {
                if (galaxyPasswords[g.name] == g.password) {
                    galaxies.remove(g.name)
                    galaxyPasswords.remove(g.name)
                    //saveGalaxyState()
                } else throw InvalidPasswordEx(g.name)
            } else throw GalaxyDoesNotExist(g.name)
        }

        suspend fun joinGalaxy(join: JoinGalaxyI, user: UserS) {
            return try { galaxies[join.galaxyName]!!.joinUser(user, join) }
              catch (ex: NullPointerException) { throw GalaxyDoesNotExist(join.userName) }
        }

        fun getGalaxies() = galaxyList().toTypedArray()

        fun getGalaxy(key: String) = galaxies[key] ?: throw GalaxyDoesNotExist(key)

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