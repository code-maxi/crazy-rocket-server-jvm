package server.adds.text

interface Logable {
    fun log(str: String, color: Ansi? = null)
}