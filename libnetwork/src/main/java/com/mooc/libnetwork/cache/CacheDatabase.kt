package com.mooc.libnetwork.cache

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.mooc.libcommon.AppGlobals


@Database(entities = [Cache::class], version = 1, exportSchema = true)
abstract class CacheDatabase : RoomDatabase(){

    companion object {
        //创建一个内存数据库
        //但是这种数据库的数据只存在于内存中，也就是进程被杀之后，数据随之丢失
        //Room.inMemoryDatabaseBuilder()
        private val database : CacheDatabase =
            Room.databaseBuilder(AppGlobals.getApplication(), CacheDatabase::class.java, "jet_pack_demo_cache")
                //是否允许在主线程进行查询
                .allowMainThreadQueries()
                //数据库创建和打开后的回调
                //.addCallback()
                //设置查询的线程池
                //.setQueryExecutor()
                //.openHelperFactory()
                //room的日志模式
                //.setJournalMode()
                //数据库升级异常之后的回滚
                //.fallbackToDestructiveMigration()
                //数据库升级异常后根据指定版本进行回滚
                //.fallbackToDestructiveMigrationFrom()
//                 .addMigrations(CacheDatabase.sMigration)
                .build()

        fun get() : CacheDatabase {
            return database
        }

//        var sMigration : Migration = object : Migration(1, 3) {
//            override fun migrate(database: SupportSQLiteDatabase) {
//                database.run {
//                    execSQL("alter table teacher rename to student")
//                    execSQL("alter table teacher add column teacher_age INTEGER NOT NULL default 0")
//                }
//            }
//        }
    }

    abstract fun getCache() : CacheDao
}