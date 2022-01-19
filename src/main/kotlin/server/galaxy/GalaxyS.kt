package server.galaxy

import CreateNewGalaxyI
import GalaxyPasswordI
import GalaxySettingsArrI
import GalaxyPropsI
import GalaxyWithoutObjectsI
import JsonStatusI
import SendFormat
import com.google.gson.Gson
import server.FileA.createIf
import server.FileA.file
import server.user.UserS
import java.io.File

class GalaxyS {
    val users = arrayListOf<UserS>()
    private var swdiCount = 0

    init {

    }

    fun sendAllClients(s: SendFormat) { users.forEach { it.send(s) } }

    companion object {
        const val SEND_WHOLE_DATA_INTERVAL = 15

        private var galaxies = arrayListOf<GalaxyWithoutObjectsI>()
        private var galaxyPasswords = hashMapOf<String, String>()

        private fun saveGalaxyState() {
            println("saveGalaxyState: in")
            val home = File(System.getProperty("user.home"))
            println("saveGalaxyState: homef")
            val confFolder = createIf(home, ".config", "d")
            println("saveGalaxyState: conff")
            val crazyRocketFolder = createIf(confFolder, "crazy-rocket", "d")
            println("saveGalaxyState: crazyf")
            val galaxyFile = createIf(crazyRocketFolder, "galaxies.json", "f")
            println("saveGalaxyState: galaxyf")
            val ob = GalaxySettingsArrI(
                galaxies.map { it.params }.toTypedArray(),
                galaxyPasswords.map { GalaxyPasswordI(it.key, it.value) }.toTypedArray()
            )
            println("saveGalaxyState: settingsa")
            val toJ = Gson().toJson(ob)
            println("saveGalaxyState: galaxyf")
            galaxyFile.writeText(toJ)
            println("saveGalaxyState: textw")
        }
        fun readGalaxyState() {
            val home = System.getProperty("user.home")
            val galaxyFile = File(file(home, ".config", "crazy-rocket"), "galaxies.json")
            val text = galaxyFile.readText()
            galaxies = try {
                val parsed = Gson().fromJson(text, GalaxySettingsArrI::class.java)

                parsed.items
                    .map { GalaxyWithoutObjectsI(arrayOf(), it, "frozen") }
                    .toCollection(ArrayList())
            } catch (ex: Exception) {
                arrayListOf()
            }
        }

        fun addGalaxy(g: CreateNewGalaxyI): String {
            return if (!galaxies.any { it.params.name == g.name }) {
                println("addGalaxy: in if")
                galaxies.add(GalaxyWithoutObjectsI(
                    arrayOf(),
                    GalaxyPropsI(g.name, 1),
                    "frozen")
                )
                println("addGalaxy: galaxies added")
                galaxyPasswords[g.name] = g.password
                println("addGalaxy: password set")
                saveGalaxyState()
                println("addGalaxy: state saved")
                println()
                "successful"
            }
            else "galaxy name already exist"
        }

        fun removeGalaxy(g: GalaxyPasswordI): String {
            return if (galaxies.any { it.params.name == g.name }) {
                if (galaxyPasswords[g.name] == g.password) {
                    galaxies.removeIf { it.params.name == g.name }
                    galaxyPasswords.remove(g.name)
                    saveGalaxyState()

                    "successful"
                } else "invalid password"
            } else "galaxy does not exist anymore"
        }

        fun getGalaxies() = galaxies.toTypedArray()
    }
}