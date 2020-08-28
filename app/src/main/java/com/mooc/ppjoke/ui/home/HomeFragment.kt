package com.mooc.ppjoke.ui.home

import androidx.lifecycle.Observer
import androidx.paging.ItemKeyedDataSource
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.scwang.smart.refresh.layout.api.RefreshLayout
import com.mooc.ppjoke.model.Feed
import com.mooc.ppjoke.ui.AbsListFragment
import com.mooc.ppjoke.ui.MutablePageKeyedDataSource
import com.mooc.libnavannotation.FragmentDestination

@FragmentDestination(pageUrl = "main/tabs/home", asStarter = true)
class HomeFragment : AbsListFragment<Feed, HomeViewModel>() {

    override fun afterCreateView() {
        viewModel?.getCacheLiveData()?.observe(viewLifecycleOwner, Observer { pagedList -> adapter?.submitList(pagedList) })
    }

    override fun createAdapter(): PagedListAdapter<Feed, RecyclerView.ViewHolder> {
        val feedType =  arguments?.getString("feedType") ?: "all"
        return FeedAdapter(context, feedType) as PagedListAdapter<Feed, RecyclerView.ViewHolder>
    }

    override fun onRefresh(refreshLayout: RefreshLayout) {
        viewModel?.getDataSource()?.invalidate()
    }

    override fun onLoadMore(refreshLayout: RefreshLayout) {
        adapter?.let {
            val feed = it.currentList?.get(it.itemCount - 1)
            val config = it.currentList?.config
            feed?.let { feed ->
                viewModel?.loadAfter(feed.id, object : ItemKeyedDataSource.LoadCallback<Feed>(){
                    override fun onResult(data: MutableList<Feed>) {
                        if (data.isNullOrEmpty()) return
                        val dataSource = MutablePageKeyedDataSource<Int, Feed>()
                        dataSource.data.addAll(data)
                        config?.let { c ->
                            val pagedList = dataSource.buildNewPagedList(c)
                            submitList(pagedList)
                        }

                    }
                })
            }
        }

    }
}