package server

import CreateNewGalaxyI
import GalaxyConfigI
import kotlinx.coroutines.*
import org.apache.http.impl.client.HttpClients
import server.adds.Network
import server.adds.math.geom.tests.CircleRicochetOnLineTest
import server.adds.math.geom.tests.LineCircleCollisionTest
import server.galaxy.GalaxyS
import tornadofx.launch

fun main(args: Array<String>) {
    when (args[0]) {
        "debugger" -> {
            launch<CircleRicochetOnLineTest>()
        }
        "server" -> {
            runBlocking {
                //GalaxyS.readGalaxyState()
                GalaxyS.createGalaxy(CreateNewGalaxyI("test", "test-p", GalaxyConfigI(1.0, 100.0, 2, 100, 100)))
                KtorServer.create()
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