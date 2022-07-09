package com.codingsp.recipebook.view.fragment.mainActivityFragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.paging.PagingConfig
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.codingsp.recipebook.R
import com.codingsp.recipebook.adapters.RecipeSpecificUserAdapter
import com.codingsp.recipebook.databinding.FragmentProfileBinding
import com.codingsp.recipebook.interfaces.DataProviderFromActivity
import com.codingsp.recipebook.model.Recipe
import com.codingsp.recipebook.model.User
import com.codingsp.recipebook.repositories.RecipeRepository
import com.codingsp.recipebook.repositories.UserRepository
import com.codingsp.recipebook.utils.Constants
import com.codingsp.recipebook.utils.GlideApp
import com.codingsp.recipebook.view.activity.GeneralUsersListActivity
import com.codingsp.recipebook.viewmodel.fragmentViewModel.ProfileViewModel
import com.firebase.ui.firestore.paging.FirestorePagingOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query


class ProfileFragment : Fragment(), View.OnClickListener {

    private lateinit var viewModel: ProfileViewModel
    private lateinit var binding: FragmentProfileBinding
    private lateinit var userRepository: UserRepository
    private lateinit var recipeRepository: RecipeRepository
    private var adapter: RecipeSpecificUserAdapter? = null
    private lateinit var db: FirebaseFirestore
    private var query: Query? = null
    private var currentUser: User? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this)[ProfileViewModel::class.java]
        userRepository = UserRepository()
        recipeRepository = RecipeRepository()
        db = FirebaseFirestore.getInstance()
        viewModel.getCurrentUser()

        val myActivity = requireActivity() as DataProviderFromActivity
        currentUser = myActivity.getUserFromActivity()
        currentUser?.let {
            updateDataInView(it)
            setUpRecipesRecyclerView(it)
        }
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.llFollowers.setOnClickListener(this)
        binding.llFollowing.setOnClickListener(this)

    }

    override fun onStart() {
        super.onStart()
        adapter?.startListening()
    }

    override fun onStop() {
        super.onStop()
        adapter?.stopListening()
    }

    override fun onClick(view: View?) {
        when (view) {
            binding.llFollowers -> {
                currentUser?.let {
                    val intent = Intent(requireContext(), GeneralUsersListActivity::class.java)
                    intent.putExtra(
                        Constants.INTENT_FOLLOWERS_FROM_PROFILE_FRAGMENT_TO_GENERAL_USERS_LIST_ACTIVITY,
                        it.followers
                    )
                    startActivity(intent)
                }
            }
            binding.llFollowing -> {
                currentUser?.let {
                    val intent = Intent(requireContext(), GeneralUsersListActivity::class.java)
                    intent.putExtra(
                        Constants.INTENT_FOLLOWING_FROM_PROFILE_FRAGMENT_TO_GENERAL_USERS_LIST_ACTIVITY,
                        it.following
                    )
                    startActivity(intent)
                }
            }
        }
    }

    private fun setUpRecipesRecyclerView(user: User) {
        if (user.recipeList.isEmpty()) {
            return
        }
        query = db.collection("Recipes")
            .whereIn("recipeId", user.recipeList)
            .orderBy("createdAt", Query.Direction.ASCENDING)

        val config = PagingConfig(20, 10, false)
        val options = FirestorePagingOptions.Builder<Recipe>()
            .setLifecycleOwner(this)
            .setQuery(query!!, config, Recipe::class.java)
            .build()

        adapter = RecipeSpecificUserAdapter(this, options)
        binding.rvPosts.layoutManager = GridLayoutManager(requireContext(), 3)
        binding.rvPosts.adapter = adapter!!
    }

    private fun updateDataInView(user: User) {
        Glide.with(this)
            .load(user.userImageUrl)
            .dontAnimate()
            .error(R.drawable.ic_default_user_image)
            .into(binding.ivProfileImage)

        if (user.userBackgroundImageUrl.isNotEmpty()) {
            GlideApp.with(this)
                .load(user.userBackgroundImageUrl)
                .placeholder(R.drawable.ic_default_user_image)
                .into(binding.ivBackground)
        }

        binding.tvDisplayName.text = user.userDisplayName
        binding.tvPostsNumber.text = user.recipeList.size.toString()
        binding.tvFollowersNumber.text = user.followers.size.toString()
        binding.tvFollowingNumbers.text = user.following.size.toString()
        binding.tvUserBio.text = user.userBio
    }
}
