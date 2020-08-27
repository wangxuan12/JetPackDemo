package com.wx.jetpackdemo.model

import java.io.Serializable

data class Comment(
    val author: User? = null,
    val commentCount: Int = 0,
    val commentId: Long = 0,
    val commentText: String? = null,
    val commentType: Int = 0,
    val createTime: Long = 0,
    val hasLiked: Boolean = false,
    val height: Int  = 0,
    val id: Int = 0,
    val imageUrl: String? = null,
    val itemId: Long = 0,
    val likeCount: Int = 0,
    val ugc: Ugc? = null,
    val userId: Long = 0,
    val videoUrl: String? = null,
    val width: Int = 0
) : Serializable