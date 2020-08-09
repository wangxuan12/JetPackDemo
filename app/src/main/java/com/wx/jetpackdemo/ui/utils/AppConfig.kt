package com.wx.jetpackdemo.ui.utils

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.wx.jetpackdemo.model.Destination
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.lang.StringBuilder

object AppConfig {

    private val sDestConfig : Map<String, Destination> by lazy {
        val content : String = parseFile("destination.json")
        Gson().fromJson(content, object : TypeToken<HashMap<String, Destination>>(){}.type) as HashMap<String, Destination>
    }

    fun getDestConfig(): Map<String, Destination> = sDestConfig

    private fun parseFile(fileName : String) : String {
        val assets = AppGlobals.getApplication().resources.assets
        val stream : InputStream = assets.open(fileName)
        val reader : BufferedReader = BufferedReader(InputStreamReader(stream))
        val sb : StringBuilder = StringBuilder()
        do {
            val line : String? = reader.readLine()
            line?.also {
                sb.append(it);
            }
        } while (line != null)
        return sb.toString()
    }
}