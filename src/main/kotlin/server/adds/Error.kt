package server.adds

import server.data_containers.ResponseResult
import com.google.gson.JsonSyntaxException
import server.data_containers.OwnException

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