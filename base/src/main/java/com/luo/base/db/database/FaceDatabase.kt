package com.luo.base.db.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.luo.base.FaceApplication
import com.luo.base.db.dao.FaceDao
import com.luo.base.db.entity.FaceFeature

/**
 * @author: Luo-DH
 * @date: 2021/6/19
 */
@Database(version = 1, entities = [FaceFeature::class])
abstract class FaceDatabase: RoomDatabase() {

    abstract fun faceDao(): FaceDao

    companion object {
        private var instance: FaceDatabase? = null

        @Synchronized
        fun getDatabase(): FaceDatabase {
            instance?.let {
                return it
            }
            return Room.databaseBuilder(
                FaceApplication.context,
                FaceDatabase::class.java,
                "face_database"
            ).build().apply {
                instance = this
            }
        }
    }

}