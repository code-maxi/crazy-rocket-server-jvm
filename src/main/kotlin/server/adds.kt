package server

import JsonStatusI
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.sun.net.httpserver.HttpExchange
import server.galaxy.GalaxyS
import java.io.File
import java.nio.file.FileSystems

object FileA {
    fun createIf(parent: File, child: String, type: String): File {
        val file = File(parent, child)
        if (!file.exists() || (type == "d" && file.isFile) || (type == "f" && file.isDirectory) || file.name != child)
            file.createNewFile()
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
        response: (parsed: T, finish: (res: JsonStatusI) -> Unit) -> Unit
    ) {
        if (it.requestMethod === method) {
            val bytes = it.requestBody.readBytes()
            val message = bytes.toString(Charsets.UTF_8)

            try {
                val parsed = Gson().fromJson(message, classT)
                response(parsed) { re ->
                    val res = Gson().toJson(re)
                    it.sendResponseHeaders(200, res.toByteArray().size.toLong())
                    it.responseBody.write(res.toByteArray())
                    it.close()
                }
            }
            catch (ex: JsonSyntaxException) {
                val res = "wrong json format"
                it.sendResponseHeaders(400, res.toByteArray().size.toLong())
                it.responseBody.write(res.toByteArray())
                it.close()
            }
        }
    }
}