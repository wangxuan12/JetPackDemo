package com.wx.libnetwork

import java.lang.StringBuilder
import java.net.URLEncoder

object UrlCreator {

    fun createUrlFromParams(url: String, params: Map<String, Any>): String {
        val sb = StringBuilder()
        sb.append(url)
        if (url.indexOf('?') > 0 || url.indexOf('&') > 0) {
            sb.append('&')
        } else {
            sb.append('?')
        }
        for ((k, v) in params) {
            val value = URLEncoder.encode(v.toString(), "UTF-8")
            sb.append(k).append('=').append(value).append('&')
        }
        sb.deleteCharAt(sb.length - 1)
        return sb.toString()
    }
}