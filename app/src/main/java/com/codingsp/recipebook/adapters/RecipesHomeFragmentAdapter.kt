package com.codingsp.recipebook.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.codingsp.recipebook.R
import com.codingsp.recipebook.databinding.ItemRecipesHomeBinding
import com.codingsp.recipebook.model.Notification
import com.codingsp.recipebook.model.Recipe
import com.codingsp.recipebook.utils.GlideApp
import com.codingsp.recipebook.utils.TimeToTimeAgo
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RecipesHomeFragmentAdapter(
    private val fragment: Fragment,
    private val listener: RecipeHomeAdapterInterface
) : RecyclerView.Adapter<RecipesHomeFragmentAdapter.MyViewHolder>() {

    private var list: ArrayList<Recipe> = arrayListOf()

    inner class MyViewHolder(item: ItemRecipesHomeBinding) : RecyclerView.ViewHolder(item.root) {
        val ivRecipeOwnerImage = item.ivRecipeOwnerImage
        val tvRecipeName = item.tvRecipeName
        val vpDishImages = item.vpDishImages
        val tlVpDishImages = item.tabLayoutVpDishImages
        val ivLiked = item.ivLiked
        val tvLiked = item.tvLiked
        val tvReviews = item.tvReviews
        val llUserInfo = item.llUserInfo
        val ivReviews = item.ivReviews
        val tvUserName = item.tvUserName
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding =
            ItemRecipesHomeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val recipe = list[position]
        holder.tvRecipeName.text = recipe.recipeName
        setUpViewPagerAdapter(holder, position)
        if (recipe.likedBy.contains(FirebaseAuth.getInstance().uid)) {
            holder.ivLiked.setImageDrawable(
                ContextCompat.getDrawable(
                    fragment.requireContext(),
                    R.drawable.ic_liked
                )
            )
        } else {
            holder.ivLiked.setImageDrawable(
                ContextCompat.getDrawable(
                    fragment.requireContext(),
                    R.drawable.ic_not_liked
                )
            )
        }
        holder.tvLiked.text = recipe.likedBy.size.toString()

        holder.llUserInfo.setOnClickListener { listener.onRecipeOwnerInfoClicked(recipeOwnerId = recipe.createdBy) }
        holder.itemView.setOnClickListener { listener.onRecipeTitleAndDescriptionClick(recipeId = recipe.recipeId) }
        holder.tvLiked.setOnClickListener { listener.onLikesCountClicked(likedBy = recipe.likedBy) }
        holder.tvReviews.setOnClickListener { listener.onCommentClicked(recipeId = recipe.recipeId) }
        holder.ivReviews.setOnClickListener { listener.onCommentClicked(recipeId = recipe.recipeId) }
        holder.ivLiked.setOnClickListener {
            if (recipe.likedBy.contains(FirebaseAuth.getInstance().uid)) {
                holder.ivLiked.setImageDrawable(
                    ContextCompat.getDrawable(
                        fragment.requireContext(),
                        R.drawable.ic_not_liked
                    )
                )
                recipe.likedBy.remove(FirebaseAuth.getInstance().uid)
                listener.onLikeClicked(recipe.recipeId, recipe.likedBy)
            } else {
                holder.ivLiked.setImageDrawable(
                    ContextCompat.getDrawable(
                        fragment.requireContext(),
                        R.drawable.ic_liked
                    )
                )
                recipe.likedBy.add(FirebaseAuth.getInstance().uid!!)
                listener.onLikeClicked(recipe.recipeId, recipe.likedBy)
                val notification = Notification(
                    FirebaseAuth.getInstance().uid!!,
                    System.currentTimeMillis().toString(),
                    "like",
                    recipe.recipeId
                )
                listener.sendNotification(notification, recipe.createdBy)
            }
            holder.tvLiked.text = recipe.likedBy.size.toString()
        }

        getRecipeOwner(recipe.createdBy, holder)

    }

    private fun getRecipeOwner(createdBy: String, holder: MyViewHolder) {
        FirebaseFirestore.getInstance().collection("Users").document(createdBy).get()
            .addOnSuccessListener {
                val userName = it.data?.get("userName").toString()
                val imageUrl = it.data?.get("userImageUrl").toString()
                holder.tvUserName.text = userName
                GlideApp.with(fragment).load(imageUrl).error(R.drawable.ic_default_user_image)
                    .into(holder.ivRecipeOwnerImage)
            }
    }

    private fun setUpViewPagerAdapter(
        holder: MyViewHolder,
        position: Int
    ) {
        val vpAdapter =
            DishImagesViewPagerAdapter(fragment.requireContext(), list[position].recipeImage)
        holder.vpDishImages.adapter = vpAdapter
        TabLayoutMediator(holder.tlVpDishImages, holder.vpDishImages) { _, _ -> }.attach()
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun addRecipeItemsToList(recipeList: ArrayList<Recipe>) {
        val size = list.size
        list.addAll(list.size, recipeList)
        notifyItemRangeInserted(list.size, (recipeList.size - size))
    }
}


interface RecipeHomeAdapterInterface {
    fun onRecipeOwnerInfoClicked(recipeOwnerId: String)
    fun onRecipeTitleAndDescriptionClick(recipeId: String)
    fun onLikeClicked(recipeId: String, likedBy: ArrayList<String>)
    fun onLikesCountClicked(likedBy: ArrayList<String>)
    fun onCommentClicked(recipeId: String)
    fun sendNotification(notification: Notification, notificationToId: String)
}