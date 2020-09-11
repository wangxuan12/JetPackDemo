package com.mooc.ppjoke.ui.detail

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.mooc.ppjoke.model.Feed

class FeedDetailActivity : AppCompatActivity() {
    private lateinit var viewHandler: ViewHandler

    companion object {
        private const val KEY_FEED = "key_feed"
        const val KEY_CATEGORY = "key_category"


        fun start(context: Context, feed: Feed, category: String) {
            val intent = Intent(context, FeedDetailActivity::class.java)
            intent.putExtra(KEY_FEED, feed)
            intent.putExtra(KEY_CATEGORY, category)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val feed: Feed? = intent.getSerializableExtra(KEY_FEED) as Feed?
        if (feed == null) {
            finish()
            return
        }
        viewHandler =
            if (feed.itemType == Feed.TYPE_IMAGE) ImageViewHandler(this)
            else VideoViewHandler(this)
        viewHandler.bindData(feed)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        viewHandler.onActivityResult(requestCode, resultCode, data)
    }
}