package com.codingsp.recipebook.adapters

import android.content.Context
import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.codingsp.recipebook.R
import com.codingsp.recipebook.databinding.ItemVpDishImagesBinding

class DishImagesViewPagerAdapter(
    private val context: Context,
    private var imagesList: ArrayList<String> = arrayListOf()
) :
    RecyclerView.Adapter<DishImagesViewPagerAdapter.ViewPagerViewHolder>() {

    inner class ViewPagerViewHolder(itemView: ItemVpDishImagesBinding) :
        RecyclerView.ViewHolder(itemView.root) {
        val ivDish = itemView.ivDishImage
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewPagerViewHolder {
        val binding =
            ItemVpDishImagesBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewPagerViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewPagerViewHolder, position: Int) {
        val currentImage = imagesList[position].toUri()
        Glide.with(context)
            .load(currentImage)
            .centerCrop()
            .error(R.drawable.ic_add_post)
            .into(holder.ivDish)
    }

    override fun getItemCount(): Int {
        return imagesList.size
    }

    fun setList(list: ArrayList<String>) {
        Log.i("InAdapter", list.toString())
        imagesList = list
        notifyDataSetChanged()
    }
}