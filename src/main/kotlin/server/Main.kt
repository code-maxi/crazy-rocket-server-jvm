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

            SocketServer(1116)

            Runtime.getRuntime().addShutdownHook(object : Thread() {
                override fun run() {
                    println()
                    println("Program exited! Stopping Server...")
                    server.stop()
                }
            })
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