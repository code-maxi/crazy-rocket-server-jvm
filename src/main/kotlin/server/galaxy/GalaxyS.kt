package server.galaxy

import CreateNewGalaxyI
import GalaxyPasswordI
import GalaxySettingsArrI
import GalaxyPropsI
import GalaxyWithoutObjectsI
import JoinGalaxyI
import SendFormat
import com.google.gson.Gson
import server.FileA.createIf
import server.FileA.file
import server.Text
import server.data.*
import server.game.Game
import server.user.UserS
import java.io.File
import kotlin.concurrent.thread

class GalaxyS(
    var props: GalaxyPropsI
) {
    private val users = arrayListOf<UserS>()
    var game: Game? = null
    var state = "frozen"

    fun data() = GalaxyWithoutObjectsI(
        users.map { it.props }.toTypedArray(),
        props, state
    )

    fun joinUser(u: UserS) {
        if (users.any { it.props.name == u.props.name })
             throw DoesAlreadyExistEx("name", u.props.name)
        else {
            if (Text.validNameText(u.props.name, "user name", 3, 15)) {
                u.galaxy = this
                users.add(u)
                u.onSuccessfullyJoined()
                sendGalaxyData()
            }
        }
    }

    private fun sendGalaxyData() {
        if (game == null) {
            sendAllClients(SendFormat("galaxy-data", data()))
        }
    }

    fun startGame(password: String) {
        checkMyPassword(password)

        game = Game(props.level)
        sendAllClients(SendFormat("game-created"))

        thread {
            var oldTimestamp: Long
            var newTimestamp: Long
            var fullDataIntervalCount = 0
            val fullDataInterval = 20

            while (game != null) {
                oldTimestamp = System.nanoTime()

                game!!.calc(1.0)

                newTimestamp = System.nanoTime()
                println("timestamp: ${newTimestamp - oldTimestamp}")

                users.forEach {
                    it.sendData(
                        fullDataIntervalCount == fullDataInterval,
                        game!!.settings,
                        game!!.objects.toTypedArray()
                    )
                }

                if (fullDataIntervalCount < fullDataInterval) fullDataIntervalCount ++
                else fullDataIntervalCount = 0
            }
        }
    }

    fun checkMyPassword(password: String) {
        if (!checkGalaxyPassword(props.name, password))
            throw InvalidPasswordEx(password, props.name)
    }

    private fun sendAllClients(s: SendFormat) {
        users.forEach { it.send(s) }
    }

    companion object {
        const val SEND_WHOLE_DATA_INTERVAL = 15

        private var galaxies = arrayListOf<GalaxyS>()
        private var galaxyPasswords = hashMapOf<String, String>()

        fun checkGalaxyPassword(galaxy: String, password: String) = galaxyPasswords[galaxy] == password

        private fun saveGalaxyState() {
            val home = File(System.getProperty("user.home"))
            val confFolder = createIf(home, ".config", "d")
            val crazyRocketFolder = createIf(confFolder, "crazy-rocket", "d")
            val galaxyFile = createIf(crazyRocketFolder, "galaxies.json", "f")

            val ob = GalaxySettingsArrI(
                galaxies.map { it.data().params }.toTypedArray(),
                galaxyPasswords.map { GalaxyPasswordI(it.key, it.value) }.toTypedArray()
            )

            val toJ = Gson().toJson(ob)
            galaxyFile.writeText(toJ)
        }

        fun readGalaxyState() {
            val home = System.getProperty("user.home")
            val galaxyFile = File(file(home, ".config", "crazy-rocket"), "galaxies.json")
            val text = galaxyFile.readText()

            val galaxiesData = try {
                val parsed = Gson().fromJson(text, GalaxySettingsArrI::class.java)
                parsed.items.map { GalaxyWithoutObjectsI(arrayOf(), it, "frozen") }
            } catch (ex: Exception) {
                arrayListOf()
            }

            galaxiesData.forEach {
                galaxies.add(GalaxyS(it.params))
            }
        }

        fun createGalaxy(g: CreateNewGalaxyI) {
            return if (!galaxies.any { it.props.name == g.name }) {
                galaxies.add(GalaxyS(GalaxyPropsI(g.name, 1)))
                galaxyPasswords[g.name] = g.password
                saveGalaxyState()
            }
            else throw NameAlreadyExistsEx(g.name)
        }

        fun removeGalaxy(g: GalaxyPasswordI) {
            if (galaxies.any { it.props.name == g.name }) {
                if (galaxyPasswords[g.name] == g.password) {
                    galaxies.removeIf { it.props.name == g.name }
                    galaxyPasswords.remove(g.name)
                    saveGalaxyState()
                } else throw InvalidPasswordEx(g.password, g.name)
            } else throw DoesNotExistEx("Galaxy", g.name)
        }

        fun joinGalaxy(join: JoinGalaxyI, user: UserS) {
            return try { galaxies.find { join.galaxyName == it.props.name }!!.joinUser(user) }
              catch (ex: Exception) { throw DoesNotExistEx("Galaxy", join.userName) }
        }

        fun getGalaxies() = galaxies.toTypedArray()
    }
}