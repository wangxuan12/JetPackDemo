package com.wx.jetpackdemo.model

data class Destination(
    val asStarter: Boolean,
    val clazzName: String,
    val id: Int,
    val isFragment: Boolean,
    val needLogin: Boolean,
    val pageUrl: String
)