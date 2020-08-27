package com.wx.jetpackdemo.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.paging.DataSource
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList

abstract class AbsViewModel<T> : ViewModel() {
    private val pageData : LiveData<PagedList<T>>
    private lateinit var dataSource : DataSource<Int, T>
    private val boundaryPageData : MutableLiveData<Boolean> = MutableLiveData()

    init {
        val config = PagedList.Config.Builder()
            .setPageSize(20)
            .setInitialLoadSizeHint(12)
//            .setMaxSize(100)
//            .setEnablePlaceholders(false)
//            .setPrefetchDistance()
            .build()

        val factory = object : DataSource.Factory<Int, T>() {
            override fun create(): DataSource<Int, T> {
                dataSource = createDataSource()
                return dataSource
            }

        }

        val callback = object : PagedList.BoundaryCallback<T>(){
            override fun onZeroItemsLoaded() {
                boundaryPageData.postValue(false)
            }

            override fun onItemAtFrontLoaded(itemAtFront: T) {
                boundaryPageData.postValue(true)
            }
        }

        pageData = LivePagedListBuilder(factory, config)
            .setInitialLoadKey(0)
//            .setFetchExecutor()
            .setBoundaryCallback(callback)
            .build()
    }

    fun getPageData() : LiveData<PagedList<T>> {
        return pageData
    }

    fun getDataSource() : DataSource<Int, T> {
        return dataSource
    }

    fun getBoundaryPageData() : MutableLiveData<Boolean> {
        return boundaryPageData
    }

    abstract fun createDataSource(): DataSource<Int, T>
}