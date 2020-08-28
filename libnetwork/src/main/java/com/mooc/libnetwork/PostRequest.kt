package com.mooc.libnetwork

import okhttp3.FormBody

class PostRequest<T>(url : String) : Request<T, PostRequest<T>>(url) {
    override fun generateRequest(builder: okhttp3.Request.Builder): okhttp3.Request {
        val bodyBuilder = FormBody.Builder()
        for ((k, v) in params) {
            bodyBuilder.add(k, v.toString())
        }
        return builder.get().url(url).post(bodyBuilder.build()).build()
    }
}