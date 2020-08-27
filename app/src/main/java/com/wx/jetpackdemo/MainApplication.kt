package com.wx.jetpackdemo

import android.app.Application
import com.facebook.stetho.Stetho
import com.wx.libnetwork.ApiService

class MainApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        ApiService.init<Any>("http://123.56.232.18:8080/serverdemo", null)

        initStetho()
    }

    private fun initStetho() {
        Stetho.initialize(
            Stetho.newInitializerBuilder(this)
                .enableDumpapp(Stetho.defaultDumperPluginsProvider(this))
                .enableWebKitInspector(Stetho.defaultInspectorModulesProvider(this))
                .build()
        )
    }
}