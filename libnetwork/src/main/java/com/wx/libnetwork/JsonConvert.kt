package com.wx.libnetwork

import com.google.gson.Gson
import com.google.gson.JsonObject
import java.lang.reflect.Type

class JsonConvert : Convert<Any> {
    override fun convert(response: String, type: Type): Any? {
        val jsonObject = Gson().fromJson<JsonObject>(response, JsonObject::class.java)
        val data = jsonObject.getAsJsonObject("data")
        data?.also {
            return Gson().fromJson(data.get("data").asString, type)
        }
        return null
    }

    override fun convert(response: String, clazz: Class<*>): Any? {
        val jsonObject = Gson().fromJson<JsonObject>(response, JsonObject::class.java)
        val data = jsonObject.getAsJsonObject("data")
        data?.also {
            return Gson().fromJson(data.get("data").asString, clazz)
        }
        return null
    }
}