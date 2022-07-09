package com.codingsp.recipebook.daos

import androidx.core.net.toUri
import com.codingsp.recipebook.model.Story
import com.codingsp.recipebook.utils.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.tasks.await

class StoryDao {

    private val collection = Firebase.firestore.collection("Stories")
    private val storageReference =
        Firebase.storage.reference.child(FirebaseAuth.getInstance().uid!!).child("Stories")

    suspend fun addStoryToDatabase(storyList: ArrayList<Story>): Resource<String> {

        try {
            storyList.forEach {
                val imageRef = storageReference.child(
                    it.imageUrl.substringAfterLast("/") + System.currentTimeMillis().toString()
                )
                imageRef.putFile(it.imageUrl.toUri()).await()
                val docRef = collection.document()
                it.imageUrl = imageRef.downloadUrl.await().toString()
                it.storyId = docRef.id
                docRef.set(it).await()
            }

            return Resource.Success("Stories Uploaded Successfully")

        } catch (e: Exception) {
            return Resource.Error(e.message +"")
        }
    }

    suspend fun getStories(userIdList: ArrayList<String>): Resource<ArrayList<Story>> {
        try {
            val stories =  collection.whereIn("createdBy",userIdList).get().await().toObjects(Story::class.java) as ArrayList<Story>
            return Resource.Success(stories)
        } catch (e: Exception) {
            Resource.Error<ArrayList<Story>> (e.message+"")
        }
        return Resource.Success(arrayListOf())
    }
}