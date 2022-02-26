package server

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import org.apache.http.impl.client.HttpClients
import server.galaxy.GalaxyS

fun main(args: Array<String>) {
    when (args[0]) {
        "server" -> {
            runBlocking {
                //GalaxyS.readGalaxyState()
                KtorServer.create()

                KtorServer.gameCalculation = CoroutineScope(Dispatchers.Main)

                coroutineScope {
                    KtorServer.otherWorks = this
                }
            }

            //SocketServer(1116)

            Runtime.getRuntime().addShutdownHook(object : Thread() {
                override fun run() {
                    println()
                    println("Program exited!")
                    //server.stop()
                }
            })
        }
        "post" -> {
            val httpClient = HttpClients.createDefault()
            val response = Network.makeHTTPPostRequest(
                "http://localhost:1234/${args[1]}",
                args[2], httpClient
            )
            println(response)
        }
    }
}