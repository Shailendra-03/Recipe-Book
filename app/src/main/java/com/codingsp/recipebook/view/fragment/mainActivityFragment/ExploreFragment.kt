package com.codingsp.recipebook.view.fragment.mainActivityFragment

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagingConfig
import androidx.recyclerview.widget.GridLayoutManager
import com.codingsp.recipebook.adapters.RandomRecipesAdapter
import com.codingsp.recipebook.databinding.FragmentExploreBinding
import com.codingsp.recipebook.model.Recipe
import com.codingsp.recipebook.model.User
import com.codingsp.recipebook.viewmodel.fragmentViewModel.ExploreViewModel
import com.firebase.ui.firestore.paging.FirestorePagingOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class ExploreFragment : Fragment() {

    private lateinit var viewModel: ExploreViewModel
    private lateinit var binding: FragmentExploreBinding
    private lateinit var query: Query
    private lateinit var db: FirebaseFirestore
    private lateinit var adapter: RandomRecipesAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentExploreBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this)[ExploreViewModel::class.java]
        db = FirebaseFirestore.getInstance()
        setUpRecipesRecyclerView()

        return binding.root
    }

    private fun setUpRecipesRecyclerView() {
        query = db.collection("Recipes")
            .orderBy("createdAt", Query.Direction.ASCENDING)

        val config = PagingConfig(20, 10, false)
        val options = FirestorePagingOptions.Builder<Recipe>()
            .setLifecycleOwner(this)
            .setQuery(query, config, Recipe::class.java)
            .build()
        adapter = RandomRecipesAdapter(this, options)
        binding.rvRecipes.layoutManager = GridLayoutManager(requireContext(), 3)
        binding.rvRecipes.adapter = adapter
    }

    override fun onStart() {
        super.onStart()
        adapter.startListening()
    }

    override fun onStop() {
        super.onStop()
        adapter.stopListening()
    }

}