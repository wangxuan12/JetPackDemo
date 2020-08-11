package com.wx.jetpackdemo.ui.utils

import android.annotation.SuppressLint
import android.app.Application
import com.wx.jetpackdemo.MainApplication
import java.lang.reflect.Method

@SuppressLint("PrivateApi", "DiscouragedPrivateApi")
object AppGlobals {
//    private val sApplication : Application by lazy {
//        val method : Method = Class.forName("android.app.ActivityThread").getDeclaredMethod("currentApplication")
//        method.invoke(null, Any()) as Application
//    }

    fun getApplication() : Application = MainApplication.getApplication()
}