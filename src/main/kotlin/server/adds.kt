package server

import JsonStatusI
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
import server.data.inRange
import java.io.File
import java.io.InputStreamReader
import java.nio.file.FileSystems


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
        method: String = "POST",
        response: (parsed: T, finish: (res: JsonStatusI, code: Int) -> Unit) -> Unit
    ) {
        if (it.requestMethod == method) {
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
                val entity: HttpEntity = it.getEntity()
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
    fun validNameText(text: String, errorType: String, minLetters: Int, maxLetters: Int): Boolean {
        if (text.length <= 3) throw InvalidTextEx(errorType, text, "it's too short.")
        else if (text.length <= 7) throw InvalidTextEx(errorType, text, "it's too long.")
        else if (!text.matches("\\w+".toRegex())) throw InvalidTextEx(errorType, text, "it's too short.")
        else return true
    }
}