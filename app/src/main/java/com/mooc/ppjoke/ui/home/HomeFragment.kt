package com.mooc.ppjoke.ui.home

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.lifecycle.Observer
import androidx.paging.ItemKeyedDataSource
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mooc.libnavannotation.FragmentDestination
import com.mooc.ppjoke.exoplayer.PageListPlayDetector
import com.mooc.ppjoke.exoplayer.PageListPlayManager
import com.mooc.ppjoke.model.Feed
import com.mooc.ppjoke.ui.AbsListFragment
import com.mooc.ppjoke.ui.MutablePageKeyedDataSource
import com.scwang.smart.refresh.layout.api.RefreshLayout
import kotlinx.android.synthetic.main.layout_refresh_view.*

@FragmentDestination(pageUrl = "main/tabs/home", asStarter = true)
class HomeFragment : AbsListFragment<Feed, HomeViewModel>() {
    private lateinit var playDetector: PageListPlayDetector
    private lateinit var feedType: String

    companion object {

        fun newInstance(feedType: String): HomeFragment {
            val args = Bundle()
            args.putString("feedType", feedType)
            val fragment = HomeFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel?.getCacheLiveData()?.observe(viewLifecycleOwner, Observer { pagedList -> adapter?.submitList(pagedList) })
        playDetector = PageListPlayDetector(viewLifecycleOwner, recycler_view)
        viewModel?.setFeedType(feedType)
    }

    override fun createAdapter(): PagedListAdapter<Feed, RecyclerView.ViewHolder> {
        feedType =  arguments?.getString("feedType") ?: "all"
        return object : FeedAdapter(context, feedType){
            override fun onViewAttachedToWindow(holder: ViewHolder) {
                super.onViewAttachedToWindow(holder)
                if (holder.isVideoItem()) holder.getListPlayerView()?.let { playDetector.addTarget(it) }
            }

            override fun onViewDetachedFromWindow(holder: ViewHolder) {
                super.onViewDetachedFromWindow(holder)
                if (holder.isVideoItem()) holder.getListPlayerView()?.let { playDetector.removeTarget(it) }
            }

//            override fun onCurrentListChanged(
//                previousList: PagedList<Feed>?,
//                currentList: PagedList<Feed>?
//            ) {
//                if (previousList != null && currentList != null) {
//                    if (!currentList.containsAll(previousList)) recycler_view?.scrollToPosition(0)
//                }
//            }
        } as PagedListAdapter<Feed, RecyclerView.ViewHolder>
    }

    override fun onRefresh(refreshLayout: RefreshLayout) {
        //invalidate 之后Paging会重新创建一个DataSource 重新调用它的loadInitial方法加载初始化数据
        //详情见：LivePagedListBuilder#compute方法
        viewModel?.getDataSource()?.invalidate()
    }

    override fun onLoadMore(refreshLayout: RefreshLayout) {
//        val currentList = adapter?.currentList
//        if (currentList.isNullOrEmpty()) {
//            finishRefresh(false)
//            return
//        }
        adapter?.also {
            val feed = it.currentList?.get(it.itemCount - 1)
            val config = it.currentList?.config
            feed?.let { feed ->
                viewModel?.loadAfter(feed.id, object : ItemKeyedDataSource.LoadCallback<Feed>(){
                    override fun onResult(data: MutableList<Feed>) {
                        if (data.isNullOrEmpty()) return
                        //这里 手动接管 分页数据加载的时候 使用MutableItemKeyedDataSource也是可以的。
                        //由于当且仅当 paging不再帮我们分页的时候，我们才会接管。所以 就不需要ViewModel中创建的DataSource继续工作了，所以使用
                        //MutablePageKeyedDataSource也是可以的
                        val dataSource = MutablePageKeyedDataSource<Int, Feed>()

                        //这里要把列表上已经显示的先添加到dataSource.data中
                        //而后把本次分页回来的数据再添加到dataSource.data中
//                        dataSource.data.addAll(currentList)
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

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (hidden) playDetector.onPause()
        else playDetector.onResume()
        if (hidden) Log.e("HomeFragment", "onHiddenPause: $feedType" )
        else Log.e("HomeFragment", "onHiddenReesume: $feedType" )
    }

    override fun onResume() {
        super.onResume()
        Log.e("HomeFragment", "onResume: $feedType" )
        if (parentFragment?.let { it.isVisible && isVisible } ?: isVisible) playDetector.onResume()
    }

    override fun onPause() {
        //如果是跳转到详情页,就不需要 暂停视频播放了
        //如果是前后台切换 或者去别的页面了 都是需要暂停视频播放的
        super.onPause()
        Log.e("HomeFragment", "onPause: $feedType")
        playDetector.onPause()
    }

    override fun onDestroy() {
        PageListPlayManager.release(feedType)
        super.onDestroy()
    }
}