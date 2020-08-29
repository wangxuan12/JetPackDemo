package com.mooc.ppjoke.ui.share

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.ResolveInfo
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.TextUtils
import android.view.*
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mooc.libcommon.utils.PixUtils
import com.mooc.libcommon.view.CornerFrameLayout
import com.mooc.libcommon.view.ViewHelper
import com.mooc.ppjoke.R
import com.mooc.ppjoke.view.CustomImageView

class ShareDialog(context: Context) : AlertDialog(context) {
    private var shareAdapter: ShareAdapter? = null
    private var shareContent: String? = null
    private var listener: View.OnClickListener? = null
    private val shareItems = mutableListOf<ResolveInfo>()
    private var onClick: ((View) -> Unit?)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        super.onCreate(savedInstanceState)

        val layout = CornerFrameLayout(context)
        layout.setBackgroundColor(Color.WHITE)
        layout.setViewOutline(PixUtils.dp2px(20), ViewHelper.RADIUS_TOP)

        val gridView = RecyclerView(context)
        gridView.layoutManager = GridLayoutManager(context, 4)
        shareAdapter = ShareAdapter()
        gridView.adapter = shareAdapter

        val params = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        val margin = PixUtils.dp2px(20)
        params.setMargins(margin, margin, margin, margin)
        params.gravity = Gravity.CENTER
        layout.addView(gridView, params)

        setContentView(layout)
        window?.setGravity(Gravity.BOTTOM)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        queryShareItems()
    }

    private fun queryShareItems() {
        val intent = Intent()
        intent.action = Intent.ACTION_SEND
        intent.type = "text/plain"

        val resolveInfos = context.packageManager.queryIntentActivities(intent, 0)
        for (resolveInfo: ResolveInfo in resolveInfos) {
            val packageName = resolveInfo.activityInfo.packageName
            if (TextUtils.equals(packageName, "com.tencent.mm") || TextUtils.equals(packageName, "com.tencent.mobileqq")) {
                shareItems.add(resolveInfo)
            }
        }
        shareAdapter?.notifyDataSetChanged()
    }

    fun setShareContent(shareContent: String?) : ShareDialog {
        this.shareContent = shareContent
        return this
    }

    fun setShareItemClickListener(block: (View) -> Unit) : ShareDialog {
        this.onClick = block
        return this
    }

    private inner class ShareAdapter() : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        private val packageManager by lazy { context.packageManager }

        @SuppressLint("InflateParams")
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val inflate = LayoutInflater.from(parent.context).inflate(R.layout.layout_share_item, parent, false)
            return object : RecyclerView.ViewHolder(inflate){}
        }

        override fun getItemCount(): Int {
            return shareItems.size
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val resolveInfo = shareItems[position]
            val imageView = holder.itemView.findViewById<CustomImageView>(R.id.share_icon)
            val textView = holder.itemView.findViewById<TextView>(R.id.share_text)

            imageView.setImageDrawable(resolveInfo.loadIcon(context.packageManager))
            textView.text = resolveInfo.loadLabel(packageManager)

            holder.itemView.setOnClickListener {
                val pkg = resolveInfo.activityInfo.packageName
                val cls = resolveInfo.activityInfo.name
                val intent = Intent()
                intent.action = Intent.ACTION_SEND
                intent.type = "text/plain"
                intent.component = ComponentName(pkg, cls)
                intent.putExtra(Intent.EXTRA_TEXT, shareContent)
                context.startActivity(intent)

                listener?.onClick(it)
                onClick?.invoke(it)
                dismiss()
            }
        }

    }
}