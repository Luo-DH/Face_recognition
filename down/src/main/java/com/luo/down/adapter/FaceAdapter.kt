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
class FaceAdapter : RecyclerView.Adapter<FaceAdapter.FaceViewHolder>() {

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
        val data = faces[position]
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

    override fun getItemCount() = faces.size

    private val diffCallback = object : DiffUtil.ItemCallback<FaceDetail>() {
        override fun areItemsTheSame(oldItem: FaceDetail, newItem: FaceDetail): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: FaceDetail, newItem: FaceDetail): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }

    }


    private val differ = AsyncListDiffer(this, diffCallback)

    var faces: List<FaceDetail>
        get() = differ.currentList
        set(value) {
            Log.d("hello", "${value}: ")
            notifyDataSetChanged()
            return differ.submitList(value)
        }

}