package com.mooc.ppjoke.ui.detail

import android.app.Activity
import android.content.Context
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.paging.ItemKeyedDataSource
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.mooc.libcommon.extention.AbsPagedListAdapter
import com.mooc.libcommon.utils.PixUtils
import com.mooc.ppjoke.databinding.LayoutFeedCommentListItemBinding
import com.mooc.ppjoke.model.Comment
import com.mooc.ppjoke.ui.InteractionPresenter
import com.mooc.ppjoke.ui.MutableItemKeyedDataSource
import com.mooc.ppjoke.ui.login.UserManager
import com.mooc.ppjoke.ui.publish.PreviewActivity

open class FeedCommentAdapter(val context: Context) : AbsPagedListAdapter<Comment, FeedCommentAdapter.ViewHolder>(object :
    DiffUtil.ItemCallback<Comment>() {
    override fun areItemsTheSame(oldItem: Comment, newItem: Comment): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Comment, newItem: Comment): Boolean {
        return oldItem == newItem
    }

}) {
    private var inflater: LayoutInflater = LayoutInflater.from(context)

    override fun onCreateViewHolder2(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = LayoutFeedCommentListItemBinding.inflate(inflater, parent, false)
        return ViewHolder(binding.root, binding)
    }

    override fun onBindViewHolder2(holder: ViewHolder, i: Int) {
        getItem(i)?.let { comment ->
            holder.bindView(comment, context)
            holder.binding.commentDelete.setOnClickListener {view ->
                InteractionPresenter.deleteFeedComment(context, comment.itemId, comment.commentId)
                    .observe(context as LifecycleOwner, {
                        if (it) deleteAndRefresh(comment)
                    })
            }
            holder.binding.commentCover.setOnClickListener {
                val isVideo = comment.commentType == Comment.COMMENT_TYPE_VIDEO
                PreviewActivity.startActivityForResult(context as Activity, if (isVideo) comment.videoUrl ?: "" else comment.imageUrl ?: "", isVideo, "")
            }
        }
    }

    fun addAndRefreshList(comment: Comment) {
        currentList?.also {
            val dataSource = object :
                MutableItemKeyedDataSource<Int, Comment>(it.dataSource as ItemKeyedDataSource<Int, Comment>) {
                override fun getKey(item: Comment): Int {
                    return item.id
                }
            }
            dataSource.data.add(comment)
            dataSource.data.addAll(it)
            val pageList = dataSource.buildNewPagedList(it.config)
            submitList(pageList)
        }
    }

    fun deleteAndRefresh(comment: Comment) {
        currentList?.also {
            val dataSource = object :
                MutableItemKeyedDataSource<Int, Comment>(it.dataSource as ItemKeyedDataSource<Int, Comment>) {
                override fun getKey(item: Comment): Int {
                    return item.id
                }
            }
            for (item in it) {
                if (comment != item) dataSource.data.add(item)
            }
            val pageList = dataSource.buildNewPagedList(it.config)
            submitList(pageList)
        }
    }

    class ViewHolder(itemView: View, val binding: LayoutFeedCommentListItemBinding): RecyclerView.ViewHolder(itemView) {
        fun bindView(item: Comment, context: Context) {
            binding.owner = context as LifecycleOwner
            binding.comment = item
            val self = if(item.author == null ) false else item.author?.userId == UserManager.getUserId()
            binding.labelAuthor.visibility = if (self) View.VISIBLE else View.GONE
            binding.commentDelete.visibility = if (self) View.VISIBLE else View.GONE
            if (!TextUtils.isEmpty(item.imageUrl)) {
                binding.commentExt.visibility = View.VISIBLE
                binding.commentCover.visibility = View.VISIBLE
                binding.commentCover.bindData(
                    item.width,
                    item.height,
                    0,
                    PixUtils.dp2px(200),
                    PixUtils.dp2px(200),
                    item.imageUrl
                )
                binding.videoIcon.visibility = if (TextUtils.isEmpty(item.videoUrl)) View.GONE else View.VISIBLE
            } else {
                binding.commentCover.visibility = View.GONE
                binding.videoIcon.visibility = View.GONE
                binding.commentExt.visibility = View.GONE
            }
        }

    }
}