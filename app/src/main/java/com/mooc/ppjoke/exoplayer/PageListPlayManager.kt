package com.mooc.ppjoke.exoplayer

import android.net.Uri
import com.google.android.exoplayer2.database.ExoDatabaseProvider
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory
import com.google.android.exoplayer2.upstream.FileDataSource
import com.google.android.exoplayer2.upstream.cache.*
import com.google.android.exoplayer2.util.Util
import com.mooc.libcommon.global.AppGlobals

/**
 * 能适应多个页面视频播放的 播放器管理者
 * 每个页面一个播放器
 * 方便管理每个页面的暂停/恢复操作
 */
object PageListPlayManager {
    private val pageListPlayMap = mutableMapOf<String, PageListPlay>()
    fun get(pageName : String) : PageListPlay {
        var pageListPlay = pageListPlayMap[pageName]
        if (pageListPlay == null) {
            pageListPlay = PageListPlay()
            pageListPlayMap[pageName] = pageListPlay
        }
        return pageListPlay
    }

    private val mediaSourceFactory: ProgressiveMediaSource.Factory
    fun createMediaSource(url: String): ProgressiveMediaSource = mediaSourceFactory.createMediaSource(Uri.parse(url))

    init {
        val application = AppGlobals.getApplication()
        //创建http视频资源如何加载的工厂对象
        val httpDataSourceFactory = DefaultHttpDataSourceFactory(Util.getUserAgent(application, application.packageName))
        //创建缓存，指定缓存位置，和缓存策略,为最近最少使用原则,最大为200m
        val cache = SimpleCache(application.cacheDir, LeastRecentlyUsedCacheEvictor(1024 * 1024 * 200), ExoDatabaseProvider(application))
        //把缓存对象cache和负责缓存数据读取、写入的工厂类CacheDataSinkFactory 相关联
        val cacheDataSinkFactory = CacheDataSinkFactory(cache, Long.MAX_VALUE)

        //创建能够 边播放边缓存的 本地资源加载和http网络数据写入的工厂类
        val cacheDataSourceFactory = CacheDataSourceFactory(
            cache, //缓存写入策略和缓存写入位置的对象
            httpDataSourceFactory, //http视频资源如何加载的工厂对象
            FileDataSource.Factory(), //本地缓存数据如何读取的工厂对象
            cacheDataSinkFactory, //http网络数据如何写入本地缓存的工厂对象
            CacheDataSource.FLAG_BLOCK_ON_CACHE, //加载本地缓存数据进行播放时的策略,如果遇到该文件正在被写入数据,或读取缓存数据发生错误时的策略
            null //缓存数据读取的回调
        )

        //最后 还需要创建一个 MediaSource 媒体资源 加载的工厂类
        //因为由它创建的MediaSource 能够实现边缓冲边播放的效果,
        //如果需要播放hls,m3u8 则需要创建DashMediaSource.Factory()
        mediaSourceFactory = ProgressiveMediaSource.Factory(cacheDataSourceFactory)
    }

    fun release(pakeName: String) = pageListPlayMap.remove(pakeName)?.release()
}