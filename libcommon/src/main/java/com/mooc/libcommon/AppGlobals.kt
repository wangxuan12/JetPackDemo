package com.mooc.libcommon

import android.annotation.SuppressLint
import android.app.Application
import java.lang.reflect.Method

@SuppressLint("DiscouragedPrivateApi", "PrivateApi")
object AppGlobals {
    private val sApplication : Application by lazy {
        val method : Method = Class.forName("android.app.ActivityThread").getDeclaredMethod("currentApplication")
        method.invoke(null) as Application
    }

    fun getApplication() : Application = sApplication
}