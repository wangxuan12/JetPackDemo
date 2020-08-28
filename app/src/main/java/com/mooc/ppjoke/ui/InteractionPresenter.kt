package com.mooc.ppjoke.ui

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.arch.core.executor.ArchTaskExecutor
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.google.gson.JsonObject
import com.mooc.libcommon.AppGlobals
import com.mooc.libnetwork.ApiResponse
import com.mooc.libnetwork.ApiService
import com.mooc.libnetwork.JsonCallback
import com.mooc.ppjoke.model.Feed
import com.mooc.ppjoke.model.User
import com.mooc.ppjoke.ui.login.UserManager
import org.json.JSONObject

object InteractionPresenter {

    private const val URL_TOGGLE_FEED_LIKE = "/ugc/toggleFeedLike"
    private const val URL_TOGGLE_FEED_DISS = "/ugc/dissFeed"

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

}