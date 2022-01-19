package server

import CreateNewGalaxyI
import GalaxyPasswordI
import GalaxyPropsI
import JsonListI
import JsonStatusI
import com.google.gson.Gson
import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import server.galaxy.GalaxyS
import java.net.InetSocketAddress

class HTTPServer(val port: Int) {
    private val responseWebSite = { ex: HttpExchange ->
        println("Get WebSite")

        val response = "This is the response at ${ex.requestURI}"
        ex.sendResponseHeaders(200, response.toByteArray().size.toLong())
        val os = ex.responseBody
        os.write(response.toByteArray())
        os.close()
    }

    private val server: HttpServer = HttpServer.create(InetSocketAddress(port), 0)

    private val contexts = arrayListOf<String>()

    fun create() {
        println("Creating Http-Server at localhost:$port.")
        server.createContext("/", responseWebSite)
        server.createContext("/get-galaxies") {
            if (it.requestMethod == "GET") {
                val response = Gson().toJson(JsonListI(GalaxyS.getGalaxies()))
                it.sendResponseHeaders(200, response.toByteArray().size.toLong())
                it.responseBody.write(response.toByteArray())
                it.close()
            }
        }
        server.createContext("/create-galaxy") {
            println("request to /create-galaxy ${it.requestMethod}")
            Network.handlePostRequest(
                it, CreateNewGalaxyI::class.java
            ) { parsed, finish ->
                println("/create-galaxy parsed: $parsed")
                val status = GalaxyS.addGalaxy(parsed)
                println("Status: $status")
                val res = JsonStatusI(status)
                println("/create-galaxy res: $res")
                finish(res, 200)
            }
        }
        server.createContext("/delete-galaxy") {
            println("request to /delete-galaxy ${it.requestMethod}")
            Network.handlePostRequest(
                it, GalaxyPasswordI::class.java, "DELETE"
            ) { parsed, finish ->
                println("/delete-galaxy parsed: $parsed")
                val res = JsonStatusI(GalaxyS.removeGalaxy(parsed))
                println("/delete-galaxy res: $res")
                finish(res, 200)
            }
        }
        server.start()
    }

    fun updateContexts(list: ArrayList<GalaxyPropsI>) {
        println("Contexts: " + contexts.joinToString())
        println("List: " + list.joinToString())

        val compare = ArrayA.compare(
            list.map { "/galaxy/${it.name}" },
            contexts
        )
        compare.removed.forEach { server.removeContext(it); contexts.remove(it) }
        compare.added.forEach { server.createContext(it, responseWebSite); contexts.add(it) }
    }
}