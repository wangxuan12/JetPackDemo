package com.mooc.ppjoke.model

import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import androidx.databinding.library.baseAdapters.BR
import java.io.Serializable

data class User(
    var avatar: String? = null,
    var commentCount: Int = 0,
    var description: String? = null,
    var expires_time: Long = 0,
    var favoriteCount: Int = 0,
    var feedCount: Int = 0,
    var followCount: Int = 0,
    var followerCount: Int = 0,
    private var hasFollow: Boolean = false,
    var historyCount: Int = 0,
    var id: Int = 0,
    var likeCount: Int = 0,
    var name: String? = null,
    var qqOpenId: String? = null,
    var score: Int = 0,
    var topCommentCount: Int = 0,
    var userId: Long = 0
) : BaseObservable(), Serializable {

    @Bindable
    fun getHasFollow(): Boolean {
        return hasFollow
    }

    fun setHasFollow(hasFollow: Boolean) {
        this.hasFollow = hasFollow
        notifyPropertyChanged(BR._all)
    }
}