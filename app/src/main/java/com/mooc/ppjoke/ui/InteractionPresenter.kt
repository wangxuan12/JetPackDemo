package com.mooc.ppjoke.ui

import android.annotation.SuppressLint
import android.content.Context
import android.text.TextUtils
import android.widget.Toast
import androidx.arch.core.executor.ArchTaskExecutor
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.google.gson.JsonObject
import com.mooc.libcommon.extention.LiveDataBus
import com.mooc.libcommon.global.AppGlobals
import com.mooc.libnetwork.ApiResponse
import com.mooc.libnetwork.ApiService
import com.mooc.libnetwork.JsonCallback
import com.mooc.ppjoke.model.Comment
import com.mooc.ppjoke.model.Feed
import com.mooc.ppjoke.model.User
import com.mooc.ppjoke.ui.login.UserManager
import com.mooc.ppjoke.ui.share.ShareDialog

object InteractionPresenter {

    const val DATA_FROM_INTERACTION = "data_from_interaction"
    private const val URL_TOGGLE_FEED_LIKE = "/ugc/toggleFeedLike"
    private const val URL_TOGGLE_FEED_DISS = "/ugc/dissFeed"
    private const val URL_SHARE = "/ugc/increaseShareCount"
    private const val URL_TOGGLE_COMMENT_LIKE = "/ugc/toggleCommentLike"

    private fun <T> T.toggle(owner: LifecycleOwner, block : (T) -> Unit) {
        if (UserManager.isLogin())  block(this)
        else login(owner, Observer { block(this) })
    }


    private fun login(owner: LifecycleOwner, observer: Observer<User>) {
        val liveData = UserManager.login(AppGlobals.getApplication())
        liveData.observe(owner, Observer { user ->
            user?.also { observer.onChanged(it) }
        })
    }

    @SuppressLint("RestrictedApi")
    private fun showToast(message : String?) {
        ArchTaskExecutor.getMainThreadExecutor().execute {
            Toast.makeText(AppGlobals.getApplication(), message, Toast.LENGTH_SHORT).show()
        }
    }

    //给一个帖子点赞/取消点赞，它和给帖子点踩一踩是互斥的
    @JvmStatic fun toggleFeedLike(owner: LifecycleOwner, feed: Feed) {
        feed.toggle(owner) {
            ApiService.get<JsonObject>(URL_TOGGLE_FEED_LIKE)
                .addParam("userId", UserManager.getUserId())
                .addParam("itemId", it.itemId)
                .execute(object : JsonCallback<JsonObject>(){
                    override fun onSuccess(response: ApiResponse<JsonObject>) {
                        val hasLiked = response.body?.get("hasLiked")?.asBoolean ?: false
                        it.getUgc().setHasLiked(hasLiked)
                        LiveDataBus.with<Feed>(DATA_FROM_INTERACTION)
                            .postValue(feed)
                    }

                    override fun onError(response: ApiResponse<JsonObject>) {
                        showToast(response.message)
                    }
                })
        }
    }

    //给一个帖子点踩一踩/取消踩一踩,它和给帖子点赞是互斥的
    @JvmStatic fun toggleFeedDiss(owner: LifecycleOwner, feed: Feed) {
        feed.toggle(owner) {
            ApiService.get<JsonObject>(URL_TOGGLE_FEED_DISS)
                .addParam("userId", UserManager.getUserId())
                .addParam("itemId", it.itemId)
                .execute(object : JsonCallback<JsonObject>(){
                    override fun onSuccess(response: ApiResponse<JsonObject>) {
                        //diss接口返回的也是hasLiked
                        val hasLiked = response.body?.get("hasLiked")?.asBoolean ?: false
                        it.getUgc().setHasdiss(hasLiked)
                    }

                    override fun onError(response: ApiResponse<JsonObject>) {
                        showToast(response.message)
                    }
                })
        }
    }

    //打开分享面板
    @JvmStatic fun openShare(context: Context, feed: Feed) {
        var shareContent = feed.feeds_text
        if (!TextUtils.isEmpty(feed.url)) shareContent = feed.url
        else if (!TextUtils.isEmpty(feed.cover)) shareContent = feed.cover
        ShareDialog(context)
            .setShareContent(shareContent)
            .setShareItemClickListener {
                ApiService.get<JsonObject>(URL_SHARE)
                    .addParam("itemId", feed.itemId)
                    .execute(object : JsonCallback<JsonObject>(){
                        override fun onSuccess(response: ApiResponse<JsonObject>) {
                            val count = response.body?.get("count")?.asInt ?: 0
                            feed.getUgc().setShareCount(count)
                            LiveDataBus.with<Feed>(DATA_FROM_INTERACTION)
                                .postValue(feed)
                        }

                        override fun onError(response: ApiResponse<JsonObject>) {
                            showToast(response.message)
                        }
                    })
            }
            .show()
    }

    //给一个帖子的评论点赞/取消点赞
    @JvmStatic fun toggleCommentLike(owner: LifecycleOwner, comment: Comment) {
        comment.toggle(owner) {
            ApiService.get<JsonObject>(URL_TOGGLE_COMMENT_LIKE)
                .addParam("commentId", comment.commentId)
                .addParam("userId", UserManager.getUserId())
                .execute(object : JsonCallback<JsonObject>(){
                    override fun onSuccess(response: ApiResponse<JsonObject>) {
                        val hasLiked = response.body?.get("hasLiked")?.asBoolean ?: false
                        it.getUgc().setHasLiked(hasLiked)
                    }

                    override fun onError(response: ApiResponse<JsonObject>) {
                        showToast(response.message)
                    }
                })
        }
    }

    //收藏/取消收藏一个帖子
    @JvmStatic fun toggleFeedFavorite(owner: LifecycleOwner, feed: Feed) {
        feed.toggle(owner) {
            ApiService.get<JsonObject>("/ugc/toggleFavorite")
                .addParam("itemId", feed.itemId)
                .addParam("userId", UserManager.getUserId())
                .execute(object : JsonCallback<JsonObject>(){
                    override fun onSuccess(response: ApiResponse<JsonObject>) {
                        val hasFavorite = response.body?.get("hasFavorite")?.asBoolean ?: false
                        it.getUgc().setHasFavorite(hasFavorite)
                        LiveDataBus.with<Feed>(DATA_FROM_INTERACTION)
                            .postValue(feed)
                    }

                    override fun onError(response: ApiResponse<JsonObject>) {
                        showToast(response.message)
                    }
                })
        }
    }

    //关注/取消关注一个用户
    @JvmStatic fun toggleFollowUser(owner: LifecycleOwner, feed: Feed) {
        feed.toggle(owner) {
            ApiService.get<JsonObject>("/ugc/toggleUserFollow")
                .addParam("followUserId", UserManager.getUserId())
                .addParam("userId", feed.getAuthor()?.userId ?: 0)
                .execute(object : JsonCallback<JsonObject>(){
                    override fun onSuccess(response: ApiResponse<JsonObject>) {
                        val hasFollow = response.body?.get("hasLiked")?.asBoolean ?: false
                        it.getAuthor()?.setHasFollow(hasFollow)
                        LiveDataBus.with<Feed>(DATA_FROM_INTERACTION)
                            .postValue(feed);
                    }

                    override fun onError(response: ApiResponse<JsonObject>) {
                        showToast(response.message)
                    }
                })
        }
    }

}