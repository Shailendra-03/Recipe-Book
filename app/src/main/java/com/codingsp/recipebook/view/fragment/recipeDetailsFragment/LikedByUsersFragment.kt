package com.codingsp.recipebook.view.fragment.recipeDetailsFragment

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagingConfig
import androidx.recyclerview.widget.LinearLayoutManager
import com.codingsp.recipebook.R
import com.codingsp.recipebook.adapters.UserListAdapterInterface
import com.codingsp.recipebook.adapters.UsersListAdapter
import com.codingsp.recipebook.databinding.FragmentLikedByUsersBinding
import com.codingsp.recipebook.model.User
import com.codingsp.recipebook.utils.Constants
import com.codingsp.recipebook.viewmodel.fragmentViewModel.LikedByUsersViewModel
import com.firebase.ui.firestore.paging.FirestorePagingOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlin.collections.ArrayList

class LikedByUsersFragment : Fragment(), UserListAdapterInterface {

    private lateinit var binding: FragmentLikedByUsersBinding
    private lateinit var viewModel: LikedByUsersViewModel
    private var query: Query? = null
    private lateinit var adapter: UsersListAdapter
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private var currentUser: User? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLikedByUsersBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this)[LikedByUsersViewModel::class.java]
        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        viewModel.getCurrentUser()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.currentUser.observe(viewLifecycleOwner) {
            currentUser = it
        }
        val bundle = this.arguments
        bundle?.let {
            val list = it.getStringArrayList(Constants.ARGUMENT_RECIPE_DETAILS_TO_LIKED_BY_FRAGMENT)
            list?.let { arrayList ->
                setUpRecyclerViewAdapter(arrayList)
            }
        }
    }

    private fun setUpRecyclerViewAdapter(list: ArrayList<String>) {
        query = db.collection("Users")
            .whereIn("userUUID", list)
            .orderBy("userName", Query.Direction.ASCENDING)
        val config = PagingConfig(20, 10, false)
        val options = FirestorePagingOptions.Builder<User>()
            .setLifecycleOwner(this)
            .setQuery(query!!, config, User::class.java)
            .build()

        adapter = UsersListAdapter(requireContext(), options, this)
        binding.rvUsersList.layoutManager = LinearLayoutManager(requireContext())
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