package com.mooc.ppjoke.utils

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.mooc.ppjoke.model.BottomBar
import com.mooc.ppjoke.model.Destination
import com.mooc.libcommon.global.AppGlobals
import com.mooc.ppjoke.model.SofaTab
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader

object AppConfig {

    private val sDestConfig : Map<String, Destination> by lazy {
        val content : String = parseFile("destination.json")
        Gson().fromJson(content, object : TypeToken<HashMap<String, Destination>>(){}.type) as HashMap<String, Destination>
    }

    private val sBottomBar : BottomBar by lazy {
        val content : String = parseFile("main_tabs_config.json")
        Gson().fromJson(content, BottomBar::class.java)
    }

    private val sSofaTab : SofaTab by lazy {
        val content : String = parseFile("sofa_tabs_config.json")
        val sofaTab = Gson().fromJson(content, SofaTab::class.java)
        sofaTab.tabs.sortedByDescending { it.index }
        sofaTab
    }

    fun getDestConfig(): Map<String, Destination> = sDestConfig

    fun getBottomBar() : BottomBar = sBottomBar

    fun getSofaTab() = sSofaTab

    private fun parseFile(fileName : String) : String {
        val assets = AppGlobals.getApplication().applicationContext.resources.assets
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