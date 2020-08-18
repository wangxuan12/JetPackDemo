package com.wx.jetpackdemo.model

data class Feed(
    val activityIcon: String,
    val activityText: String,
    val authorId: Long,
    val cover: String,
    val createTime: Int,
    val duration: Double,
    val feeds_text: String,
    val height: Int,
    val id: Int,
    val itemId: Long,
    val itemType: Int,
    val url: String,
    val width: Int,
    val author : User,
    val topComment: Comment,
    val ugc: Ugc
)