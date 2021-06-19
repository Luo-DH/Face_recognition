package com.luo.base

import com.luo.base.db.dao.FaceDao
import com.luo.base.db.database.FaceDatabase

/**
 * @author: Luo-DH
 * @date: 2021/6/19
 */
object Test {

    fun getDao(): FaceDao =
        FaceDatabase.getDatabase().faceDao()

}
