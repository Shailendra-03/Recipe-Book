package com.codingsp.recipebook.adapters

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.codingsp.recipebook.R
import com.codingsp.recipebook.databinding.ItemUserListBinding
import com.codingsp.recipebook.model.User
import com.codingsp.recipebook.utils.Constants
import com.codingsp.recipebook.utils.GlideApp
import com.codingsp.recipebook.view.activity.UserDetailsActivity
import com.firebase.ui.firestore.paging.FirestorePagingAdapter
import com.firebase.ui.firestore.paging.FirestorePagingOptions
import com.google.firebase.auth.FirebaseAuth

class UsersListAdapter(
    private val context: Context,
    private val options: FirestorePagingOptions<User>,
    private val listener: UserListAdapterInterface
) :
    FirestorePagingAdapter<User, UsersListAdapter.MyViewHolder>(options) {

    private val auth = FirebaseAuth.getInstance()

    inner class MyViewHolder(item: ItemUserListBinding) : RecyclerView.ViewHolder(item.root) {
        val userImage = item.ivUserInList
        val userName = item.tvUserName
        val userDisplayName = item.tvDisplayName
        val tvFollow = item.tvFollow
        val llUsernameAndDisplayName = item.llUserNameAndDisplayName
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding =
            ItemUserListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int, model: User) {
        GlideApp.with(context)
            .load(model.userImageUrl)
            .placeholder(ContextCompat.getDrawable(context, R.drawable.ic_default_user_image))
            .into(holder.userImage)

        holder.userName.text = model.userName
        holder.userDisplayName.text = model.userDisplayName
        if (model.userUUID != auth.currentUser!!.uid) {
            holder.tvFollow.visibility = View.VISIBLE
        }

        if (model.followers.contains(auth.currentUser!!.uid)) {
            holder.tvFollow.text = context.getString(R.string.following)
            holder.tvFollow.background = ContextCompat.getDrawable(
                context,
                R.drawable.sign_in_and_register_button_background
            )
        } else {
            holder.tvFollow.text = context.getString(R.string.follow)
            holder.tvFollow.background =
                ContextCompat.getDrawable(context, R.drawable.follow_textview_background)
        }

        holder.tvFollow.setOnClickListener {
            if (model.followers.contains(auth.currentUser!!.uid)) {
                holder.tvFollow.text = context.getString(R.string.follow)
                holder.tvFollow.background =
                    ContextCompat.getDrawable(context, R.drawable.follow_textview_background)
            } else {
                holder.tvFollow.text = context.getString(R.string.following)
                holder.tvFollow.background = ContextCompat.getDrawable(
                    context,
                    R.drawable.sign_in_and_register_button_background
                )
            }
            listener.onFollowClicked(model.userUUID, model.followers)
        }

        holder.llUsernameAndDisplayName.setOnClickListener {
            Intent(context, UserDetailsActivity::class.java).also {
                it.putExtra(Constants.INTENT_USER_DETAILS_FROM_USER_LIST_ADAPTER, model)
                context.startActivity(it)
            }
        }
    }
}

interface UserListAdapterInterface {
    fun onFollowClicked(userId: String, followers: ArrayList<String>)
}