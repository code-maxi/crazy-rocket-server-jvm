package server

import CreateNewGalaxyI
import GalaxyPasswordI
import JsonListI
import ResponseResult
import com.google.gson.Gson
import com.google.gson.JsonParseException
import io.ktor.application.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.http.*
import io.ktor.http.cio.websocket.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.websocket.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import server.Text.coloredLog
import server.data.*
import server.galaxy.GalaxyS
import server.user.UserS

object KtorServer {
    lateinit var gameCalculation: CoroutineScope
    lateinit var otherWorks: CoroutineScope
    fun create() {
        log("starting server...")
        embeddedServer(Netty, port = 1234) {
            install(WebSockets)
            routing {
                get("/") {

                }
                get("/get-galaxies") {
                    log("request to /create-galaxy/{galaxy}...")

                    val response = Gson().toJson(JsonListI(GalaxyS.getGalaxies()))
                    call.respondText(response, ContentType.Application.Json, HttpStatusCode.OK)
                }
                post("/create-galaxy") {
                    val res = try {
                        val json = call.receiveText()
                        log("request to /create-galaxy/{galaxy}: $json", Ansi.CYAN)
                        val parsed = Gson().fromJson(json, CreateNewGalaxyI::class.java)

                        GalaxyS.createGalaxy(parsed)

                        ResponseResult(true, message = "The Galaxy was successfully created!")
                    }
                    catch (ex: NullPointerException) { MissingParameters("URL-Parameter", "galaxy").printAndReponseResult() }
                    catch (ex: JsonParseException) { JsonParseEx(call.parameters["galaxy"]!!, "CreateNewGalaxyI").printAndReponseResult() }
                    catch (ex: OwnException) { ex.printAndReponseResult() }

                    call.respondText(Gson().toJson(res), ContentType.Application.Json)
                }
                post("/delete-galaxy") {
                    val res = try {
                        val param = call.receiveText()
                        val parsed = Gson().fromJson(param, GalaxyPasswordI::class.java)

                        GalaxyS.deleteGalaxy(parsed)

                        ResponseResult(true, message = "The Galaxy was successfully deleted!")
                    }
                    catch (ex: NullPointerException) { MissingParameters("URL-Parameter", "galaxy").printAndReponseResult() }
                    catch (ex: JsonParseException) { JsonParseEx(call.parameters["galaxy"]!!, "GalaxyPasswordI").printAndReponseResult() }
                    catch (ex: OwnException) { ex.printAndReponseResult() }

                    call.respondText(Gson().toJson(res), ContentType.Application.Json)
                }
                webSocket("/socket") {
                    val user = UserS(this)
                    try {
                        for (frame in incoming){
                            if (frame is Frame.Text) {
                                try {
                                    val message = parseSendFormat(frame.readText())
                                    user.onMessage(message)
                                }
                                catch (ex: WrongFormatEx) { ex.printError() }
                            }
                        }
                    } catch (e: ClosedReceiveChannelException) {
                        user.onClose()
                    } catch (e: Throwable) {
                        println("onError ${closeReason.await()}")
                        e.printStackTrace()
                    }
                }
            }
        }.start(true)
    }

    fun log(str: String, color: Ansi? = null) {
        coloredLog("Server", str, color)
    }
}