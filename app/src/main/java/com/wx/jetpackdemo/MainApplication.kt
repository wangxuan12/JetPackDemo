package com.wx.jetpackdemo

import android.app.Application

class MainApplication : Application() {

    companion object {
        private lateinit var application : Application
        @JvmStatic
        fun getApplication(): Application = application
    }

    override fun onCreate() {
        super.onCreate()
        application = this
    }
}