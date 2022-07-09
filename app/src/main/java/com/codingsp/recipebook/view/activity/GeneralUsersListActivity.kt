package com.codingsp.recipebook.view.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.ViewModelProvider
import androidx.paging.PagingConfig
import androidx.recyclerview.widget.LinearLayoutManager
import com.codingsp.recipebook.R
import com.codingsp.recipebook.adapters.UserListAdapterInterface
import com.codingsp.recipebook.adapters.UsersListAdapter
import com.codingsp.recipebook.databinding.ActivityGeneralUsersListBinding
import com.codingsp.recipebook.model.User
import com.codingsp.recipebook.utils.Constants
import com.codingsp.recipebook.viewmodel.activtityViewmodel.GeneralUsersListActivityViewModel
import com.firebase.ui.firestore.paging.FirestorePagingOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class GeneralUsersListActivity : AppCompatActivity(), UserListAdapterInterface {
    private lateinit var binding: ActivityGeneralUsersListBinding
    private lateinit var viewModel: GeneralUsersListActivityViewModel
    private lateinit var db: FirebaseFirestore
    private var currentUser: User? = null
    private lateinit var adapter: UsersListAdapter
    private var list: ArrayList<String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGeneralUsersListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        viewModel = ViewModelProvider(this)[GeneralUsersListActivityViewModel::class.java]
        db = FirebaseFirestore.getInstance()
        viewModel.getCurrentUser()
        viewModel.currentUser.observe(this) {
            currentUser = it
        }

        if (intent.hasExtra(Constants.INTENT_FOLLOWERS_FROM_SPECIFIC_USER_TO_GENERAL_USERS_LIST_ACTIVITY)) {
            list =
                intent.getStringArrayListExtra(Constants.INTENT_FOLLOWERS_FROM_SPECIFIC_USER_TO_GENERAL_USERS_LIST_ACTIVITY)
            list?.let {
                if (it.isNotEmpty()) setUpRecyclerView(it)
            }
        }

        if (intent.hasExtra(Constants.INTENT_FOLLOWING_FROM_SPECIFIC_USER_TO_GENERAL_USERS_LIST_ACTIVITY)) {
            list =
                intent.getStringArrayListExtra(Constants.INTENT_FOLLOWING_FROM_SPECIFIC_USER_TO_GENERAL_USERS_LIST_ACTIVITY)
            list?.let {
                if (it.isNotEmpty()) setUpRecyclerView(it)
            }
        }

        if (intent.hasExtra(Constants.INTENT_FOLLOWERS_FROM_PROFILE_FRAGMENT_TO_GENERAL_USERS_LIST_ACTIVITY)) {
            list =
                intent.getStringArrayListExtra(Constants.INTENT_FOLLOWERS_FROM_PROFILE_FRAGMENT_TO_GENERAL_USERS_LIST_ACTIVITY)
            list?.let {
                if (it.isNotEmpty()) setUpRecyclerView(it)
            }
        }

        if (intent.hasExtra(Constants.INTENT_FOLLOWING_FROM_PROFILE_FRAGMENT_TO_GENERAL_USERS_LIST_ACTIVITY)) {
            list =
                intent.getStringArrayListExtra(Constants.INTENT_FOLLOWING_FROM_PROFILE_FRAGMENT_TO_GENERAL_USERS_LIST_ACTIVITY)
            list?.let {
                if (it.isNotEmpty()) setUpRecyclerView(it)
            }
        }
        if (intent.hasExtra(Constants.INTENT_TO_GENERAL_USER_LIST)) {
            list = intent.getStringArrayListExtra(Constants.INTENT_TO_GENERAL_USER_LIST)
            list?.let {
                if (it.isNotEmpty()) setUpRecyclerView(it)
            }
        }
    }

    private fun setUpRecyclerView(userList: ArrayList<String>) {
        val query = db.collection("Users")
            .whereIn("userUUID", userList)
            .orderBy("userName", Query.Direction.ASCENDING)

        val config = PagingConfig(20, 10, false)
        val options = FirestorePagingOptions.Builder<User>()
            .setLifecycleOwner(this)
            .setQuery(query, config, User::class.java)
            .build()

        adapter = UsersListAdapter(this, options, this)
        binding.rvUsersList.layoutManager = LinearLayoutManager(this)
        binding.rvUsersList.adapter = adapter

    }

    override fun onFollowClicked(userId: String, followers: ArrayList<String>) {
        currentUser?.let {
            if (followers.contains(it.userUUID)) {
                followers.remove(it.userUUID)
                it.following.remove(userId)
                Log.i("INCONTAINSVIEW", followers.toString() + it.following.toString())
                viewModel.updateUsersFollowersData(userId, followers)
                viewModel.updateUsersFollowingData(it.userUUID, it.following)
            } else {
                followers.add(it.userUUID)
                it.following.add(userId)
                Log.i("INCONTAINSVIEWNOT", followers.toString() + it.following.toString())
                viewModel.updateUsersFollowersData(userId, followers)
                viewModel.updateUsersFollowingData(it.userUUID, it.following)
            }
        }
    }
}