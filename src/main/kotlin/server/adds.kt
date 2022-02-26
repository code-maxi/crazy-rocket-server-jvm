package server

import JsonStatusI
import ResponseResult
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.sun.net.httpserver.HttpExchange
import org.apache.http.HttpEntity
import org.apache.http.client.ClientProtocolException
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.util.EntityUtils
import server.data.InvalidTextEx
import server.data.OwnException
import server.data.inRange
import java.io.File
import java.io.InputStreamReader
import java.nio.file.FileSystems

// Hallo, eine neue Nachricht!


object FileA {
    fun createIf(
        parent: File,
        child: String,
        type: String,
        onCreate: (f: File) -> Unit = {}
    ): File {
        val file = File(parent, child)
        if (type == "f") { if (file.createNewFile()) onCreate(file) }
        else { if (file.mkdir()) onCreate(file) }
        return file
    }
    fun file(vararg s: String) = File(s.joinToString(FileSystems.getDefault().separator))
}

class ArrayCompareD<T>(
    val added: Collection<T>,
    val removed: Collection<T>,
    val stayed: Collection<T>
)

object ArrayA {
    fun <T>compare(
        a: Collection<T>,
        b: Collection<T>,
        equal: (a: T, b: T) -> Boolean = { ai,bi -> ai == bi }
    ): ArrayCompareD<T> {
        val added = a.filterNot { ai -> b.any { bi -> equal(ai, bi) } }
        val removed = b.filterNot { bi -> b.any { ai -> equal(ai, bi) } }
        val stayed = a.filter { ai -> b.any { bi -> equal(ai, bi) } }
        return ArrayCompareD(added, removed, stayed)
    }
}

object Network {
    fun <T> handlePostRequest(
        it: HttpExchange,
        classT: Class<T>,
        method: String? = null,
        response: (parsed: T, finish: (res: JsonStatusI, code: Int) -> Unit) -> Unit
    ) {
        if (method == null || it.requestMethod == method) {
            val bytes = InputStreamReader(it.requestBody)
            val message = bytes.readText()

            try {
                val parsed = Gson().fromJson(message, classT)
                response(parsed) { re, code ->
                    val res = Gson().toJson(re)
                    it.sendResponseHeaders(code, res.toByteArray().size.toLong())
                    it.responseBody.write(res.toByteArray())
                    it.responseBody.flush()
                    it.responseBody.close()
                }
            }
            catch (ex: JsonSyntaxException) {
                val res = "wrong json format"
                println("ERROR: $res")
                it.sendResponseHeaders(400, res.toByteArray().size.toLong())
                it.responseBody.write(res.toByteArray())
                it.responseBody.flush()
                it.responseBody.close()
            }
        }
    }
    fun makeHTTPPostRequest(
        url: String,
        json: String,
        httpclient: CloseableHttpClient
    ): String {
        val httpPost = HttpPost(url)
        httpPost.setHeader("Accept", "application/json")
        httpPost.setHeader("Content-type", "application/json")

        val stringEntity = StringEntity(json)
        httpPost.entity = stringEntity

        println("Executing request " + httpPost.requestLine)

        val responseBody = httpclient.execute(httpPost) {
            val status: Int = it.statusLine.statusCode

            if (status in 200..299) {
                val entity: HttpEntity = it.entity
                (EntityUtils.toString(entity))!!
            } else {
                throw ClientProtocolException("Unexpected response status: $status")
            }
        }

        return responseBody
    }
}

object Game {
    fun effectStep(
        value: Double,
        target: Double,
        speed: Double,
        linear: Boolean = true,
        range: Double? = null
    ): Double {
        return if (inRange(value, target, range ?: speed)) target
        else if (value > target && linear) value - speed
        else if (value < target && linear) value + speed
        else value + (target - value) * speed
    }
}

object Text {
    private const val LOG_HEADER_WIDTH = 18
    fun checkValidName(text: String, errorType: String, minLetters: Int, maxLetters: Int) {
        if (text.length <= minLetters) throw InvalidTextEx(errorType, text, "it's too short.")
        else if (text.length <= maxLetters) throw InvalidTextEx(errorType, text, "it's too long.")
        else if (!text.matches("\\w+".toRegex())) throw InvalidTextEx(errorType, text, "it's too short.")
    }
    fun coloredLog(
        from: String,
        str: String,
        color: Ansi? = null,
        name: Ansi? = null,
        maxSize: Int = 18
    ) {
        if (str != "") println("${Ansi.BOLD.color}${name?.color ?: Ansi.YELLOW.color}${sizeString(from, maxSize)} ${Ansi.RESET.color} ${if (color != null) "${color.color}$str${Ansi.RESET.color}" else str}")
        else println()
    }
    fun maxSizeString(str: String, maxSize: Int) =
        if (str.length > maxSize) "${str.substring(0, maxSize - 3)}..."
        else str
    private fun sizeString(str: String, size: Int) =
        maxSizeString(str, size).let { "$it${Array(size - it.length) { " " }.joinToString("")}" }
}

object Error {
    suspend fun resultCatch(
        header: String,
        callback: suspend () -> ResponseResult,
        onError: Map<String, OwnException> = mapOf(),
        print: Boolean = false
    ): ResponseResult =
        try { val call = callback(); call.copy(header = header) }
        catch (ex: OwnException) { ex.responseResult(header, print) }
        catch (ex: NullPointerException) { onError["null-pointer"]!!.responseResult(header, print) }
        catch (ex: JsonSyntaxException) { onError["json-syntax"]!!.responseResult(header, print) }
        catch (ex: ClassCastException) { onError["class-cast"]!!.responseResult(header, print) }
}

enum class Ansi(val color: String) {
    RESET("\u001b[0m"),
    BLACK("\u001b[30m"),
    RED("\u001b[31m"),
    GREEN("\u001b[32m"),
    YELLOW("\u001b[33m"),
    BLUE("\u001b[34m"),
    PURPLE("\u001b[35m"),
    CYAN("\u001b[36m"),
    WHITE("\u001b[37m"),
    BOLD("\u001b[1m")
}

interface Logable {
    fun log(str: String, color: Ansi? = null)
}