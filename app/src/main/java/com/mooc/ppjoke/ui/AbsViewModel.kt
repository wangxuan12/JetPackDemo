package com.mooc.ppjoke.ui

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
    protected val config : PagedList.Config

    init {
        config = PagedList.Config.Builder()
            .setPageSize(10)
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

        //PagedList数据被加载 情况的边界回调callback
        //但 不是每一次分页 都会回调这里，具体请看 ContiguousPagedList#mReceiver#onPageResult
        //deferBoundaryCallbacks
        val callback = object : PagedList.BoundaryCallback<T>(){
            override fun onZeroItemsLoaded() {
                //新提交的PagedList中没有数据
                boundaryPageData.postValue(false)
            }

            override fun onItemAtFrontLoaded(itemAtFront: T) {
                //新提交的PagedList中第一条数据被加载到列表上
                boundaryPageData.postValue(true)
            }

            //新提交的PagedList中最后一条数据被加载到列表上
//            override fun onItemAtEndLoaded(itemAtEnd: T) {
//                super.onItemAtEndLoaded(itemAtEnd)
//            }
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

    //可以在这个方法里 做一些清理 的工作
//    override fun onCleared() {
//        super.onCleared()
//    }
}