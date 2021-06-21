package com.luo.down.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
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
        TODO("Not yet implemented")
    }

    override fun getItemCount(): Int {
        TODO("Not yet implemented")
    }

}