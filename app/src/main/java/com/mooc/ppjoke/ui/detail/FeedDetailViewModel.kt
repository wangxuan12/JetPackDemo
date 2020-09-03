package com.mooc.ppjoke.ui.detail

import androidx.paging.ItemKeyedDataSource
import com.google.gson.reflect.TypeToken
import com.mooc.libnetwork.ApiService
import com.mooc.ppjoke.model.Comment
import com.mooc.ppjoke.ui.AbsViewModel
import com.mooc.ppjoke.ui.login.UserManager

class FeedDetailViewModel: AbsViewModel<Comment>() {
    var itemId: Long = 0

    override fun createDataSource(): DataSource {
        return DataSource()
    }

    inner class DataSource: ItemKeyedDataSource<Int, Comment>() {
        override fun loadInitial(
            params: LoadInitialParams<Int>,
            callback: LoadInitialCallback<Comment>
        ) {
            loadData(params.requestedInitialKey ?: 0, params.requestedLoadSize, callback)
        }

        override fun loadAfter(params: LoadParams<Int>, callback: LoadCallback<Comment>) {
            params.key.takeIf { it > 0 }?.let { loadData(it, params.requestedLoadSize, callback) }
        }

        override fun loadBefore(params: LoadParams<Int>, callback: LoadCallback<Comment>) {
            callback.onResult(emptyList())
        }

        override fun getKey(item: Comment): Int {
            return item.id
        }

        private fun loadData(
            key: Int,
            requestedLoadSize: Int,
            callback: LoadCallback<Comment>
        ) {
            val response = ApiService.get<List<Comment>>("/comment/queryFeedComments")
                .addParam("id", key)
                .addParam("itemId", itemId)
                .addParam("userId", UserManager.getUserId())
                .addParam("pageCount", requestedLoadSize)
                .responseType(object : TypeToken<List<Comment>>() {}.type)
                .execute()

            callback.onResult(response.body ?: emptyList())
        }

    }
}