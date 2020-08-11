package com.wx.jetpackdemo.model

data class BottomBar(
    var activeColor: String,
    var inActiveColor: String,
    var selectTab: Int,
    var tabs: List<Tabs>
)

data class Tabs(
    var enable: Boolean,
    var index: Int,
    var pageUrl: String,
    var size: Int,
    var tintColor: String,
    var title: String
)