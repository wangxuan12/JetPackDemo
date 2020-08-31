@file:SuppressLint("InflateParams")

package com.mooc.ppjoke.exoplayer

import android.annotation.SuppressLint
import android.view.LayoutInflater
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ui.PlayerControlView
import com.google.android.exoplayer2.ui.PlayerView
import com.mooc.libcommon.global.AppGlobals
import com.mooc.ppjoke.R

class PageListPlay {
    var exoPlayer: SimpleExoPlayer?
    var playerView : PlayerView?
    var controlView : PlayerControlView?
    var playUrl: String? = null

    init {
        val application = AppGlobals.getApplication()
        //创建exoplayer播放器实例
        exoPlayer = SimpleExoPlayer.Builder(application).build()
//        exoPlayer = SimpleExoPlayer.Builder(application, DefaultRenderersFactory(application)) //视频每一这的画面如何渲染,实现默认的实现类
//            .setTrackSelector(DefaultTrackSelector(application)) //视频的音视频轨道如何加载,使用默认的轨道选择器
//            .setLoadControl(DefaultLoadControl()) //视频缓存控制逻辑,使用默认的即可
//            .build()

        //加载布局层级优化之后的能够展示视频画面的View
        playerView = LayoutInflater.from(application).inflate(R.layout.layout_exo_player_view, null) as PlayerView
        //加载布局层级优化之后的视频播放控制器
        controlView = LayoutInflater.from(application).inflate(R.layout.layout_exo_player_controller_view, null) as PlayerControlView

        //把播放器实例 和 playerView，controlView相关联
        //如此视频画面才能正常显示,播放进度条才能自动更新
        playerView?.player = exoPlayer
        controlView?.player = exoPlayer
    }

    fun release() {
        exoPlayer?.playWhenReady = false
        exoPlayer?.stop()
        exoPlayer?.release()
        exoPlayer = null

        playerView?.player = null
        playerView = null

        controlView?.player = null
        controlView = null
    }

//    /**
//     * 切换与播放器exoplayer 绑定的exoplayerView。用于页面切换视频无缝续播的场景
//     */
//    fun switchPlayerView(newPlayerView: PlayerView?, attach: Boolean) {
//        playerView?.player = if (attach) null else exoPlayer
//        newPlayerView?.player = if (attach) exoPlayer else null
//    }
}