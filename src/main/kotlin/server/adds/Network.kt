package server.adds

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
import java.io.InputStreamReader

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