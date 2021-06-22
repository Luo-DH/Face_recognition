package com.luo.down.adapter

import android.app.Application
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.luo.base.FaceApplication
import com.luo.base.net.FaceDetail
import com.luo.down.databinding.ItemDownBinding

/**
 * @author: Luo-DH
 * @date: 2021/6/21
 */
class FaceAdapter2(val datas: List<FaceDetail>) : RecyclerView.Adapter<FaceAdapter2.FaceViewHolder>() {

    inner class FaceViewHolder(itemView: ItemDownBinding) : RecyclerView.ViewHolder(itemView.root) {
        val imageView = itemView.itemDownIv
        val name = itemView.downTvName
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FaceViewHolder {
        val holder = FaceViewHolder(
            ItemDownBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
        return holder
    }

    override fun onBindViewHolder(holder: FaceViewHolder, position: Int) {
        Log.d("TAGonResourceReady", "onBindViewHolder: I am here")
        val data = datas[position]
        holder.name.text = data.name
        Glide.with(FaceApplication.context).asBitmap().load(data.imgUrl).into(object : CustomTarget<Bitmap>() {
            override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                holder.imageView.setImageBitmap(resource)
            }

            override fun onLoadCleared(placeholder: Drawable?) {
                TODO("Not yet implemented")
            }

        })

    }

    override fun getItemCount(): Int {
       return datas.size
    }

}