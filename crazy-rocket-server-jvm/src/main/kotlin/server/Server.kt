package server

import server.user.UserS
import java.net.ServerSocket
import kotlin.concurrent.thread

lateinit var server: Server

class Server(port: Int) {
    var serverSocket = ServerSocket(port)
    var alive = true

    fun listen() {
        thread {
            while (alive) {
                log("New User Recieved...")
                val socket = serverSocket.accept()
                UserS(socket, newID())
            }
        }
    }

    fun stop() { alive = false }
    fun log(str: String) { println("Server logs: $str") }

    companion object {
        private var idCounter = Int.MIN_VALUE
        fun newID(): Int {
            idCounter ++
            if (idCounter == 0) idCounter ++
            return idCounter
        }
    }
}