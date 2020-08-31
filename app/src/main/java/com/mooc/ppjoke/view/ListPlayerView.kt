package com.mooc.ppjoke.view

import android.annotation.SuppressLint
import android.content.Context
import android.text.TextUtils
import android.util.AttributeSet
import android.view.*
import android.widget.FrameLayout
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.PlayerControlView
import com.mooc.libcommon.utils.PixUtils
import com.mooc.ppjoke.R
import com.mooc.ppjoke.exoplayer.IPlayTarget
import com.mooc.ppjoke.exoplayer.PageListPlayManager
import com.mooc.ppjoke.utils.BindingAdapters
import kotlinx.android.synthetic.main.layout_player_view.view.*

class ListPlayerView : FrameLayout, IPlayTarget, PlayerControlView.VisibilityListener, Player.EventListener {
    private lateinit var category: String
    private lateinit var videoUrl: String
    private var isPlaying = false

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : this(context, attrs, defStyleAttr, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int): super(context, attrs, defStyleAttr, defStyleRes) {
        LayoutInflater.from(context).inflate(R.layout.layout_player_view, this, true)

        play_btn?.setOnClickListener {
            if (isPlaying()) inActive() else onActive()
        }
        this.transitionName = "listPlayerView"
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        PageListPlayManager.get(category).controlView?.show()
        return true
    }

    fun bindData(category: String, width: Int, height: Int, coverUrl: String?, videoUrl: String) {
        this.category = category
        this.videoUrl = videoUrl
        BindingAdapters.setImageUrl(cover, coverUrl)

        //如果该视频的宽度小于高度,则高斯模糊背景图显示出来
        if (width < height) {
            BindingAdapters.setBlurImageUrl(blur_backgroud, coverUrl, 10)
            blur_backgroud.visibility = View.VISIBLE
        } else {
            blur_backgroud.visibility = View.INVISIBLE
        }
        setSize(width, height)
    }

    private fun setSize(width: Int, height: Int) {
        //这里主要是做视频宽大与高,或者高大于宽时  视频的等比缩放
        val maxWidth = PixUtils.getScreenWidth()
        val maxHeight = maxWidth

        val layoutWidth = maxWidth
        val layoutHeight: Int

        val coverWidht : Int
        val coverHeight : Int

        if (width >= height) {
            coverWidht = maxWidth
            layoutHeight = (height / (width * 1.0f / maxWidth)).toInt()
            coverHeight = layoutHeight
        } else {
            layoutHeight = maxHeight
            coverHeight = maxHeight
            coverWidht = (width / (height * 1.0f / maxHeight)).toInt()
        }
        val params = layoutParams
        params.width = layoutWidth
        params.height = layoutHeight
        layoutParams = params

        val blurParams = blur_backgroud.layoutParams
        blurParams.width = layoutWidth
        blurParams.height = layoutHeight
        blur_backgroud.layoutParams = blurParams

        val coverParams = cover.layoutParams as FrameLayout.LayoutParams
        coverParams.width = coverWidht
        coverParams.height = coverHeight
        coverParams.gravity = Gravity.CENTER
        cover.layoutParams = coverParams

        val playBtnParams = play_btn.layoutParams as FrameLayout.LayoutParams
        playBtnParams.gravity = Gravity.CENTER
        play_btn.layoutParams = playBtnParams
    }

    override fun getOwner(): ViewGroup {
        return this
    }

    override fun onActive() {
        //视频播放,或恢复播放

        //通过该View所在页面的mCategory(比如首页列表tab_all,沙发tab的tab_video,标签帖子聚合的tag_feed) 字段，
        //取出管理该页面的Exoplayer播放器，ExoplayerView播放View,控制器对象PageListPlay
        val pageListPlay = PageListPlayManager.get(category)
        val playerView = pageListPlay.playerView
        val controlView = pageListPlay.controlView
        val exoPlayer = pageListPlay.exoPlayer
        if (playerView == null) return

        //此处需要主动调用一次 switchPlayerView，把播放器Exoplayer和展示视频画面的View ExoplayerView相关联
        //为什么呢？因为在列表页点击视频Item跳转到视频详情页的时候，详情页会复用列表页的播放器Exoplayer，然后和新创建的展示视频画面的View ExoplayerView相关联，达到视频无缝续播的效果
        //如果 再次返回列表页，则需要再次把播放器和ExoplayerView相关联
//        pageListPlay.switchPlayerView(playerView, true);
        val parent = playerView.parent
        if (parent != this) {

            //把展示视频画面的View添加到ItemView的容器上
            (parent as ViewGroup?)?.removeView(playerView)
            //还应该暂停掉列表上正在播放的那个
            (parent as ListPlayerView?)?.inActive()

            this.addView(playerView, 1, cover.layoutParams)
        }

        val ctrlParent = controlView?.parent
        if (ctrlParent != this) {
            //把视频控制器 添加到ItemView的容器上
            (ctrlParent as ViewGroup?)?.removeView(controlView)
            val params = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            params.gravity = Gravity.BOTTOM
            this.addView(controlView, params)
        }

        //如果是同一个视频资源,则不需要从重新创建mediaSource。
        //但需要onPlayerStateChanged 否则不会触发onPlayerStateChanged()
        if (TextUtils.equals(pageListPlay.playUrl, videoUrl)) {
            onPlayerStateChanged(true, Player.STATE_READY)
        } else {
            val mediaSource = PageListPlayManager.createMediaSource(videoUrl)
            exoPlayer?.prepare(mediaSource)
            exoPlayer?.repeatMode = Player.REPEAT_MODE_ONE
            pageListPlay.playUrl = videoUrl
        }
        //增加监听，通过回调展示ui
        controlView?.show()
        controlView?.addVisibilityListener(this)
        exoPlayer?.addListener(this)
        exoPlayer?.playWhenReady = true
//        cover?.visibility = View.GONE
//        play_btn?.visibility = View.VISIBLE
//        play_btn?.setImageResource(R.drawable.icon_video_pause)
    }

    override fun inActive() {
        //暂停视频的播放并让封面图和 开始播放按钮 显示出来
        val pageListPlay = PageListPlayManager.get(category)
//        if (pageListPlay.exoPlayer == null || pageListPlay.controlView == null) return
        pageListPlay.exoPlayer?.playWhenReady = false
        //暂停时取消监听
        pageListPlay.controlView?.removeVisibilityListener(this)
        pageListPlay.exoPlayer?.removeListener(this)
        cover?.visibility = View.VISIBLE
        play_btn?.visibility = View.VISIBLE
        play_btn?.setImageResource(R.drawable.icon_video_play)
    }

    override fun isPlaying(): Boolean {
        return isPlaying
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        isPlaying = false
        buffer_view?.visibility = View.GONE
        cover?.visibility = View.VISIBLE
        play_btn?.visibility = View.VISIBLE
        play_btn?.setImageResource(R.drawable.icon_video_play)
    }

    override fun onVisibilityChange(visibility: Int) {
        play_btn?.visibility = visibility
        play_btn?.setImageResource(if (isPlaying()) R.drawable.icon_video_pause else R.drawable.icon_video_play)
    }

    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
        //监听视频播放的状态
        val exoPlayer = PageListPlayManager.get(category).exoPlayer
        if (playbackState == Player.STATE_READY && playWhenReady && exoPlayer?.bufferedPosition?.toInt() != 0) {
            cover?.visibility = View.GONE
            buffer_view?.visibility = View.GONE
        } else if (playbackState == Player.STATE_BUFFERING) {
            buffer_view?.visibility = View.VISIBLE
        }
        isPlaying = playbackState == Player.STATE_READY && playWhenReady && exoPlayer?.bufferedPosition?.toInt() != 0
        play_btn?.setImageResource(if (isPlaying) R.drawable.icon_video_pause else R.drawable.icon_video_play)
    }

    fun getPlayController() =  PageListPlayManager.get(category).controlView
}