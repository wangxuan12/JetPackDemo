package com.wx.jetpackdemo.model

data class Comment(
    val author: User,
    val commentCount: Int,
    val commentId: Long,
    val commentText: String,
    val commentType: Int,
    val createTime: Int,
    val hasLiked: Boolean,
    val height: Int,
    val id: Int,
    val imageUrl: String,
    val itemId: Long,
    val likeCount: Int,
    val ugc: Ugc,
    val userId: Int,
    val videoUrl: String,
    val width: Int
)