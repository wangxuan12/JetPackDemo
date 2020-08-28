package com.mooc.ppjoke.model

import java.io.Serializable

data class User(
    val avatar: String? = null,
    val commentCount: Int = 0,
    val description: String? = null,
    val expires_time: Long = 0,
    val favoriteCount: Int = 0,
    val feedCount: Int = 0,
    val followCount: Int = 0,
    val followerCount: Int = 0,
    val hasFollow: Boolean = false,
    val historyCount: Int = 0,
    val id: Int = 0,
    val likeCount: Int = 0,
    val name: String? = null,
    val qqOpenId: String? = null,
    val score: Int = 0,
    val topCommentCount: Int = 0,
    val userId: Long = 0
) : Serializable