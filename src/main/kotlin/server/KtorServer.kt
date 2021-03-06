package server

import CreateNewGameI
import GamePasswordI
import server.data_containers.JsonListI
import server.data_containers.ResponseResult
import com.google.gson.Gson
import io.ktor.application.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.http.*
import io.ktor.http.cio.websocket.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import server.adds.text.Ansi
import server.adds.Error
import server.adds.text.Text.formattedPrint
import server.data_containers.*
import server.galaxy.GameContainer
import server.user.UserS

object KtorServer {
    fun create() {
        log("starting server...")
        embeddedServer(Netty, port = 1234) {
            install(WebSockets)
            routing {
                get("/") {

                }

                get("/get-galaxies") {
                    log("request to /create-galaxy/{galaxy}...")

                    val response = Gson().toJson(JsonListI(GameContainer.getGameContainers()))
                    call.respondText(response, ContentType.Application.Json, HttpStatusCode.OK)
                }

                post("/create-galaxy") {
                    val res = Error.resultCatch("create-galaxy-http-response", {
                        val json = call.receiveText()
                        log("request to /create-galaxy/{galaxy}: $json", Ansi.CYAN)
                        val parsed = Gson().fromJson(json, CreateNewGameI::class.java)

                        GameContainer.create(parsed)

                        ResponseResult(true, message = "The Galaxy was successfully created!")
                    }, mapOf(
                        "null-pointer" to MissingParameters("URL-Parameter", "galaxy"),
                        "json-syntax" to JsonParseEx(call.parameters["galaxy"]!!, "CreateNewGalaxyI")
                    ), true)

                    call.respondText(Gson().toJson(res), ContentType.Application.Json)
                }

                post("/delete-galaxy") {
                    val res = Error.resultCatch("create-galaxy-http-response", {
                        val param = call.receiveText()
                        val parsed = Gson().fromJson(param, GamePasswordI::class.java)

                        GameContainer.delete(parsed)

                        ResponseResult(true, message = "The Galaxy was successfully deleted!")
                    }, mapOf(
                        "null-pointer" to MissingParameters("URL-Parameter", "galaxy"),
                        "json-syntax" to JsonParseEx(call.parameters["galaxy"]!!, "GalaxyPasswordI")
                    ), true)

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
                        log("Websocket-Error: User closed!")
                        user.onClose()
                        e.printStackTrace()
                    }
                }
            }
        }.start(true)
    }

    fun log(str: String, color: Ansi? = null) {
        formattedPrint("Server", str, color)
    }
}