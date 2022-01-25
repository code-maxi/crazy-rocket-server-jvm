package server

import org.apache.http.impl.client.HttpClients
import server.galaxy.GalaxyS
import kotlin.concurrent.thread

fun main(args: Array<String>) {
    when (args[0]) {
        "server" -> {
            GalaxyS.readGalaxyState()

            val httpServer = HTTPServer(1115)
            httpServer.create()

            SocketServer(1112)
        }
        "post" -> {
            val httpClient = HttpClients.createDefault()
            val response = Network.makeHTTPPostRequest(
                "http://localhost:1112/${args[1]}",
                args[2], httpClient
            )
            println(response)
        }
    }
}