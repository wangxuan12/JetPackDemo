package com.mooc.ppjoke.ui.detail

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Looper
import android.text.TextUtils
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.arch.core.executor.ArchTaskExecutor
import androidx.lifecycle.Observer
import com.mooc.libcommon.dialog.LoadingDialog
import com.mooc.libcommon.utils.*
import com.mooc.libcommon.view.ViewHelper
import com.mooc.libnetwork.ApiResponse
import com.mooc.libnetwork.ApiService
import com.mooc.libnetwork.JsonCallback
import com.mooc.ppjoke.R
import com.mooc.ppjoke.databinding.LayoutCommentDialogBinding
import com.mooc.ppjoke.model.Comment
import com.mooc.ppjoke.ui.login.UserManager
import com.mooc.ppjoke.ui.publish.CaptureActivity
import com.mooc.ppjoke.utils.BindingAdapters
import java.util.concurrent.atomic.AtomicInteger

@SuppressLint("RestrictedApi")
class CommentDialog : AppCompatDialogFragment(), View.OnClickListener {

    private var loadingDialog: LoadingDialog? = null
    private var height: Int = 0
    private var width: Int = 0
    private var isVideo = false
    private var filePath: String? = null
    private var itemId: Long? = null
    private lateinit var binding: LayoutCommentDialogBinding
    private var onAddComment: (Comment) -> Unit? = { null }

    private var fileUrl: String? = null
    private var coverUrl: String? = null

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
            R.id.comment_video -> {
                activity?.let { CaptureActivity.startActivityForResult(it) }
            }
            R.id.comment_delete -> {
                filePath = null
                isVideo = false
                width = 0
                height = 0
                binding.commentCover.setImageDrawable(null)
                binding.commentExtLayout.visibility = View.GONE
                binding.commentVideo.isEnabled = true
                binding.commentVideo.alpha = 1f
            }
        }
    }

    private fun publishComment() {
        if (TextUtils.isEmpty(binding.inputView.text)) return
        filePath?.also {
            if (isVideo) {
                FileUtils.generateVideoCover(it).observe(this, { coverPath ->
                    uploadFile(coverPath, it)
                })
            } else {
                uploadFile(null, it)
            }
        } ?: publish()
    }

    private fun uploadFile(coverPath: String?, filePath: String) {
        //AtomicInteger, CountDownLatch, CyclicBarrier
        showLoadingDialog()
        val count = AtomicInteger(1)
        coverPath?.also {
            count.set(2)
            postIO {
                val remain = count.decrementAndGet()
                coverUrl = FileUploadManager.upload(coverPath)
                if (remain <= 0) {
                    if (!TextUtils.isEmpty(fileUrl) && !TextUtils.isEmpty(coverUrl)) {
                        publish()
                    } else {
                        dismissLoadingDialog()
                        showToast(getString(R.string.file_upload_failed))
                    }
                }
            }
        }
        postIO {
            val remain = count.decrementAndGet()
            fileUrl = FileUploadManager.upload(filePath)
            if (remain <= 0) {
                if (!TextUtils.isEmpty(filePath) || !TextUtils.isEmpty(coverPath) && !TextUtils.isEmpty(coverUrl)) {
                    publish()
                } else {
                    dismissLoadingDialog()
                    showToast(getString(R.string.file_upload_failed))
                }
            }
        }
    }

    fun publish() {
        val commentText = binding.inputView.text.toString()
        ApiService.post<Comment>("/comment/addComment")
            .addParam("userId", UserManager.getUserId())
            .addParam("itemId", itemId)
            .addParam("commentText", commentText)
            .addParam("image_url", if(isVideo) coverUrl else fileUrl)
            .addParam("video_url", if(isVideo) filePath else null)
            .addParam("width", width)
            .addParam("height", height)
            .execute(object : JsonCallback<Comment>(){
                override fun onSuccess(response: ApiResponse<Comment>) {
                    showToast("评论发布成功")
                    postMain {
                        response.body?.let { onAddComment?.invoke(it) }
                        dismiss()
                    }
                    dismissLoadingDialog()
                }

                override fun onError(response: ApiResponse<Comment>) {
                    showToast("评论失败: ${response.message}")
                    dismissLoadingDialog()
                }
            })
    }

    private fun showLoadingDialog() {
        context?.also {
            (loadingDialog ?: LoadingDialog(it)).also {
                loadingDialog = it
                it.setLoadingText(getString(R.string.upload_text))
                it.setCanceledOnTouchOutside(false)
                it.setCancelable(false)
                it.takeIf { !it.isShowing }?.also { it.show() }
            }
        }
    }

    private fun dismissLoadingDialog() {
        loadingDialog?.also {
            //dismissLoadingDialog  的调用可能会出现在异步线程调用
            if (Looper.getMainLooper() != Looper.myLooper()) {
                postMain {
                    it.takeIf { it.isShowing }?.also { it.dismiss() }
                }
            } else {
                it.takeIf { it.isShowing }?.also { it.dismiss() }
            }
        }
    }

    fun onAddComment(block: (Comment) -> Unit) {
        onAddComment = block
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CaptureActivity.REQ_CAPTURE && resultCode == Activity.RESULT_OK) {
           data?.also {
               filePath = data.getStringExtra(CaptureActivity.RESULT_FILE_PATH)
               width = data.getIntExtra(CaptureActivity.RESULT_FILE_WIDTH, 0)
               height = data.getIntExtra(CaptureActivity.RESULT_FILE_HEIGHT, 0)
               isVideo = data.getBooleanExtra(CaptureActivity.RESULT_FILE_TYPE, false)
           }
            if (!TextUtils.isEmpty(filePath)) {
                binding.commentExtLayout.visibility = View.VISIBLE
                BindingAdapters.setImageUrl(binding.commentCover, filePath)
                if (isVideo) binding.commentIconPlay.visibility = View.VISIBLE
            }
            binding.commentVideo.isEnabled = false
            binding.commentVideo.alpha = 0.2f
        }
    }
}