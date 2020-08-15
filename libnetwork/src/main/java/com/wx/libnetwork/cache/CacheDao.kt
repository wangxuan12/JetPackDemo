package com.wx.libnetwork.cache

import androidx.room.*

@Dao
interface CacheDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun save(cache: Cache)

    @Query("select * from cache where `key` = :key")
    fun getCache(key : String) : Cache?

    @Query("delete from cache where `key` = :key")
    fun delete(key: String) : Int

    @Delete
    fun delete(vararg cache: Cache)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun update(vararg cache: Cache) : Int
}