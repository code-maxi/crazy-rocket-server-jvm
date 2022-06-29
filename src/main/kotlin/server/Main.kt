package server

import CreateNewGalaxyI
import GameConfigI
import kotlinx.coroutines.*
import org.apache.http.impl.client.HttpClients
import server.adds.Network
import server.galaxy.GameContainer
import server.game.debug.GameDebugger
import tornadofx.launch

fun main(args: Array<String>) {
    when (args[0]) {
        "debugger" -> {
            launch<GameDebugger>()
            /*val goods1 = CrazyGoodsContainer(mapOf(
                CrazyGood.FOOD to 20.0,
                CrazyGood.FUEL to 130.0,
                CrazyGood.ROCKS to 220.0,
                CrazyGood.GOLD to 10.0,
            ))

            val goods2 = CrazyGoodsContainer(mapOf(
                CrazyGood.FOOD to 10.0,
                CrazyGood.FUEL to 20.0,
                CrazyGood.ROCKS to 10.0,
                CrazyGood.GOLD to 5.0,
            ))

            println("Goods 1: $goods1")
            println("Goods 2: $goods2")
            println("Goods 1 - Goods 2: ${goods1 - goods2}")
            println("Goods 1 + Goods 2: ${goods1 + goods2}")
            println("Goods 2 * 2: ${goods2 * 2.0}")
            println("-Goods 2: ${-goods2}")

            println(GsonBuilder().create().fromJson("{\"good\":\"ROCKS\"}", TestEnum::class.java))*/
        }
        "server" -> {
            runBlocking {
                //GalaxyS.readGalaxyState()
                GameContainer.create(CreateNewGalaxyI("test", "test-p", GameConfigI(1.0, 100.0, 2, 100, 100)))
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