package com.luo.base.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.luo.base.db.converter.FeatureConverter

/**
 *  单个人脸信息
 *  - id 自增
 *  - name 名字
 *  - img 压缩图像连接
 *  - fea 特征向量
 *
 * @author: Luo-DH
 * @date: 2021/6/19
 */
@Entity
@TypeConverters(FeatureConverter::class)
data class FaceFeature(
    var name: String,
    var img: String,
    var fea: List<Float>,
    var hashcode: Long
) {
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0

}
