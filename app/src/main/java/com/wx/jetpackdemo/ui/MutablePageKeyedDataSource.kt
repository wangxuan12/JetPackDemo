package com.wx.jetpackdemo.ui

import android.annotation.SuppressLint
import androidx.arch.core.executor.ArchTaskExecutor
import androidx.paging.PageKeyedDataSource
import androidx.paging.PagedList

class MutablePageKeyedDataSource<K, V> : PageKeyedDataSource<K, V>() {
    val data = arrayListOf<V>()

    @SuppressLint("RestrictedApi")
    fun buildNewPagedList(config : PagedList.Config) : PagedList<V> {
        return PagedList.Builder<K, V>(this, config)
            .setFetchExecutor(ArchTaskExecutor.getIOThreadExecutor())
            .setNotifyExecutor(ArchTaskExecutor.getMainThreadExecutor())
            .build()
    }

    override fun loadInitial(params: LoadInitialParams<K>, callback: LoadInitialCallback<K, V>) {
        callback.onResult(data, null, null)
    }

    override fun loadAfter(params: LoadParams<K>, callback: LoadCallback<K, V>) {
        callback.onResult(emptyList(), null)
    }

    override fun loadBefore(params: LoadParams<K>, callback: LoadCallback<K, V>) {
        callback.onResult(emptyList(), null)
    }
}