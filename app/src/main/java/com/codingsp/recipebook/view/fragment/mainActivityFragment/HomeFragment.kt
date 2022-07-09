package com.codingsp.recipebook.view.fragment.mainActivityFragment

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.codingsp.recipebook.R
import com.codingsp.recipebook.adapters.RecipeHomeAdapterInterface
import com.codingsp.recipebook.adapters.RecipesHomeFragmentAdapter
import com.codingsp.recipebook.adapters.StoryAdapter
import com.codingsp.recipebook.databinding.FragmentHomeBinding
import com.codingsp.recipebook.interfaces.DataProviderFromActivity
import com.codingsp.recipebook.model.Notification
import com.codingsp.recipebook.model.Recipe
import com.codingsp.recipebook.model.User
import com.codingsp.recipebook.model.UserStories
import com.codingsp.recipebook.utils.Constants
import com.codingsp.recipebook.utils.GlideApp
import com.codingsp.recipebook.view.activity.GeneralUsersListActivity
import com.codingsp.recipebook.view.activity.RecipesDetailActivity
import com.codingsp.recipebook.view.activity.UserDetailsActivity
import com.codingsp.recipebook.viewmodel.fragmentViewModel.HomeViewModel
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class HomeFragment : Fragment(), View.OnClickListener, RecipeHomeAdapterInterface {

    private lateinit var homeViewModel: HomeViewModel
    private lateinit var binding: FragmentHomeBinding
    private lateinit var db: FirebaseFirestore
    private var user: User? = null
    private lateinit var recipeList: ArrayList<Recipe>
    private lateinit var adapter: RecipesHomeFragmentAdapter
    private var userStories: ArrayList<UserStories>? = null
    private lateinit var storyAdapter: StoryAdapter

    private val getStatusLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.let {
                    homeViewModel.getImagesAndAddToUserStories(it)
                }
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        homeViewModel = ViewModelProvider(this)[HomeViewModel::class.java]
        binding = FragmentHomeBinding.inflate(inflater, container, false)

        subscribeObservers()

//        if(requireActivity().intent.hasExtra(Constants.USER_DETAILS_FROM_LOGIN_ACTIVITY_TO_MAIN_ACTIVITY)){
//            user = requireActivity().intent.getParcelableExtra(Constants.USER_DETAILS_FROM_LOGIN_ACTIVITY_TO_MAIN_ACTIVITY)
//        }

        db = FirebaseFirestore.getInstance()
        user = (requireActivity() as DataProviderFromActivity).getUserFromActivity()
        Log.i("InMainActivity",user.toString())
        recipeList = arrayListOf()
        adapter = RecipesHomeFragmentAdapter(this, this)
        binding.rvRecipesHome.layoutManager = LinearLayoutManager(requireContext())
        binding.rvRecipesHome.adapter = adapter

        storyAdapter = StoryAdapter(this)
        binding.rvStories.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, true)
        binding.rvStories.adapter = storyAdapter

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.ivCurrentUserStory.setOnClickListener(this)

        user?.let {
            if(it.following.size > 0) homeViewModel.getStories(it.following)
        }

        binding.rvRecipesHome.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val lm = binding.rvRecipesHome.layoutManager as LinearLayoutManager?
                if (lm != null && lm.findLastCompletelyVisibleItemPosition() == recipeList.size - 1) {
                    loadNextBatch()
                }
            }
        })

        loadNextBatch()

        homeViewModel.recipeList.observe(viewLifecycleOwner) {
            adapter.addRecipeItemsToList(it)
        }

        homeViewModel.userStories.observe(viewLifecycleOwner) {
            userStories = it
            storyAdapter.setStoriesData(it)
        }
    }

    private fun loadNextBatch() {
        user?.let {
            Log.i("InLoadNextBatch",it.toString())
            GlideApp.with(this).load(it.userImageUrl).error(R.drawable.ic_default_user_image)
                .into(binding.ivCurrentUserStory)
            if (it.following.size > 0) homeViewModel.getRecipeList(it.following)
        }
    }

    override fun onClick(view: View?) {
        when (view) {
            binding.ivCurrentUserStory -> {
                val intent = Intent(Intent.ACTION_GET_CONTENT)
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                intent.type = "image/*"
                getStatusLauncher.launch(intent)
            }
        }
    }

    override fun onRecipeOwnerInfoClicked(recipeOwnerId: String) {
        val intent = Intent(requireContext(), UserDetailsActivity::class.java)
        intent.putExtra(Constants.INTENT_USER_DETAILS_BY_USERID, recipeOwnerId)
        startActivity(intent)
    }

    override fun onRecipeTitleAndDescriptionClick(recipeId: String) {
        val intent = Intent(requireContext(), RecipesDetailActivity::class.java)
        intent.putExtra(Constants.INTENT_RECIPE_BY_RECIPEID, recipeId)
        startActivity(intent)
    }

    override fun onLikeClicked(recipeId: String, likedBy: ArrayList<String>) {
        homeViewModel.updateRecipesLikesData(recipeId, likedBy)
    }

    override fun onLikesCountClicked(likedBy: ArrayList<String>) {
        val intent = Intent(requireContext(), GeneralUsersListActivity::class.java)
        intent.putStringArrayListExtra(Constants.INTENT_TO_GENERAL_USER_LIST, likedBy)
        startActivity(intent)
    }

    override fun onCommentClicked(recipeId: String) {
        val intent = Intent(requireContext(), RecipesDetailActivity::class.java)
        intent.putExtra(Constants.INTENT_RECIPE_BY_RECIPEID, recipeId)
        startActivity(intent)
    }

    override fun sendNotification(notification: Notification, notificationToId: String) {
        homeViewModel.sendNotification(notification, notificationToId)
    }
    private fun subscribeObservers() {
        lifecycleScope.launchWhenStarted {
            homeViewModel.message.collectLatest {
                Snackbar.make(binding.root,it,Snackbar.LENGTH_LONG).show()
            }
        }
    }

}


