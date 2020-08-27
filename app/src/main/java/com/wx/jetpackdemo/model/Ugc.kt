package com.wx.jetpackdemo.model

import androidx.databinding.BaseObservable
import java.io.Serializable

data class Ugc(
    val commentCount: Int = 0,
    val hasFavorite: Boolean = false,
    val hasLiked: Boolean = false,
    val hasdiss: Boolean = false,
    val likeCount: Int = 0,
    val shareCount: Int = 0
) : BaseObservable(), Serializable