package com.mooc.ppjoke.ui.detail

import android.content.Context
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.mooc.libcommon.extention.AbsPagedListAdapter
import com.mooc.libcommon.utils.PixUtils
import com.mooc.ppjoke.databinding.LayoutFeedCommentListItemBinding
import com.mooc.ppjoke.model.Comment
import com.mooc.ppjoke.ui.login.UserManager

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
        getItem(i)?.let { holder.bindView(it, context) }
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