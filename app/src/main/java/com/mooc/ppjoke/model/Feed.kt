package com.mooc.ppjoke.model

import androidx.databinding.BaseObservable
import java.io.Serializable

data class Feed constructor(
    val activityIcon: String? = null,
    val activityText: String? = null,
    val authorId: Long = 0,
    val cover: String? = null,
    val createTime: Long = 0,
    val duration: Double = 0.0,
    val feeds_text: String? = null,
    val height: Int = 0,
    val id: Int = 0,
    val itemId: Long = 0,
    val itemType: Int = 0,
    val url: String? = null,
    val width: Int = 0,
    val author : User? = null,
    val topComment: Comment? = null,
    val ugc: Ugc? = null
) : BaseObservable(), Serializable {
    companion object {
        const val TYPE_IMAGE = 1
        const val TYPE_VIDEO = 1
    }
}