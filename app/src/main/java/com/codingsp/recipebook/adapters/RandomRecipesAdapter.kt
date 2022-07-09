package com.codingsp.recipebook.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.codingsp.recipebook.R
import com.codingsp.recipebook.databinding.ItemRandomRecipesBinding
import com.codingsp.recipebook.model.Recipe
import com.codingsp.recipebook.utils.Constants
import com.codingsp.recipebook.utils.GlideApp
import com.codingsp.recipebook.view.activity.RecipesDetailActivity
import com.firebase.ui.firestore.paging.FirestorePagingAdapter
import com.firebase.ui.firestore.paging.FirestorePagingOptions

class RandomRecipesAdapter(
    private val fragment: Fragment,
    private val options: FirestorePagingOptions<Recipe>
) : FirestorePagingAdapter<Recipe, RandomRecipesAdapter.MyViewHolder>(options) {

    inner class MyViewHolder(item: ItemRandomRecipesBinding) : RecyclerView.ViewHolder(item.root) {
        val ivRandomRecipe = item.ivRandomRecipe
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding =
            ItemRandomRecipesBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int, model: Recipe) {
        val currentRecipeImage = model.recipeImage[0]
        GlideApp.with(fragment)
            .load(currentRecipeImage)
            .placeholder(R.drawable.ic_add)
            .into(holder.ivRandomRecipe)

        holder.itemView.setOnClickListener {
            val intent = Intent(fragment.requireContext(), RecipesDetailActivity::class.java)
            intent.putExtra(Constants.INTENT_RECIPE_DETAILS, model)
            fragment.startActivity(intent)
        }
    }
}