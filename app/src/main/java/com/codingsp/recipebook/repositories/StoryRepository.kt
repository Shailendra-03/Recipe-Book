package com.codingsp.recipebook.repositories

import com.codingsp.recipebook.daos.StoryDao
import com.codingsp.recipebook.model.Story
import com.codingsp.recipebook.model.UserStories
import com.codingsp.recipebook.utils.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import kotlin.collections.ArrayList

class StoryRepository {

    private val storyDao = StoryDao()

    suspend fun addUserStories(imageUrlList: ArrayList<String>?): Resource<String> {
        val storyList: ArrayList<Story> = arrayListOf()
        imageUrlList?.forEach {
            val story = Story(
                "",
                FirebaseAuth.getInstance().uid!!,
                System.currentTimeMillis().toString(),
                it
            )
            storyList.add(story)
        }

        if (storyList.isNotEmpty()) {
            return storyDao.addStoryToDatabase(storyList)
        }
        return Resource.Error("No Story Selected")
    }

    suspend fun getStories(userIdList: java.util.ArrayList<String>): Resource<ArrayList<UserStories>> {
        val data = storyDao.getStories(userIdList)
        if (data is Resource.Error) return Resource.Error(data.message.toString())
        val storiesList = data.data!!
        storiesList.sortedByDescending { it.creationTIme }
        val hashMap = HashMap<String, ArrayList<Story>>()
        storiesList.forEach {
            if (hashMap[it.createdBy] == null) hashMap[it.createdBy] = arrayListOf()
            hashMap[it.createdBy]!!.add(it)
        }
        val userStoriesList: ArrayList<UserStories> = arrayListOf()
        hashMap.forEach { mapEntry ->
            val id = mapEntry.key
            val list = mapEntry.value
            userStoriesList.add(UserStories(id, list))
        }
        return Resource.Success(userStoriesList)
    }
}