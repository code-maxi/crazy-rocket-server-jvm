package server

import SendFormat
import com.google.gson.Gson
import org.java_websocket.WebSocket
import org.java_websocket.handshake.ClientHandshake
import org.java_websocket.server.WebSocketServer
import server.user.UserS
import java.net.InetSocketAddress
import javax.websocket.OnClose
import javax.websocket.OnMessage
import javax.websocket.OnOpen
import javax.websocket.Session
import javax.websocket.server.ServerEndpoint
import kotlin.concurrent.thread

//lateinit var server: SocketServer

/*class SocketServer(port: Int) : WebSocketServer(InetSocketAddress(port)), Logable {
    init {
        println("Initializing SocketServer on port ${port}.")
        start()
        server = this
    }

    override fun onOpen(conn: WebSocket?, handshake: ClientHandshake?) {
        if (conn != null) {
            log("New User Recieved...")
            UserS(conn, newID())
        }
    }

    override fun onStart() {
        log("Starting...")
    }
    override fun onClose(conn: WebSocket?, code: Int, reason: String?, remote: Boolean) { UserS.findUser(conn!!).onClose() }
    override fun onError(conn: WebSocket?, ex: Exception?) { UserS.findUser(conn!!).onError(ex!!) }
    override fun onMessage(conn: WebSocket?, message: String?) {
        try {
            val parse = Gson().fromJson(message!!, SendFormat::class.java)
            UserS.findUser(conn!!).onMessage(parse)
        }
        catch (ex: com.google.gson.JsonSyntaxException) { log("Couldn't parse $message", LogType.ERROR) }
        catch (ex: NullPointerException) { ex.printStackTrace() }
    }

    override fun log(str: String, type: LogType) { coloredLog("SocketServer: ", str, type) }

    companion object {
        private var idCounter = Int.MIN_VALUE
        fun newID(): String {
            idCounter ++
            if (idCounter == 0) idCounter ++
            return idCounter.toString()
        }
    }
}*/

/*@ServerEndpoint("/")
class SocketServer(port: Int) : Logable {


    override fun log(str: String, type: LogType) { coloredLog("SocketServer: ", str, type) }

    companion object {
        private var idCounter = Int.MIN_VALUE
        fun newID(): String {
            idCounter ++
            if (idCounter == 0) idCounter ++
            return idCounter.toString()
        }
    }
}*/

