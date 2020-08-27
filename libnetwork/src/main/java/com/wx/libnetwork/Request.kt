@file:Suppress("UNCHECKED_CAST")

package com.wx.libnetwork

import android.annotation.SuppressLint
import android.text.TextUtils
import android.util.Log
import androidx.annotation.IntDef
import androidx.arch.core.executor.ArchTaskExecutor
import com.wx.libnetwork.cache.CacheManager
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException
import java.io.Serializable
import java.lang.Exception
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

abstract class Request<T, R : Request<T, R>>(var url : String) : Cloneable {
    private var headers = mutableMapOf<String, String>()
    protected var params = mutableMapOf<String, Any>()
    private var cacheKey : String? = null
    private var type: Type? = null
    private var clazz : Class<*>? = null
    private var cacheStrategy : Int = CACHE_FIRST

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

    fun addHeader(key : String, value : String) : R {
        headers[key] = value
        return this as R
    }

    fun addParam(key : String, value : Any?) : R {
        if (value == null) return this as R
        try {
            if (value is String) {
                params[key] = value
            } else {
                val field = value.javaClass.getField("TYPE")
                val clazz : Class<*> = field.get(null) as Class<*>
                if (clazz.isPrimitive) {
                    params[key] = value
                }
            }
        } catch (e : NoSuchFieldException) {
            e.printStackTrace()
        }
        return this as R
    }

    fun cacheKey(key : String) : R {
        this.cacheKey = key
        return key as R
    }

    @SuppressLint("RestrictedApi")
    fun execute(callback : JsonCallback<T>) {
        if (cacheStrategy != NET_ONLY) {
            ArchTaskExecutor.getIOThreadExecutor().execute {
                val response : ApiResponse<T> = readCache()
                callback.onCacheSuccess(response)
            }
        }
        if (cacheStrategy != CACHE_ONLY) {
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
    }

    fun execute() : ApiResponse<T> {
        if (cacheStrategy == CACHE_ONLY) {
            return readCache()
        }
        val response = getCall().execute()
        return parseResponse(response, null)
    }

    private fun readCache(): ApiResponse<T> {
        val key = if (TextUtils.isEmpty(cacheKey)) generateCacheKey() else cacheKey ?: ""
        val cache = CacheManager.getCache(key)
        val result = ApiResponse<T>()
        cache?.also {
            result.status = 304
            result.success = true
            result.body = it as T
            result.message = "缓存获取成功"
        }
        return result
    }

    private fun parseResponse(response: Response, callback: JsonCallback<T>?): ApiResponse<T> {
        var message : String? = null
        val status = response.code
        val success = response.isSuccessful
        val result = ApiResponse<T>()
        val convert = ApiService.convert ?:JsonConvert<T>()
        val content = response.body?.string()
        if (success && content != null) {
            when {
                callback != null -> {
                    val type: ParameterizedType =
                        callback.javaClass.genericSuperclass as ParameterizedType
                    val argument = type.actualTypeArguments[0]
                    result.body = convert.convert(content, argument) as T?
                }
                type != null -> {
                    result.body = convert.convert(content, type!!) as T?
                }
                //                else if (clazz != null) {
                //                    clazz?.also { result.body = convert?.convert(content, it) as T? }
                //                }
                else -> {
                    Log.e("request", "parseResponse: 无法解析")
                }
            }
        } else {
            message = content
        }
//        try {
//
//        } catch (e: Exception) {
//            message = e.message
//            success = false
//        }
        result.success = success
        result.status = status
        result.message = message

        if (cacheStrategy != NET_ONLY && result.success && result.body != null && result.body is Serializable) {
            saveCache(result.body as T)
        }
        return result
    }

    private fun saveCache(body: T) {
        val key = if (TextUtils.isEmpty(cacheKey)) generateCacheKey() else cacheKey ?: ""
        CacheManager.save(key, body)
    }

    private fun generateCacheKey(): String {
        cacheKey = UrlCreator.createUrlFromParams(url, params)
        return cacheKey ?: ""
    }

    fun cacheStrategy(@CacheStrategy cacheStrategy: Int) : R {
        this.cacheStrategy = cacheStrategy
        return this as R
    }

    fun responseType(type : Type) : R {
        this.type = type
        return this as R
    }

    fun responseType(clazz : Class<*>) : R {
        this.clazz = clazz
        return this as R
    }

    private fun getCall() : Call {
        val builder = okhttp3.Request.Builder()
        addHeaders(builder)
        val request = generateRequest(builder)
        return ApiService.okHttpClient.newCall(request)
    }

    abstract fun generateRequest(builder: okhttp3.Request.Builder): okhttp3.Request

    private fun addHeaders(builder: okhttp3.Request.Builder) {
        for ((k, v) in headers) {
            builder.addHeader(k, v)
        }
    }

    public override fun clone(): Request<T, R> {
        return super.clone() as Request<T, R>
    }
}