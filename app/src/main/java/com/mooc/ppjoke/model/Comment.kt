package com.mooc.ppjoke.model

import java.io.Serializable

data class Comment(
    var author: User? = null,
    var commentCount: Int = 0,
    var commentId: Long = 0,
    var commentText: String? = null,
    var commentType: Int = 0,
    var createTime: Long = 0,
    var hasLiked: Boolean = false,
    var height: Int = 0,
    var id: Int = 0,
    var imageUrl: String? = null,
    var itemId: Long = 0,
    var likeCount: Int = 0,
    private var ugc: Ugc? = null,
    var userId: Long = 0,
    var videoUrl: String? = null,
    var width: Int = 0,
) : Serializable {

    companion object {
        private const val serialVersionUID: Long = -90014747L
        const val COMMENT_TYPE_IMAGE_TEXT = 2
        const val COMMENT_TYPE_VIDEO = 3
    }

    fun getUgc(): Ugc {
        return ugc ?: Ugc().also { ugc = it }
    }

    fun setUgc(ugc: Ugc?) {
        this.ugc = ugc
    }
}
