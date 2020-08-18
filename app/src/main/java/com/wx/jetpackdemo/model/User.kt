package com.wx.jetpackdemo.model

import java.io.Serializable

data class User(
    val avatar: String,
    val commentCount: Int,
    val description: String,
    val expires_time: Int,
    val favoriteCount: Int,
    val feedCount: Int,
    val followCount: Int,
    val followerCount: Int,
    val hasFollow: Boolean,
    val historyCount: Int,
    val id: Int,
    val likeCount: Int,
    val name: String,
    val qqOpenId: Any,
    val score: Int,
    val topCommentCount: Int,
    val userId: Long
) : Serializable