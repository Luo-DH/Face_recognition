package com.luo.base.db.converter

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * @author: Luo-DH
 * @date: 2021/6/19
 */
class FeatureConverter {

    @TypeConverter
    fun stringToObject(value: String): List<Float> {
        val listType = object : TypeToken<List<Float>>() {

        }.type
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    fun objectToString(list: List<Float>): String {
        val gson = Gson()
        return gson.toJson(list)
    }
}
