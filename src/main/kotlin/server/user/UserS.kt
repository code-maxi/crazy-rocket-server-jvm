package server.user

import ClientDataI
import SendFormat
import UserPropsI
import UserViewI
import com.google.gson.Gson
import org.java_websocket.WebSocket
import server.LogType
import server.Logable
import server.coloredLog
import server.galaxy.GalaxyS


class UserS(val socket: WebSocket, id: Int) : Logable {
    var props = UserPropsI("UNDEFINED", null, id)
    private var inGame = false

    lateinit var view: UserViewI
    lateinit var clientData: ClientDataI
    lateinit var galaxy: GalaxyS

    init {
        userQueue.add(this)
        userSocketMap.put(socket, this)
    }

    override fun log(str: String, type: LogType) {
        coloredLog("UserS[${props.name}, ${props.id}] logs: ", str, type)
    }
    fun send(v: SendFormat) { socket.send(Gson().toJson(v)) }

    fun onMessage(a: SendFormat) {
        log("Recieving: $a")
        when(a.header) {
            "create new galaxy" -> {
                log("creating galaxy")
            }
            "join galaxy" -> {
                log("joining galaxy")
            }
        }
    }

    fun onOpen() {
        log("Opened.")
    }

    fun onClose() {
        log("Closed.")
    }

    fun onError(ex: Exception) {
        println("onError::" + ex.message)
    }

    fun isInGame() = inGame

    companion object {
        val userQueue = ArrayList<UserS>()
        val userSocketMap = HashMap<WebSocket, UserS>()
        fun findUser(s: WebSocket) = userSocketMap[s]!!
    }
}