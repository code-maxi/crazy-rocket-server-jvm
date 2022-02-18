package server.galaxy

import CreateNewGalaxyI
import GalaxyPasswordI
import GalaxyPasswordArrI
import GalaxyPropsI
import GalaxyI
import JoinGalaxyI
import SendFormat
import UserPropsI
import com.google.gson.Gson
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import server.Ansi
import server.FileA.createIf
import server.FileA.file
import server.KtorServer
import server.Text
import server.Text.coloredLog
import server.data.*
import server.game.Game
import server.game.GameConfig
import server.user.UserS
import java.io.File

class GalaxyS(
    var props: GalaxyPropsI
) {
    private val users = hashMapOf<String, UserS>()
    var game: Game? = null
    var state = "frozen"
    val clientAnswerChannel = Channel<>()

    private val sendGameQueue = arrayListOf<Pair<SendFormat, UserPropsI>>()

    private fun userList() = users.values.toTypedArray()

    fun data() = GalaxyI(
        userList().map { it.props }.toTypedArray(),
        props, state
    )

    init {
        log("created with password '${galaxyPassword(props.name)}'!")
    }

    private fun log(text: String, color: Ansi? = null) {
        coloredLog("Galaxy [${props.name}]", text, color, name = Ansi.CYAN)
    }

    suspend fun joinUser(u: UserS) {
        if (users[u.props.id] != null)
             throw DoesAlreadyExistEx("name", u.props.name)
        else {
            if (Text.validNameText(u.props.name, "user name", 3, 15)) {
                u.galaxy = this
                users[u.props.id] = u
                u.onSuccessfullyJoined()
                sendGalaxyData()
            }
        }
    }

    fun sendGame(message: SendFormat, u: UserPropsI) { sendGameQueue.add(message to u) }

    suspend fun deleteUser(u: UserS) {
        users.remove(u.props.id)
        sendGalaxyData()
    }

    private suspend fun sendGalaxyData() {
        if (game == null) {
            sendAllClients(SendFormat("galaxy-data", data()))
        }
    }

    private suspend fun setupGame(password: String) {
        log("Game setup 1!")

        checkMyPassword(password)

        game = Game(props.level, data().users, GameConfig(
            sendUser = { id, sendFormat -> users[id]?.onMessageFromGame(sendFormat) },
            onRocketCreated = { rocket -> users[rocket.userProps.id]?.myRocket = rocket }
        ))

        sendAllClients(SendFormat("game-created"))

        log("Game setup! 2")
    }

    fun startGame(password: String) {
        log("Start Game! t")
        KtorServer.gameCalculation.launch {
            log("Setup game 0!")
            setupGame(password)
            gameLoop()
        }
        log("Exit Start Game!")
    }

    private suspend fun gameLoop() {
        log("Starting Game Loop...")
        log("")

        val fullDataInterval = 1
        val estimatedTime = 30

        var oldTimestamp: Long
        var measuredTime = estimatedTime
        var fullDataIntervalCount = fullDataInterval

        while (game != null) {
            oldTimestamp = System.nanoTime()

            sendGameQueue.forEach { game!!.onMessage(it.first, it.second) }

            val factor = (measuredTime.toDouble() / estimatedTime.toDouble())

            game!!.calc(factor)

            userList().forEach {
                it.sendData(
                    fullDataIntervalCount == fullDataInterval,
                    game!!.settings,
                    game!!.objectList()
                )
            }

            if (fullDataIntervalCount < fullDataInterval) fullDataIntervalCount ++
            else fullDataIntervalCount = 0

            delay(20)

            measuredTime = ((System.nanoTime() - oldTimestamp) / 100000).toInt()
        }
    }

    private fun checkMyPassword(password: String) {
        if (!checkGalaxyPassword(props.name, password))
            throw InvalidPasswordEx(password, props.name)
    }

    private suspend fun sendAllClients(s: SendFormat) {
        userList().forEach { it.send(s) }
    }

    companion object {

        private var galaxies = hashMapOf<String, GalaxyS>()
        private var galaxyPasswords = hashMapOf<String, String>()

        fun galaxyList() = galaxies.values

        fun checkGalaxyPassword(galaxy: String, password: String) = galaxyPasswords[galaxy] == password
        fun galaxyPassword(galaxy: String) = galaxyPasswords[galaxy]

        private fun saveGalaxyState() {
            val home = File(System.getProperty("user.home"))
            val confFolder = createIf(home, ".config", "d")
            val crazyRocketFolder = createIf(confFolder, "crazy-rocket", "d")
            val galaxyFile = createIf(crazyRocketFolder, "galaxies.json", "f")

            val ob = GalaxyPasswordArrI(
                galaxyList().map { it.data().params }.toTypedArray(),
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
                if (galaxies[it.params.name] == null) galaxies[it.params.name] = GalaxyS(it.params)
                else throw NameAlreadyExistsEx(it.params.name)
            }
        }

        fun createGalaxy(g: CreateNewGalaxyI) {
            return if (galaxies[g.name] == null) {
                galaxies[g.name] = (GalaxyS(GalaxyPropsI(g.name, 1)))
                galaxyPasswords[g.name] = g.password
                saveGalaxyState()
            }
            else throw NameAlreadyExistsEx(g.name)
        }

        fun deleteGalaxy(g: GalaxyPasswordI) {
            if (galaxies[g.name] != null) {
                if (galaxyPasswords[g.name] == g.password) {
                    galaxies.remove(g.name)
                    galaxyPasswords.remove(g.name)
                    saveGalaxyState()
                } else throw InvalidPasswordEx(g.password, g.name)
            } else throw DoesNotExistEx("Galaxy", g.name)
        }

        suspend fun joinGalaxy(join: JoinGalaxyI, user: UserS) {
            return try { galaxies[join.galaxyName]!!.joinUser(user) }
              catch (ex: NullPointerException) { throw DoesNotExistEx("Galaxy", join.userName) }
        }

        fun getGalaxies() = galaxyList().toTypedArray()
    }
}