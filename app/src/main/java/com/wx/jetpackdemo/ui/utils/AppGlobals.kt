package com.wx.jetpackdemo.ui.utils

import android.annotation.SuppressLint
import android.app.Application
import java.lang.reflect.Method

@SuppressLint("PrivateApi")
object AppGlobals {
    val sApplication : Application by lazy {
        val method : Method = Class.forName("android.app.activityThread").getDeclaredMethod("currentApplication")
        method.invoke(null, null) as Application
    }

    fun getApplication() = sApplication
}