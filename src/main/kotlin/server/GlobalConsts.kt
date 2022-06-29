package server

import com.google.gson.Gson
import com.google.gson.GsonBuilder

object GlobalConsts {
    val gson: Gson = GsonBuilder().create()
}