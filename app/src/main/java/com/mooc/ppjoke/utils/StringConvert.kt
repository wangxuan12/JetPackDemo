package com.mooc.ppjoke.utils

object StringConvert {
    @JvmStatic
    fun convertFeedUgc(count: Int): String =
        if (count < 10000) count.toString() else "${count / 10000}万"
}