package com.mooc.ppjoke.ui.detail

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.TextUtils
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.arch.core.executor.ArchTaskExecutor
import com.mooc.libcommon.utils.PixUtils
import com.mooc.libcommon.utils.showToast
import com.mooc.libcommon.view.ViewHelper
import com.mooc.libnetwork.ApiResponse
import com.mooc.libnetwork.ApiService
import com.mooc.libnetwork.JsonCallback
import com.mooc.ppjoke.R
import com.mooc.ppjoke.databinding.LayoutCommentDialogBinding
import com.mooc.ppjoke.model.Comment
import com.mooc.ppjoke.ui.login.UserManager

@SuppressLint("RestrictedApi")
class CommentDialog : AppCompatDialogFragment(), View.OnClickListener {

    private var itemId: Long? = null
    private lateinit var binding: LayoutCommentDialogBinding
    private var onAddComment: (Comment) -> Unit? = { null }

    companion object {
        private const val KEY_ITEM_ID = "key_item_id"

        @JvmStatic
        fun newInstance(itemId: Long): CommentDialog{
            val args = Bundle()
            args.putLong(KEY_ITEM_ID, itemId)
            val fragment = CommentDialog()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val window = dialog?.window
        window?.setWindowAnimations(0)

        binding = LayoutCommentDialogBinding.inflate(inflater, window?.findViewById(R.id.content), false)
        binding.commentVideo.setOnClickListener(this)
        binding.commentSend.setOnClickListener(this)
        binding.commentDelete.setOnClickListener(this)

        // TODO: 2020/9/5 输入布局不在底部
        val layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        layoutParams.gravity = Gravity.BOTTOM
        binding.inputLayout.layoutParams = layoutParams
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT)

        itemId = arguments?.getLong(KEY_ITEM_ID)
        ViewHelper.setViewOutline(binding.root, PixUtils.dp2px(10), ViewHelper.RADIUS_TOP)
        binding.root.post { showSoftInputMethod() }
        dismissWhenPressBack()
        return binding.root
    }

    private fun dismissWhenPressBack() {
        binding.inputView.onKeyEvent {
            binding.inputView.postDelayed({
                dismiss()
            }, 200)
            return@onKeyEvent true
        }
    }

    private fun showSoftInputMethod() {
        binding.inputView.isFocusable = true
        binding.inputView.isFocusableInTouchMode = true
        //请求获得焦点
        binding.inputView.requestFocus()
        val manager = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
        manager?.showSoftInput(binding.inputView, 0)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.comment_send -> publishComment()
            R.id.comment_video -> {}
            R.id.comment_delete -> {}
        }
    }

    private fun publishComment() {
        if (TextUtils.isEmpty(binding.inputView.text)) return;
        val commentText = binding.inputView.text.toString()
        ApiService.post<Comment>("/comment/addComment")
            .addParam("userId", UserManager.getUserId())
            .addParam("itemId", itemId)
            .addParam("commentText", commentText)
            .addParam("image_url", null)
            .addParam("video_url", null)
            .addParam("width", 0)
            .addParam("height", 0)
            .execute(object : JsonCallback<Comment>(){
                override fun onSuccess(response: ApiResponse<Comment>) {
                    showToast("评论发布成功")
                    ArchTaskExecutor.getMainThreadExecutor().execute {
                        response.body?.let { onAddComment?.invoke(it) }
                        dismiss()
                    }
                }

                override fun onError(response: ApiResponse<Comment>) {
                    showToast("评论失败: ${response.message}")
                }
            })
    }

    fun onAddComment(block: (Comment) -> Unit) {
        onAddComment = block
    }
}