package com.wx.jetpackdemo.ui.publish

import android.os.Bundle
import android.os.PersistableBundle
import androidx.appcompat.app.AppCompatActivity
import com.wx.jetpackdemo.R
import com.wx.libnavannotation.ActivityDestination

@ActivityDestination(pageUrl = "main/tabs/publish")
class CaptureActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
        setContentView(R.layout.fragment_home)
    }
}