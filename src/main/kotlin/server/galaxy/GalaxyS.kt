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
import server.FileA.createIf
import server.FileA.file
import server.Text
import server.data.*
import server.game.Game
import server.game.GameConfig
import server.user.UserS
import java.io.File
import kotlin.concurrent.thread

class GalaxyS(
    var props: GalaxyPropsI
) {
    private val users = hashMapOf<String, UserS>()
    var game: Game? = null
    var state = "frozen"

    private val sendGameQueue = arrayListOf<Pair<SendFormat, UserPropsI>>()

    fun userList() = users.values.toTypedArray()

    fun data() = GalaxyI(
        userList().map { it.props }.toTypedArray(),
        props, state
    )

    init {
        println("Galaxy ${Gson().toJson(data())} with password '${galaxyPassword(props.name)}' created!")
    }

    fun joinUser(u: UserS) {
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

    fun deleteUser(u: UserS) {
        users.remove(u.props.id)
        sendGalaxyData()
    }

    private fun sendGalaxyData() {
        if (game == null) {
            sendAllClients(SendFormat("galaxy-data", data()))
        }
    }

    fun startGame(password: String) {
        checkMyPassword(password)

        game = Game(props.level, data().users, GameConfig(
            sendUser = { id, sendFormat -> users[id]?.onMessageFromGame(sendFormat) },
            onRocketCreated = { rocket -> users[rocket.userProps.id]?.myRocket = rocket }
        ))
        sendAllClients(SendFormat("game-created"))

        thread {
            val fullDataInterval = 1
            val estimatedTime = 300

            var oldTimestamp: Long
            var measuredTime = estimatedTime
            var fullDataIntervalCount = fullDataInterval

            while (game != null) {
                oldTimestamp = System.nanoTime()

                Thread.sleep(10)

                sendGameQueue.forEach { game!!.onMessage(it.first, it.second) }

                println("$measuredTime vs. $estimatedTime")

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

                Thread.sleep(10)

                measuredTime = ((System.nanoTime() - oldTimestamp) / 100000).toInt()
            }
        }
    }

    fun checkMyPassword(password: String) {
        if (!checkGalaxyPassword(props.name, password))
            throw InvalidPasswordEx(password, props.name)
    }

    private fun sendAllClients(s: SendFormat) {
        userList().forEach { it.send(s) }
    }

    companion object {
        const val SEND_WHOLE_DATA_INTERVAL = 15

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

        fun removeGalaxy(g: GalaxyPasswordI) {
            if (galaxies[g.name] != null) {
                if (galaxyPasswords[g.name] == g.password) {
                    galaxies.remove(g.name)
                    galaxyPasswords.remove(g.name)
                    saveGalaxyState()
                } else throw InvalidPasswordEx(g.password, g.name)
            } else throw DoesNotExistEx("Galaxy", g.name)
        }

        fun joinGalaxy(join: JoinGalaxyI, user: UserS) {
            return try { galaxies[join.galaxyName]!!.joinUser(user) }
              catch (ex: NullPointerException) { throw DoesNotExistEx("Galaxy", join.userName) }
        }

        fun getGalaxies() = galaxyList().toTypedArray()
    }
}