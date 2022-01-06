package server

import org.java_websocket.server.WebSocketServer
import java.net.ServerSocket
import kotlin.concurrent.thread

class Server(port: Int) {
    var serverSocket = ServerSocket(port)
    var running = true
    var loopThread = Thread {
        while (running) {
            val client = serverSocket.accept()
        }
    }
    fun listen() { loopThread.start() }
    fun stop() { running = false }
}