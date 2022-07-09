package com.codingsp.recipebook.view.fragment.recipeDetailsFragment

import android.content.Intent
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import androidx.paging.PagingConfig
import androidx.recyclerview.widget.LinearLayoutManager
import com.codingsp.recipebook.R
import com.codingsp.recipebook.adapters.CommentAdapter
import com.codingsp.recipebook.adapters.DishImagesViewPagerAdapter
import com.codingsp.recipebook.databinding.FragmentRecipeDetailsBinding
import com.codingsp.recipebook.model.Comment
import com.codingsp.recipebook.model.Notification
import com.codingsp.recipebook.model.Recipe
import com.codingsp.recipebook.model.User
import com.codingsp.recipebook.utils.Constants
import com.codingsp.recipebook.utils.GlideApp
import com.codingsp.recipebook.utils.TimeToTimeAgo
import com.codingsp.recipebook.view.activity.UserDetailsActivity
import com.codingsp.recipebook.viewmodel.fragmentViewModel.RecipeDetailsViewModel
import com.firebase.ui.firestore.paging.FirestorePagingOptions
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.ktx.Firebase

class RecipeDetailsFragment : Fragment(), View.OnClickListener {

    private lateinit var binding: FragmentRecipeDetailsBinding
    private lateinit var viewModel: RecipeDetailsViewModel
    private var recipe: Recipe? = null
    private lateinit var auth: FirebaseAuth
    private lateinit var adapter: DishImagesViewPagerAdapter
    private var recipeOwner: User? = null
    private var currentUser: User? = null
    private lateinit var commentQuery: Query
    private lateinit var db: FirebaseFirestore
    private lateinit var commentAdapter: CommentAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRecipeDetailsBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this)[RecipeDetailsViewModel::class.java]
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        if (requireActivity().intent.hasExtra(Constants.INTENT_RECIPE_DETAILS)) {
            try {
                recipe =
                    requireActivity().intent.getParcelableExtra(Constants.INTENT_RECIPE_DETAILS)!!
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        if (requireActivity().intent.hasExtra(Constants.INTENT_RECIPE_BY_RECIPEID)) {
            try {
                val recipeId =
                    requireActivity().intent.getStringExtra(Constants.INTENT_RECIPE_BY_RECIPEID)
                recipeId?.let { viewModel.getRecipeById(it) }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        if (requireActivity().intent.hasExtra(Constants.INTENT_CURRENT_USER_RECIPE_DETAILS)) {
            try {
                recipe =
                    requireActivity().intent.getParcelableExtra(Constants.INTENT_CURRENT_USER_RECIPE_DETAILS)!!
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        setUpCommentsRecyclerView()

        viewModel.getCurrentUser()
        recipe?.let {
            initializeAllViewsWithData(it)
        }

        recipe?.let {
            viewModel.getCurrentRecipeOwner(it.createdBy)
        }

        viewModel.recipe.observe(viewLifecycleOwner) {
            recipe = it
            Log.d("InRecipeDetailsFragment", it.toString())
            recipe?.let { recipeN -> initializeAllViewsWithData(recipeN) }
        }
        viewModel.recipeOwner.observe(viewLifecycleOwner) {
            GlideApp.with(this)
                .load(it.userImageUrl)
                .placeholder(R.drawable.ic_default_user_image)
                .into(binding.ivRecipeOwnerImage)
            binding.tvUserDisplayNameRecipeDetailsFragment.text = it.userDisplayName

            when {
                it.userUUID == auth.currentUser!!.uid -> {
                    binding.tvFollow.visibility = View.GONE
                }
                it.followers.contains(auth.currentUser!!.uid) -> {
                    binding.tvFollow.text = getString(R.string.following)
                    binding.tvFollow.background = ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.sign_in_and_register_button_background
                    )
                    binding.tvFollow.visibility = View.VISIBLE
                }
                else -> {
                    binding.tvFollow.visibility = View.VISIBLE
                }
            }
            recipeOwner = it
        }
        viewModel.currentUser.observe(viewLifecycleOwner) {
            currentUser = it
        }

        binding.tvLiked.setOnClickListener(this)
        binding.ivLiked.setOnClickListener(this)
        binding.llUserInfo.setOnClickListener(this)
        binding.tvFollow.setOnClickListener(this)
        binding.llReviews.setOnClickListener(this)
        binding.btnPostComment.setOnClickListener(this)
    }

    override fun onClick(view: View?) {
        when (view) {
            binding.llUserInfo -> {
                recipeOwner?.let {
                    val intent = Intent(requireContext(), UserDetailsActivity::class.java)
                    intent.putExtra(Constants.INTENT_USER_DETAILS, it)
                    startActivity(intent)
                }
            }

            binding.tvFollow -> {
                recipeOwner?.let { recipeOwnerU ->
                    currentUser?.let { currentU ->
                        updateDataOnFollowClick(recipeOwnerU, currentU)
                    }
                }
            }

            binding.ivLiked -> {
                recipe?.let {
                    updateRecipeDataOnLikeClick(it)
                }
            }

            binding.tvLiked -> {
                recipe?.let {
                    val bundle = Bundle()
                    bundle.putStringArrayList(
                        Constants.ARGUMENT_RECIPE_DETAILS_TO_LIKED_BY_FRAGMENT,
                        it.likedBy
                    )
                    findNavController().navigate(
                        R.id.action_recipeDetailsFragment_to_likedByUsersFragment,
                        bundle
                    )
                }
            }
            binding.llReviews -> {
                binding.llComments.visibility = View.VISIBLE
                binding.llIngredientsAndInstructions.visibility = View.GONE
            }
            binding.btnPostComment -> {
                val commentBody = binding.etAddComment.text.toString()
                if (commentBody.isNotEmpty()) {
                    val currentTime = System.currentTimeMillis().toString()
                    val comment = Comment("", commentBody, currentUser!!.userUUID, currentTime)
                    viewModel.addCommentToRecipe(comment, recipe!!.recipeId)
                    val notification = Notification(
                        auth.uid!!,
                        System.currentTimeMillis().toString(),
                        "comment",
                        recipe!!.recipeId
                    )
                    viewModel.sendNotification(notification, recipeOwner!!.userUUID)
                }
            }
        }
    }

    private fun setUpCommentsRecyclerView() {
        if (recipe == null) return
        commentQuery = db.collection("Recipes")
            .document(recipe!!.recipeId)
            .collection("Comments")
            .orderBy("commentAt", Query.Direction.DESCENDING)

        val config = PagingConfig(10, 10, false)
        val options = FirestorePagingOptions.Builder<Comment>()
            .setLifecycleOwner(this)
            .setQuery(commentQuery, config, Comment::class.java)
            .build()

        commentAdapter = CommentAdapter(this, options)
        binding.rvComments.layoutManager = LinearLayoutManager(requireContext())
        binding.rvComments.adapter = commentAdapter
    }

    private fun initializeAllViewsWithData(it: Recipe) {
        binding.tvRecipeName.text = it.recipeName
        binding.tvRecipeDescription.text = it.recipeDescription
        binding.tvIngredientsData.text = it.ingredients
        binding.tvInstructionsData.text = it.instructions
        if (it.likedBy.contains(auth.currentUser!!.uid)) {
            binding.ivLiked.setImageDrawable(
                ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.ic_liked
                )
            )
        } else {
            binding.ivLiked.setImageDrawable(
                ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.ic_not_liked
                )
            )
        }
        if (it.likedBy.size > 0) {
            binding.tvLikedNumber.text = it.likedBy.size.toString()
            binding.tvLikedNumber.visibility = View.VISIBLE
        }
        adapter = DishImagesViewPagerAdapter(requireContext(), it.recipeImage)
        binding.vpDishImages.adapter = adapter
        TabLayoutMediator(binding.tabLayoutVpDishImages, binding.vpDishImages) { _, _ -> }.attach()
    }

    private fun updateRecipeDataOnLikeClick(it: Recipe) {
        if (it.likedBy.contains(auth.currentUser!!.uid)) {
            binding.ivLiked.setImageDrawable(
                ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.ic_not_liked
                )
            )
            it.likedBy.remove(auth.currentUser!!.uid)
            binding.tvLikedNumber.text = it.likedBy.size.toString()
            viewModel.updateRecipesLikesData(it.recipeId, it.likedBy)
        } else {
            binding.ivLiked.setImageDrawable(
                ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.ic_liked
                )
            )
            it.likedBy.add(auth.currentUser!!.uid)
            binding.tvLikedNumber.text = it.likedBy.size.toString()
            viewModel.updateRecipesLikesData(it.recipeId, it.likedBy)
            val notification = Notification(
                FirebaseAuth.getInstance().uid!!,
                System.currentTimeMillis().toString(),
                "like",
                it.recipeId
            )
            viewModel.sendNotification(notification, it.createdBy)
        }
    }

    private fun updateDataOnFollowClick(recipeOwnerU: User, currentU: User) {
        if (recipeOwnerU.followers.contains(auth.currentUser!!.uid)) {
            recipeOwnerU.followers.remove(auth.currentUser!!.uid)
            currentU.following.remove(recipeOwnerU.userUUID)
            viewModel.updateUsersFollowersData(recipeOwnerU.userUUID, recipeOwnerU.followers)
            viewModel.updateUsersFollowingData(auth.currentUser!!.uid, currentU.following)
            binding.tvFollow.text = getString(R.string.follow)
            binding.tvFollow.background =
                ContextCompat.getDrawable(requireContext(), R.drawable.follow_textview_background)
        } else {
            recipeOwnerU.followers.add(auth.currentUser!!.uid)
            currentU.following.add(recipeOwnerU.userUUID)
            viewModel.updateUsersFollowersData(recipeOwnerU.userUUID, recipeOwnerU.followers)
            viewModel.updateUsersFollowingData(auth.currentUser!!.uid, currentU.following)
            binding.tvFollow.text = getString(R.string.following)
            binding.tvFollow.background = ContextCompat.getDrawable(
                requireContext(),
                R.drawable.sign_in_and_register_button_background
            )
            val notification = Notification(
                FirebaseAuth.getInstance().uid!!,
                System.currentTimeMillis().toString(),
                "follow"
            )
            viewModel.sendNotification(notification, recipeOwnerU.userUUID)
        }
    }
}