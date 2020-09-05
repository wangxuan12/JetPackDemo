package com.mooc.ppjoke.ui

import android.annotation.SuppressLint
import androidx.arch.core.executor.ArchTaskExecutor
import androidx.paging.ItemKeyedDataSource
import androidx.paging.PagedList

/**
 * 一个可变更的ItemKeyedDataSource 数据源
 * <p>
 * 工作原理是：我们知道DataSource是会被PagedList 持有的。
 * 一旦，我们调用了new PagedList.Builder<Key, Value>().build(); 那么就会立刻触发当前DataSource的loadInitial()方法，而且是同步
 * 详情见ContiguousPagedList的构造函数,而我们在当前DataSource的loadInitial()方法中返回了 最新的数据集合 data。
 * 一旦，我们再次调用PagedListAdapter#submitList()方法 就会触发差分异计算 把新数据变更到列表之上了。
 *
 * @param <K>
 * @param <V>
 */
abstract class MutableItemKeyedDataSource<K, V>(var dataSource: ItemKeyedDataSource<K, V>): ItemKeyedDataSource<K, V>() {
    val data = arrayListOf<V>()

    @SuppressLint("RestrictedApi")
    fun buildNewPagedList(config : PagedList.Config) : PagedList<V> {
        return PagedList.Builder<K, V>(this, config)
            .setFetchExecutor(ArchTaskExecutor.getIOThreadExecutor())
            .setNotifyExecutor(ArchTaskExecutor.getMainThreadExecutor())
            .build()
    }

    override fun loadInitial(params: LoadInitialParams<K>, callback: LoadInitialCallback<V>) {
        callback.onResult(data)
    }

    override fun loadAfter(params: LoadParams<K>, callback: LoadCallback<V>) {
        //一旦 和当前DataSource关联的PagedList被提交到PagedListAdapter。那么ViewModel中创建的DataSource 就不会再被调用了
        //需要在分页的时候 代理一下 原来的DataSource，迫使其继续工作
        dataSource.loadAfter(params, callback)
    }

    override fun loadBefore(params: LoadParams<K>, callback: LoadCallback<V>) {
        callback.onResult(emptyList())
    }

    abstract override fun getKey(item: V): K
}