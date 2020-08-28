package com.mooc.libnetwork.cache

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

object CacheManager {
    fun <T> save(key: String, body: T) {
        val cache = Cache(key)
        cache.data = toByteArray(body)

        CacheDatabase.get().getCache().save(cache)
    }

    fun getCache(key: String) : Any? {
        val cache = CacheDatabase.get().getCache().getCache(key)
        cache?.data?.also {
            return toObject(it)
        }
        return null
    }

    private fun toObject(data: ByteArray): Any? {
        ObjectInputStream(ByteArrayInputStream(data)).use {
            return it.readObject()
        }
    }

    fun <T> toByteArray(body: T): ByteArray? {
        ByteArrayOutputStream().use {baos ->
            ObjectOutputStream(baos).use {
                it.writeObject(body)
                it.flush()
                return baos.toByteArray()
            }
        }
    }
}
