package com.codingsp.recipebook.view.fragment.recipeDetailsFragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.codingsp.recipebook.databinding.FragmentRecipeInstructionsBinding
import com.codingsp.recipebook.model.Recipe
import com.codingsp.recipebook.utils.Constants

class RecipeInstructionsFragment : Fragment() {
    private lateinit var binding: FragmentRecipeInstructionsBinding
    private var recipe: Recipe? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRecipeInstructionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (requireActivity().intent.hasExtra(Constants.INTENT_VIEW_INSTRUCTIONS)) {
            recipe = requireActivity().intent.getParcelableExtra(Constants.INTENT_VIEW_INSTRUCTIONS)
        }
        recipe?.let {

        }
    }

}