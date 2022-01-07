package server.user

import ClientDataI
import SendFormat
import UserI
import UserPropsI
import UserViewI
import server.galaxy.GalaxyS
import java.net.Socket

class UserS(socket: Socket, id: Int) : SocketUser(socket) {
    var props = UserPropsI("UNDEFINED", null, id)
    private var inGame = false

    lateinit var view: UserViewI
    lateinit var clientData: ClientDataI
    lateinit var galaxy: GalaxyS

    init {
        quiet = false
        userQueue.add(this)
    }

    override fun log(str: String) { println("UserS[${props.name}, ${props.id}] logs: $str") }

    override fun onMessage(a: SendFormat) {
        when(a.header) {
            "create new galaxy" -> {
                log("creating galaxy")
            }
            "join galaxy" -> {
                log("joining galaxy")
            }
        }
    }

    fun isInGame() = inGame

    override fun onConnection() {
        super.onConnection()
        log("Connected!")
        sendMessage(SendFormat("hello", "Hello, my Friend!"))
    }

    companion object {
        val userQueue = ArrayList<UserS>()
    }
}