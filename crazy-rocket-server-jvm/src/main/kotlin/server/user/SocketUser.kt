package server.user

import SendFormat
import com.google.gson.Gson
import java.io.InputStream
import java.io.OutputStream
import java.net.Socket
import java.security.MessageDigest
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern
import kotlin.concurrent.thread
import kotlin.experimental.xor


abstract class SocketUser(private val socket: Socket) {
    private var alive = true
    protected var quiet = true

    private val writer: OutputStream = socket.getOutputStream()

    init { connect() }

    fun kill() { alive = false }

    private fun handshaking() {
        println("Client Handshaking...")
        val inp: InputStream = socket.getInputStream()
        val reader = Scanner(inp, "UTF-8")

        val data = reader.useDelimiter("\\r\\n\\r\\n").next()
        log("Data: $data")
        val get: Matcher = Pattern.compile("^GET").matcher(data)

        if (get.find()) {
            val match = Pattern.compile("Sec-WebSocket-Key: (.*)").matcher(data)
            match.find()

            /*val response = ("""HTTP/1.1 101 Switching Protocols
Connection: Upgrade
Upgrade: websocket
Sec-WebSocket-Accept: ${
                Base64.getEncoder().encodeToString(
                    MessageDigest.getInstance("SHA-1").digest(
                        (match.group(1).toString() + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11").toByteArray(
                            charset("UTF-8")
                        )
                    )
                )
            }
""").toByteArray()*/
            val response = (
                "HTTP/1.1 101 Switching Protocols\r\n"
                + "Connection: Upgrade\r\n"
                + "Upgrade: websocket\r\n"
                + "Sec-WebSocket-Accept: "
                + Base64.getEncoder().encodeToString(MessageDigest.getInstance("SHA-1").digest((match.group(1) + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11").toByteArray()))
                + "\r\n\r\n").toByteArray()
            writer.write(response, 0, response.size)
        }
    }

    private fun connect() {
        thread {
            handshaking()
            onConnection()
            listen()
        }
    }

    private fun listen() {
        log("Listening...")

        val inp: InputStream = socket.getInputStream()
        val reader = Scanner(inp, "UTF-8")

        while (alive) {
            val encodedData = socket.getInputStream().readAllBytes()
            val decodedData = decodeMessage(encodedData)
            val data = decodedData.toString(Charsets.UTF_8)

            if (!quiet) log("new message: $data")
            try {
                val inpp = Gson().fromJson(data, SendFormat::class.java)
                onMessage(inpp)
            } catch (e: Exception) {
                println("Can't Read Object $data. It's not a SendFormat.")
            }
        }
        onKill()
        socket.close()
    }

    open fun onConnection() {}
    open fun onKill() {}

    abstract fun onMessage(a: SendFormat)
    abstract fun log(str: String)

    fun sendMessage(a: SendFormat) {
        val str = Gson().toJson(a)
        val data = str.toByteArray()
        writer.write(data, 0, data.size)
        if (!quiet) log("Sent: $str")
    }

    companion object {
        fun decodeMessage(message: ByteArray): ByteArray {
            val key = message.copyOfRange(0, 6)
            val encoded = message.copyOfRange(6, message.size)
            val decoded = ByteArray(encoded.size)
            for (i in encoded.indices) decoded[i] = (encoded[i] xor key[i and 0x3])
            return decoded
        }
    }
}