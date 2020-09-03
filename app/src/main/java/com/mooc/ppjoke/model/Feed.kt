package com.mooc.ppjoke.model

import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import java.io.Serializable

data class Feed constructor(
    var activityIcon: String? = null,
    var activityText: String? = null,
    var authorId: Long = 0,
    var cover: String? = null,
    var createTime: Long = 0,
    var duration: Double = 0.0,
    var feeds_text: String? = null,
    var height: Int = 0,
    var id: Int = 0,
    var itemId: Long = 0,
    var itemType: Int = 0,
    var url: String? = null,
    var width: Int = 0,
    private var author : User? = null,
    var topComment: Comment? = null,
    private var ugc: Ugc? = null
) : BaseObservable(), Serializable {
    companion object {
        private const val serialVersionUID: Long = -90000002L
        const val TYPE_IMAGE = 1
        const val TYPE_VIDEO = 2
    }

    @Bindable
    fun getUgc() : Ugc {
        return ugc ?: Ugc().also { ugc = it }
    }

    fun setUgc(ugc: Ugc) {
        this.ugc = ugc
    }

    fun getAuthor() : User? {
        return author
    }

    fun setAuthor(author: User?) {
        this.author = author
    }
}