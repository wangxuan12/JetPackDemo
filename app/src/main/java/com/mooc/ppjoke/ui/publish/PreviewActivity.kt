package com.mooc.ppjoke.ui.publish

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DataSpec
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.upstream.FileDataSource
import com.google.android.exoplayer2.util.Util
import com.mooc.libcommon.global.AppGlobals
import com.mooc.ppjoke.R
import com.mooc.ppjoke.databinding.ActivityLayoutPreviewBinding
import java.io.File

class PreviewActivity : AppCompatActivity(), View.OnClickListener {
    private var player: SimpleExoPlayer? = null
    private lateinit var previewBinding: ActivityLayoutPreviewBinding

    companion object {
        const val KEY_PREVIEW_URL = "preview_url"
        const val KEY_PREVIEW_VIDEO = "preview_video"
        const val KEY_PREVIEW_BTNTEXT = "preview_btntext"
        const val REQ_PREVIEW = 1000

        fun startActivityForResult(
            activity: CaptureActivity,
            previewUrl: String,
            isVideo: Boolean,
            btnText: String,
        ) {
            val intent = Intent(activity, PreviewActivity::class.java)
            intent.putExtra(KEY_PREVIEW_URL, previewUrl)
            intent.putExtra(KEY_PREVIEW_VIDEO, isVideo)
            intent.putExtra(KEY_PREVIEW_BTNTEXT, btnText)
            activity.startActivityForResult(intent, REQ_PREVIEW)
            activity.overridePendingTransition(0, 0)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        previewBinding = DataBindingUtil.setContentView<ActivityLayoutPreviewBinding>(this,
            R.layout.activity_layout_preview)
        val previewUrl = intent.getStringExtra(KEY_PREVIEW_URL)
        val isVideo = intent.getBooleanExtra(KEY_PREVIEW_VIDEO, false)
        val btnText = intent.getStringExtra(KEY_PREVIEW_BTNTEXT)
        if (TextUtils.isEmpty(btnText)) {
            previewBinding.actionOk.visibility = View.GONE
        } else {
            previewBinding.actionOk.visibility = View.VISIBLE
            previewBinding.actionOk.text = btnText
            previewBinding.actionOk.setOnClickListener(this)
        }
        previewBinding.actionClose.setOnClickListener(this)
        if (isVideo) previewVideo(previewUrl) else previewImage(previewUrl)
    }

    private fun previewImage(previewUrl: String?) {
        if (previewUrl == null) return
        previewBinding.photoView.visibility = View.VISIBLE
        Glide.with(this).load(previewUrl).into(previewBinding.photoView)
    }

    private fun previewVideo(previewUrl: String?) {
        if (previewUrl == null) return
        previewBinding.playerView.visibility = View.VISIBLE
        player = SimpleExoPlayer.Builder(AppGlobals.getApplication()).build()

        val file = File(previewUrl)
        val uri =
            if (file.exists()) {
                val dataSpec = DataSpec(Uri.fromFile(file))
                val fielDataSource = FileDataSource()
                fielDataSource.open(dataSpec)
                fielDataSource.uri
            } else {
                Uri.parse(previewUrl)
            }
        val factory = ProgressiveMediaSource.Factory(DefaultDataSourceFactory(this,
            Util.getUserAgent(this, packageName)))
        val mediaSource = factory.createMediaSource(uri)
        player?.prepare(mediaSource)
        player?.playWhenReady = true
        previewBinding.playerView.player = player
    }

    override fun onPause() {
        super.onPause()
        player?.playWhenReady = false
    }

    override fun onResume() {
        super.onResume()
        player?.playWhenReady = true
    }

    override fun onDestroy() {
        super.onDestroy()
        player?.playWhenReady = false
        player?.stop(true)
        player?.release()
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.action_close -> finish()
            R.id.action_ok -> {
                setResult(RESULT_OK, Intent())
                finish()
            }
        }
    }
}