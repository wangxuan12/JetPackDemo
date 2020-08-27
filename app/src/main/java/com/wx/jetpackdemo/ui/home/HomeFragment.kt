package com.wx.jetpackdemo.ui.home

import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.scwang.smart.refresh.layout.api.RefreshLayout
import com.wx.jetpackdemo.model.Feed
import com.wx.jetpackdemo.ui.AbsListFragment
import com.wx.libnavannotation.FragmentDestination

@FragmentDestination(pageUrl = "main/tabs/home", asStarter = true)
class HomeFragment : AbsListFragment<Feed, HomeViewModel>() {

    private lateinit var homeViewModel: HomeViewModel

    override fun afterCreateView() {

    }

    override fun getAdapter(): PagedListAdapter<Feed, RecyclerView.ViewHolder> {
        val feedType =  arguments?.getString("feedType") ?: "all"
        return FeedAdapter(context, feedType) as PagedListAdapter<Feed, RecyclerView.ViewHolder>
    }

    override fun onRefresh(refreshLayout: RefreshLayout) {

    }

    override fun onLoadMore(refreshLayout: RefreshLayout) {

    }
}