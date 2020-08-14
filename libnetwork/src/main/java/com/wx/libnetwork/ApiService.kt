package com.wx.libnetwork

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.*

object ApiService {
    internal var okHttpClient : OkHttpClient
    internal lateinit var baseUrl : String
    internal lateinit var convert: Convert<*>


    init {
        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BODY

        okHttpClient = OkHttpClient.Builder()
            .readTimeout(5, TimeUnit.SECONDS)
            .writeTimeout(5, TimeUnit.SECONDS)
            .connectTimeout(5, TimeUnit.SECONDS)
            .addInterceptor(interceptor)
            .build()

        val trustManagers : Array<TrustManager> = arrayOf(object : X509TrustManager {
            override fun checkClientTrusted(p0: Array<out X509Certificate>?, p1: String?) {
                TODO("Not yet implemented")
            }

            override fun checkServerTrusted(p0: Array<out X509Certificate>?, p1: String?) {
                TODO("Not yet implemented")
            }

            override fun getAcceptedIssuers(): Array<X509Certificate> {
                return arrayOf()
            }
        })
        val ssl = SSLContext.getInstance("SSL")
        ssl.init(null, trustManagers, SecureRandom())
        HttpsURLConnection.setDefaultSSLSocketFactory(ssl.socketFactory)
        HttpsURLConnection.setDefaultHostnameVerifier { _, _ -> true }
    }

    fun init(baseUrl : String, convert: Convert<*>) {
        this.baseUrl = baseUrl
        this.convert = convert
    }

}