package com.mooc.libnetwork

import com.google.gson.Gson
import com.google.gson.JsonObject
import java.lang.reflect.Type

class JsonConvert<T> : Convert<T> {
    override fun convert(response: String, type: Type): T? {
        val jsonObject = Gson().fromJson<JsonObject>(response, JsonObject::class.java)
        val data = jsonObject.getAsJsonObject("data")
        data?.also {
            return Gson().fromJson(data.get("data"), type)
        }
        return null
    }

    override fun convert(response: String, clazz: Class<*>): T? {
//        val jsonObject = Gson().fromJson<JsonObject>(response, JsonObject::class.java)
//        val data = jsonObject.getAsJsonObject("data")
//        data?.also {
//            return Gson().fromJson(data.get("data").asString, clazz)
//        }
        return null
    }
}