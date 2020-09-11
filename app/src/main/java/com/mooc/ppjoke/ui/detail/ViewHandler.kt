package com.mooc.ppjoke.ui.detail

import android.content.Intent
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.paging.PagedList
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mooc.libcommon.utils.PixUtils
import com.mooc.libcommon.view.EmptyView
import com.mooc.ppjoke.R
import com.mooc.ppjoke.databinding.LayoutFeedDetailBottomInteractionBinding
import com.mooc.ppjoke.model.Comment
import com.mooc.ppjoke.model.Feed

abstract class ViewHandler(val activity: FragmentActivity) {
    private var commentDialog: CommentDialog? = null
    protected lateinit var interactionBinding: LayoutFeedDetailBottomInteractionBinding
    protected lateinit var feed: Feed
    protected lateinit var recyclerView: RecyclerView
    protected lateinit var listAdapter: FeedCommentAdapter
    private var viewModel: FeedDetailViewModel = ViewModelProvider(activity).get(FeedDetailViewModel::class.java)

    @CallSuper
    open fun bindData(feed: Feed) {
        interactionBinding.owner = activity
        this.feed = feed
        recyclerView.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
        recyclerView.itemAnimator = null
        listAdapter = object : FeedCommentAdapter(activity){
            override fun onCurrentListChanged(
                previousList: PagedList<Comment>?,
                currentList: PagedList<Comment>?
            ) {
                handleEmpty(currentList?.let { it.size > 0 } ?: false)
            }
        }
        recyclerView.adapter = listAdapter

        viewModel.itemId = feed.itemId
        viewModel.getPageData().observe(activity, Observer {
            listAdapter.submitList(it)
            handleEmpty(it.size > 0)
        })
        interactionBinding.inputView.setOnClickListener {
            if (commentDialog == null) commentDialog = CommentDialog.newInstance(feed.itemId)
            commentDialog?.onAddComment {
                handleEmpty(true)
                listAdapter.addAndRefreshList(it)
            }
            commentDialog?.show(activity.supportFragmentManager, "comment_dialog")
        }
    }

    private var emptyView: EmptyView? = null
    private fun handleEmpty(hasData: Boolean) {
        if (hasData) emptyView?.also { listAdapter.removeHeaderView(it) }
        else {
            (emptyView ?: EmptyView(activity)).also {
                val layoutParams = RecyclerView.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                layoutParams.topMargin = PixUtils.dp2px(40)
                it.layoutParams = layoutParams
                it.setTitle(activity.getString(R.string.feed_comment_empty))
                listAdapter.addHeaderView(it)
            }
        }
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        commentDialog?.onActivityResult(requestCode, resultCode, data)
    }
}