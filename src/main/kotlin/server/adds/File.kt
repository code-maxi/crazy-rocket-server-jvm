package server.adds

import java.io.File
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