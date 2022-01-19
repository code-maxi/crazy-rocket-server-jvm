package server

import org.apache.http.impl.client.HttpClients
import server.galaxy.GalaxyS

fun main(args: Array<String>) {
    when (args[0]) {
        "server" -> {
            GalaxyS.readGalaxyState()

            val server = HTTPServer(1238)
            server.create()
        }
        "post" -> {
            val httpClient = HttpClients.createDefault()
            val response = Network.makeHTTPPostRequest(
                "http://localhost:1238/${args[1]}",
                args[2], httpClient
            )
            println(response)
        }
    }
}