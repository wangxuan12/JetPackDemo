package com.wx.jetpackdemo.ui.home

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.paging.DataSource
import androidx.paging.ItemKeyedDataSource
import com.google.gson.reflect.TypeToken
import com.wx.jetpackdemo.model.Feed
import com.wx.jetpackdemo.ui.AbsViewModel
import com.wx.libnetwork.*

class HomeViewModel : AbsViewModel<Feed>() {
    @Volatile private var withCache : Boolean = true

    override fun createDataSource(): DataSource<Int, Feed> {
        return dataSource
    }

    val dataSource = object : ItemKeyedDataSource<Int, Feed>() {
        override fun loadInitial(
            params: LoadInitialParams<Int>,
            callback: LoadInitialCallback<Feed>
        ) {
            //加载初始化数据
            loadData(0, callback)
            withCache = false
        }

        override fun loadAfter(params: LoadParams<Int>, callback: LoadCallback<Feed>) {
            //数据向后加载
            loadData(params.key, callback)
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
    private fun loadData(key: Int, callback: ItemKeyedDataSource.LoadCallback<Feed>) {
        val request = ApiService.get<List<Feed>>("/feeds/queryHotFeedsList")
            .addParam("feedType", "all")
            .addParam("userId", 0)
            .addParam("feedId", key)
            .addParam("pageCount", 10)
            .responseType(object : TypeToken<List<Feed>>() {}.type)

        if (withCache) {
            request.cacheStrategy(Request.CACHE_ONLY)
            request.execute(object : JsonCallback<List<Feed>>(){
                override fun onCacheSuccess(response: ApiResponse<List<Feed>>) {
                    Log.e("onCacheSuccess", "onCacheSuccess: ${response.body?.size}")
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
        }
    }
}