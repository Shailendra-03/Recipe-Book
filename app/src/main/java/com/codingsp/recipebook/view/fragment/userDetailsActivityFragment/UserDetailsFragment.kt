package com.codingsp.recipebook.view.fragment.userDetailsActivityFragment

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
import androidx.paging.PagingConfig
import androidx.recyclerview.widget.GridLayoutManager
import com.codingsp.recipebook.R
import com.codingsp.recipebook.adapters.RecipeSpecificUserAdapter
import com.codingsp.recipebook.databinding.FragmentUserDetailsBinding
import com.codingsp.recipebook.model.Recipe
import com.codingsp.recipebook.model.User
import com.codingsp.recipebook.utils.Constants
import com.codingsp.recipebook.utils.GlideApp
import com.codingsp.recipebook.view.activity.GeneralUsersListActivity
import com.codingsp.recipebook.viewmodel.fragmentViewModel.UserDetailsViewModel
import com.firebase.ui.firestore.paging.FirestorePagingOptions
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.util.ArrayList

class UserDetailsFragment : Fragment(),View.OnClickListener {

    private lateinit var binding:FragmentUserDetailsBinding
    private lateinit var viewModel: UserDetailsViewModel
    private var user:User?=null
    private lateinit var auth:FirebaseAuth
    private var adapter:RecipeSpecificUserAdapter?=null
    private lateinit var db:FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding= FragmentUserDetailsBinding.inflate(inflater,container,false)
        viewModel=ViewModelProvider(this)[UserDetailsViewModel::class.java]
        auth= FirebaseAuth.getInstance()
        db= FirebaseFirestore.getInstance()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if(requireActivity().intent.hasExtra(Constants.INTENT_USER_DETAILS_BY_USERID)){
            try {
                val userId=requireActivity().intent.getStringExtra(Constants.INTENT_USER_DETAILS_BY_USERID)
                userId?.let { viewModel.getUserById(it) }
            }catch (e:Exception){
                e.printStackTrace()
            }
        }


        if(requireActivity().intent.hasExtra(Constants.INTENT_USER_DETAILS_FROM_USER_LIST_ADAPTER)){
            try {
                user=requireActivity().intent.getParcelableExtra(Constants.INTENT_USER_DETAILS_FROM_USER_LIST_ADAPTER)
            }catch (e:Exception){
                e.printStackTrace()
            }
        }
        viewModel.userDetails.observe(viewLifecycleOwner){userDetails->
            userDetails?.let { user=it }
            initializeAllViewsWithUserData(user!!)
        }

        user?.let {
            requireActivity().title = it.userName
            initializeAllViewsWithUserData(it)
        }
        binding.llFollowers.setOnClickListener(this)
        binding.llFollowing.setOnClickListener(this)
        binding.tvFollow.setOnClickListener(this)
    }

    override fun onClick(view: View?) {
        when(view){
            binding.llFollowers->{
                user?.let {
                    val intent= Intent(requireContext(),GeneralUsersListActivity::class.java)
                    intent.putExtra(Constants.INTENT_FOLLOWERS_FROM_SPECIFIC_USER_TO_GENERAL_USERS_LIST_ACTIVITY,it.followers)
                    startActivity(intent)
                }
            }
            binding.llFollowing->{
                user?.let {
                    val intent= Intent(requireContext(),GeneralUsersListActivity::class.java)
                    intent.putExtra(Constants.INTENT_FOLLOWING_FROM_SPECIFIC_USER_TO_GENERAL_USERS_LIST_ACTIVITY,it.following)
                    startActivity(intent)
                }
            }
            binding.tvFollow->{
                return
                TODO("Yet to implement")
            }
        }
    }
    private fun initializeAllViewsWithUserData(user: User) {
        GlideApp.with(this)
            .load(user.userImageUrl)
            .placeholder(R.drawable.ic_default_user_image)
            .into(binding.ivUserProfileImage)

        binding.tvUserDisplayName.text=user.userDisplayName
        binding.tvPostsNumber.text=user.recipeList.size.toString()
        binding.tvFollowersNumber.text=user.followers.size.toString()
        binding.tvFollowingNumbers.text=user.following.size.toString()

        if(user.userBackgroundImageUrl.isNotEmpty()){
            GlideApp.with(this)
                .load(user.userBackgroundImageUrl)
                .placeholder(R.drawable.ic_default_user_image)
                .into(binding.ivUserBackground)
        }
        binding.tvUserBio.text=user.userBio

        auth.currentUser?.let {
            when {
                user.userUUID==it.uid -> {
                    binding.tvFollow.visibility=View.GONE
                }
                user.followers.contains(it.uid) -> {
                    binding.tvFollow.text=getString(R.string.following)
                    binding.tvFollow.background=ContextCompat.getDrawable(requireContext(),R.drawable.sign_in_and_register_button_background)
                    binding.tvFollow.visibility=View.VISIBLE
                }
                else -> {
                    binding.tvFollow.text=getString(R.string.follow)
                    binding.tvFollow.background=ContextCompat.getDrawable(requireContext(),R.drawable.follow_textview_background)
                    binding.tvFollow.visibility=View.VISIBLE
                }
            }
        }
        if(user.recipeList.isNotEmpty()){
            setUpRecyclerView(user.recipeList)
        }
    }

    private fun setUpRecyclerView(recipeList: ArrayList<String>) {
        val query=db.collection("Recipes")
            .whereIn("recipeId",recipeList)
            .orderBy("createdAt",Query.Direction.ASCENDING)

        val config = PagingConfig( 20,10,false)
        val options = FirestorePagingOptions.Builder<Recipe>()
            .setLifecycleOwner(this)
            .setQuery(query, config, Recipe::class.java)
            .build()

        adapter= RecipeSpecificUserAdapter(this,options)
        binding.rvPosts.layoutManager=GridLayoutManager(requireContext(),3)
        binding.rvPosts.adapter=adapter
    }

}