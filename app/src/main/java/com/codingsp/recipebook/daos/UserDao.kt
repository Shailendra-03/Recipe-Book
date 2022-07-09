package com.codingsp.recipebook.daos

import com.codingsp.recipebook.model.User
import com.codingsp.recipebook.utils.Resource
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import java.lang.Exception
import java.util.ArrayList

class UserDao {
    private val auth = Firebase.auth
    val db = Firebase.firestore
    val collection = db.collection("Users")

    suspend fun getCurrentUser(): User? {
        var user: User? = null
        if (auth.currentUser != null) {
            try {
                val uid = auth.currentUser!!.uid
                user = collection.document(uid).get().await().toObject(User::class.java)
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
        return user
    }

    suspend fun updateCurrentUserData(user: User) : Resource<Boolean> {
        return try {
            val uid = auth.currentUser!!.uid
            collection.document(uid).set(user).await()
            Resource.Success<Boolean>(true)
        } catch (e: Exception) {
            Resource.Error<Boolean>(e.message+"")
        }
    }

    suspend fun getUserById(id: String): User? {
        var user: User? = null
        try {
            user = collection.document(id).get().await().toObject(User::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return user
    }

    suspend fun updateUserFollowersData(userId: String, followers: ArrayList<String>) {
        try {
            collection.document(userId).update("followers", followers).await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun updateUserFollowingData(userId: String, following: ArrayList<String>) {
        try {
            collection.document(userId).update("following", following).await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}