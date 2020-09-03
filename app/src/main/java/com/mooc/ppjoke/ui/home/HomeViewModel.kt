package com.mooc.ppjoke.ui.home

import android.annotation.SuppressLint
import android.util.Log
import androidx.arch.core.executor.ArchTaskExecutor
import androidx.lifecycle.MutableLiveData
import androidx.paging.DataSource
import androidx.paging.ItemKeyedDataSource
import androidx.paging.PagedList
import com.google.gson.reflect.TypeToken
import com.mooc.ppjoke.model.Feed
import com.mooc.ppjoke.ui.AbsViewModel
import com.mooc.ppjoke.ui.MutablePageKeyedDataSource
import com.mooc.libnetwork.ApiResponse
import com.mooc.libnetwork.ApiService
import com.mooc.libnetwork.JsonCallback
import com.mooc.libnetwork.Request
import com.mooc.ppjoke.ui.login.UserManager
import java.util.concurrent.atomic.AtomicBoolean

class HomeViewModel : AbsViewModel<Feed>() {
    @Volatile private var withCache : Boolean = true
    private val cacheLiveData : MutableLiveData<PagedList<Feed>> = MutableLiveData()
    private var loadAfter : AtomicBoolean = AtomicBoolean(false)
    private var feedType: String? = null

    fun getCacheLiveData() : MutableLiveData<PagedList<Feed>> {
        return cacheLiveData
    }

    override fun createDataSource(): DataSource<Int, Feed> {
        return FeedDataSource()
    }

    inner class FeedDataSource: ItemKeyedDataSource<Int, Feed>() {
        override fun loadInitial(
            params: LoadInitialParams<Int>,
            callback: LoadInitialCallback<Feed>
        ) {
            //加载初始化数据
            Log.e("homeviewmodel", "loadInitial: ")
            loadData(0, params.requestedLoadSize, callback)
            withCache = false
        }

        override fun loadAfter(params: LoadParams<Int>, callback: LoadCallback<Feed>) {
            //数据向后加载
            Log.e("homeviewmodel", "loadAfter: ")
            loadData(params.key, params.requestedLoadSize, callback)
        }

        override fun loadBefore(params: LoadParams<Int>, callback: LoadCallback<Feed>) {
            //数据向前加载
            callback.onResult(emptyList())
        }

        override fun getKey(item: Feed): Int {
            return item.id
        }

    }

    //  /feeds/queryHotFeedsList
    private fun loadData(key: Int, count: Int, callback: ItemKeyedDataSource.LoadCallback<Feed>) {
        if (key > 0) loadAfter.set(true)
        val request = ApiService.get<List<Feed>>("/feeds/queryHotFeedsList")
            .addParam("feedType", feedType ?: "all")
            .addParam("userId", UserManager.getUserId())
            .addParam("feedId", key)
            .addParam("pageCount", count)
            .responseType(object : TypeToken<List<Feed>>() {}.type)

        if (withCache) {
            request.cacheStrategy(Request.CACHE_ONLY)
            request.execute(object : JsonCallback<List<Feed>>(){
                override fun onCacheSuccess(response: ApiResponse<List<Feed>>) {
                    Log.e("loadData", "onCacheSuccess: ${response.body?.size}")
                    val dataSource = MutablePageKeyedDataSource<Int, Feed>()
                    response.body?.let { dataSource.data.addAll(it) }
                    val pagedList = dataSource.buildNewPagedList(config)
                    cacheLiveData.postValue(pagedList)
                }
            })
        }

        val netRequest = if (withCache) request.clone() else request
        netRequest.cacheStrategy(if (key == 0) Request.NET_CACHE else Request.NET_ONLY)
        val response = netRequest.execute()
        val data = if (response.body == null) emptyList<Feed>() else response.body
        data?.also {
            callback.onResult(it)
        }
        if (key > 0) {
            //通过livedata发送数据， 告诉ui层，是否应该主动关闭上拉加载动画
            getBoundaryPageData().postValue(!data.isNullOrEmpty())
            loadAfter.set(false)
        }

        Log.e("loadData", "loadData: Key: $key")
    }

    @SuppressLint("RestrictedApi")
    fun loadAfter(id: Int, callback: ItemKeyedDataSource.LoadCallback<Feed>) {
        if (loadAfter.get()) {
            callback.onResult(emptyList())
            return
        }
        ArchTaskExecutor.getIOThreadExecutor().execute {
            loadData(id, config.pageSize, callback)
        }
    }

    fun setFeedType(feedType: String) {
        this.feedType = feedType
    }
}