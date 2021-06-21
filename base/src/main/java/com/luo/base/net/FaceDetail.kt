package com.luo.base.net


import com.google.gson.annotations.SerializedName

data class FaceDetail(
    @SerializedName("hashcode")
    val hashcode: String,
    @SerializedName("id")
    val id: Int,
    @SerializedName("imgUrl")
    val imgUrl: String,
    @SerializedName("name")
    val name: String
)