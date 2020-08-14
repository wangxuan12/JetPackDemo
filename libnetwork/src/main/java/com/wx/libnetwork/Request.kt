package com.wx.libnetwork

import androidx.annotation.IntDef
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException
import java.lang.Exception
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

abstract class Request<T, R : Request<T, R>>(var url : String) {
    protected var headers = mutableMapOf<String, String>()
    protected var params = mutableMapOf<String, Any>()
    private var cacheKey : String? = null
    private var type: Type? = null
    private var clazz : Class<*>? = null

    companion object {
        //仅仅只访问本地缓存，即便本地缓存不存在，也不会发起网络请求
        const val CACHE_ONLY = 1
        //先访问缓存，同时发起网络的请求，成功后缓存到本地
        const val CACHE_FIRST = 2
        //仅仅只访问服务器，不存任何存储
        const val NET_ONLY = 3
        //先访问网络，成功后缓存到本地
        const val NET_CACHE = 4
    }

    @IntDef(CACHE_ONLY, CACHE_FIRST, NET_ONLY, NET_CACHE)
    annotation class CacheStrategy

    @Suppress("UNCHECKED_CAST")
    fun addHeader(key : String, value : String) : R {
        headers[key] = value
        return this as R
    }

    @Suppress("UNCHECKED_CAST")
    fun addParam(key : String, value : Any) : R {
        val field = value.javaClass.getField("TYPE")
        val clazz : Class<*> = field.get(null) as Class<*>
        if (clazz.isPrimitive) {
            params[key] = value
        }
        return this as R
    }

    @Suppress("UNCHECKED_CAST")
    fun cacheKey(key : String) : R {
        this.cacheKey = key
        return key as R
    }

    fun execute(callback : JsonCallback<T>) {
        getCall().enqueue(object : Callback{
            override fun onFailure(call: Call, e: IOException) {
                val response = ApiResponse<T>(message = e.message ?: "")
                callback.onError(response)
            }

            override fun onResponse(call: Call, response: Response) {
                val apiResponse = parseResponse(response, callback)
                if (apiResponse.success) {
                    callback.onSuccess(apiResponse)
                } else {
                    callback.onError(apiResponse)
                }
            }
        })
    }

    fun execute() : ApiResponse<T> {
        val response = getCall().execute()
        return parseResponse(response, null)
    }

    @Suppress("UNCHECKED_CAST")
    private fun parseResponse(response: Response, callback: JsonCallback<T>?): ApiResponse<T> {
        var message : String = ""
        val status = response.code
        var success = response.isSuccessful
        val result = ApiResponse<T>()
        val convert = ApiService.convert
        try {
            val content = response.body.toString()
            if (success) {
                if (callback != null) {
                    val type: ParameterizedType =
                        callback.javaClass.genericSuperclass as ParameterizedType
                    val argument = type.actualTypeArguments[0]
                    result.body = convert.convert(content, argument) as T
                } else if (type != null) {
                    type?.also { result.body = convert.convert(content, it) as T }
                } else if (clazz != null) {
                    clazz?.also { result.body = convert.convert(content, it) as T }
                } else {
//                    android.util.Log.e("request", "parseResponse: 无法解析" )
                }
            } else {
                message = content
            }
        } catch (e: Exception) {
            message = e.message ?: ""
            success = false
        }
        result.success = success
        result.status = status
        result.message = message
        return result
    }

    @Suppress("UNCHECKED_CAST")
    fun responseType(type : Type) : R {
        this.type = type
        return this as R
    }

    @Suppress("UNCHECKED_CAST")
    fun responseType(clazz : Class<*>) : R {
        this.clazz = clazz
        return this as R
    }

    fun getCall() : Call {
        val builder = okhttp3.Request.Builder()
        addHeaders(builder)
        val request = generateRequest(builder)
        val call = ApiService.okHttpClient.newCall(request)
        return call
    }

    abstract fun generateRequest(builder: okhttp3.Request.Builder): okhttp3.Request

    private fun addHeaders(builder: okhttp3.Request.Builder) {
        for ((k, v) in headers) {
            builder.addHeader(k, v)
        }
    }
}