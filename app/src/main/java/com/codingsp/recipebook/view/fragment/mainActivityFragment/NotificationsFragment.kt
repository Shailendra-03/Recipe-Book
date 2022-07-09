package com.codingsp.recipebook.view.fragment.mainActivityFragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.codingsp.recipebook.adapters.NotificationAdapter
import com.codingsp.recipebook.adapters.NotificationListener
import com.codingsp.recipebook.databinding.FragmentNotificationsBinding
import com.codingsp.recipebook.model.Notification
import com.codingsp.recipebook.utils.Constants
import com.codingsp.recipebook.view.activity.RecipesDetailActivity
import com.codingsp.recipebook.view.activity.UserDetailsActivity
import com.codingsp.recipebook.view.fragment.recipeDetailsFragment.RecipeDetailsFragment
import com.codingsp.recipebook.viewmodel.fragmentViewModel.NotificationsViewModel
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class NotificationsFragment : Fragment(), NotificationListener {

    private lateinit var notificationsViewModel: NotificationsViewModel
    private lateinit var binding: FragmentNotificationsBinding
    private lateinit var adapter: NotificationAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        notificationsViewModel = ViewModelProvider(this)[NotificationsViewModel::class.java]
        binding = FragmentNotificationsBinding.inflate(inflater, container, false)

        val query = FirebaseFirestore.getInstance()
            .collection("Notifications")
            .document(FirebaseAuth.getInstance().uid!!)
            .collection("Notifications")
            .orderBy("notificationAt")
            .limit(20)

        val options: FirestoreRecyclerOptions<Notification> =
            FirestoreRecyclerOptions.Builder<Notification>()
                .setQuery(query, Notification::class.java)
                .setLifecycleOwner(this)
                .build()

        adapter = NotificationAdapter(this, options, this)
        binding.rvNotifications.layoutManager = LinearLayoutManager(requireContext())
        binding.rvNotifications.adapter = adapter

        return binding.root
    }

    override fun onStart() {
        super.onStart()
        binding.rvNotifications.recycledViewPool.clear()
        adapter.notifyDataSetChanged()
        adapter.startListening()
    }

    override fun onStop() {
        super.onStop()
        adapter.stopListening()
    }

    override fun onNotificationClick(notification: Notification) {
        if (notification.type == "like" || notification.type == "comment") {
            val intent = Intent(requireContext(), RecipesDetailActivity::class.java)
            intent.putExtra(Constants.INTENT_RECIPE_BY_RECIPEID, notification.postId)
            startActivity(intent)
        } else if (notification.type == "follow") {
            val intent = Intent(requireContext(), UserDetailsActivity::class.java)
            intent.putExtra(Constants.INTENT_USER_DETAILS_BY_USERID, notification.notificationBy)
            startActivity(intent)
        }
    }
}