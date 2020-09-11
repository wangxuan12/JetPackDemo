@file:Suppress("UNREACHABLE_CODE")

package com.mooc.libcommon.utils

import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.os.Environment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

object FileUtils {

    fun generateVideoCover(filePath: String): LiveData<String> {
        val liveData = MutableLiveData<String>()
        postIO {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(filePath)

            val frame = retriever.frameAtTime
            if (frame != null) {
                //压缩到200k以下，再存储到本地文件中
                val bytes = compressBitmap(frame, 200)
                val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "${System.currentTimeMillis()}.jpeg")
                file.createNewFile()
                FileOutputStream(file).use {
                    it.write(bytes)
                    liveData.postValue(file.absolutePath)
                }
            } else {
                liveData.postValue(null)
            }
        }
        return liveData
    }


    /**
     * 循环压缩
     */
    private fun compressBitmap(frame: Bitmap, limit: Int): ByteArray? {
        if (limit <= 0) return null
        ByteArrayOutputStream().use {
            var options = 100
            frame.compress(Bitmap.CompressFormat.JPEG, options, it)
            while (it.size() > limit * 1024) {
                it.reset()
                options -= 5
                frame.compress(Bitmap.CompressFormat.JPEG, options, it)
            }
            return it.toByteArray()
        }
        return null
    }
}