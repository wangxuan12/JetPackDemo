package com.wx.libnetwork.cache

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "cache")
class Cache(@PrimaryKey var key: String) : Serializable {
    var data: ByteArray? = null
}