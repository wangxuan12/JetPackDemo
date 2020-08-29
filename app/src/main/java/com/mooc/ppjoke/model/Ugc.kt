package com.mooc.ppjoke.model

import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import androidx.databinding.library.baseAdapters.BR
import java.io.Serializable

data class Ugc(
    var commentCount: Int = 0,
    val hasFavorite: Boolean = false,
    private var hasLiked: Boolean = false,
    private var hasdiss: Boolean = false,
    var likeCount: Int = 0,
    private var shareCount: Int = 0
) : BaseObservable(), Serializable {

    @Bindable
    fun getShareCount() : Int {
        return shareCount
    }

    fun setShareCount(shareCount: Int) {
        this.shareCount = shareCount
        notifyPropertyChanged(BR._all)
    }

    @Bindable
    fun getHasLiked(): Boolean {
        return hasLiked
    }

    fun setHasLiked(hasLiked: Boolean) {
        if (this.hasLiked == hasLiked) return
        if (hasLiked) {
            likeCount++;
            setHasdiss(false)
        } else {
            likeCount--;
        }
        this.hasLiked = hasLiked
        notifyPropertyChanged(BR._all)
    }

    @Bindable
    fun getHasdiss() : Boolean {
        return hasdiss
    }

    fun setHasdiss(hasdiss: Boolean) {
        if (this.hasdiss == hasdiss) return
        if (hasdiss) setHasLiked(false)
        this.hasdiss = hasdiss
        notifyPropertyChanged(BR._all)
    }
}