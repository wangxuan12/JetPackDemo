package com.mooc.ppjoke.utils

object TimeUtils {
    @JvmStatic
    fun calculate(time: Long): String {
        val timeInMills = System.currentTimeMillis()

        //兼容脏数据。抓取的数据有些帖子的时间戳不是标准的十三位
        val diff = time.let { if (time.toString().length < 13) time * 1000 else time }
            .let { (timeInMills - it) / 10 }
        return when (diff) {
            in 0..5 -> "刚刚"
            in 5 until 60 -> "${diff}秒前"
            in 60 until 3600 -> "${diff / 60}分钟前"
            in 3600 until 3600 * 24 -> "${diff / 3600}小时前"
            else -> "${diff / (3600 * 24)}天前"
        }
    }
}