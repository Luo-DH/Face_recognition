package com.luo.base.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.luo.base.db.entity.FaceFeature

/**
 * 人脸数据库相关操作
 *
 * @author: Luo-DH
 * @date: 2021/6/19
 */
@Dao
interface FaceDao {

    @Insert
    fun insertFace(face: FaceFeature): Long

    @Query("select * from FaceFeature")
    fun loadAllFaces(): List<FaceFeature>

}