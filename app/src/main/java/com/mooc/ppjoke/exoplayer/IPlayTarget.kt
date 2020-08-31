package com.mooc.ppjoke.exoplayer

import android.view.ViewGroup

interface IPlayTarget {
    fun getOwner(): ViewGroup

    //活跃状态 视频可播放
    fun onActive()

    //非活跃状态，暂停它
    fun inActive()

    fun isPlaying(): Boolean
}