package com.codingsp.recipebook.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.codingsp.recipebook.R
import com.codingsp.recipebook.databinding.ItemStoryBinding
import com.codingsp.recipebook.model.User
import com.codingsp.recipebook.model.UserStories
import com.codingsp.recipebook.utils.GlideApp
import com.codingsp.recipebook.utils.TimeToTimeAgo
import com.google.firebase.firestore.FirebaseFirestore
import omari.hamza.storyview.StoryView
import omari.hamza.storyview.callback.StoryClickListeners
import omari.hamza.storyview.model.MyStory
import kotlin.collections.ArrayList


class StoryAdapter(
    private val fragment:Fragment
) :RecyclerView.Adapter<StoryAdapter.MyViewHolder>(){

    private var userStories: ArrayList<UserStories> = arrayListOf()

    class MyViewHolder(item:ItemStoryBinding) : RecyclerView.ViewHolder(item.root) {
        val ivUser = item.ivUserImage
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = ItemStoryBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val model = userStories[position]
        var userLogo = ""
        var userName = ""
        FirebaseFirestore.getInstance().collection("Users").document(model.userId).get().addOnSuccessListener {
            val user = it.toObject(User::class.java)
            userLogo = user?.userImageUrl.toString()
            userName= user?.userName.toString()
            GlideApp.with(fragment)
                .load(user?.userImageUrl)
                .error(R.drawable.ic_default_user_image)
                .into(holder.ivUser)
        }
        holder.itemView.setOnClickListener {

            val stories : java.util.ArrayList<MyStory> = arrayListOf()
            userStories[position].stories.forEach {
                stories.add(MyStory(it.imageUrl))
            }

            StoryView.Builder(fragment.requireActivity().supportFragmentManager)
                .setStoriesList(stories) // Required
                .setStoryDuration(5000) // Default is 2000 Millis (2 Seconds)
                .setTitleLogoUrl(userLogo)
                .setTitleText(userName)
                .setSubtitleText(TimeToTimeAgo().getTimeAgo(model.stories[position].creationTIme.toLong()))
                .setStoryClickListeners(object : StoryClickListeners {
                    override fun onDescriptionClickListener(position: Int) {
                        //your action
                    }

                    override fun onTitleIconClickListener(position: Int) {

                    }
                })
                .build()
                .show()

        }

    }

    override fun getItemCount(): Int {
        return userStories.size
    }

    fun setStoriesData(list: ArrayList<UserStories>) {
        userStories = list
        Log.i("InStoryAdapter",list.toString())
        notifyDataSetChanged()
    }

}