package com.codingsp.recipebook.daos

import androidx.core.net.toUri
import com.codingsp.recipebook.model.Comment
import com.codingsp.recipebook.model.Recipe
import com.codingsp.recipebook.repositories.UserRepository
import com.codingsp.recipebook.utils.Resource
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.tasks.await
import java.lang.Exception

class RecipeDao {
    private val auth = Firebase.auth
    private val db = Firebase.firestore
    private val collection = db.collection("Recipes")
    private val storageRef = Firebase.storage.reference
    private val userDao = UserDao()
    private val userRepository = UserRepository()
    suspend fun addRecipeToDB(recipe: Recipe): Resource<Boolean> {
        if (auth.currentUser != null) {
            return addRecipeImageToDBStorage(recipe)
        }
        return Resource.Error("User Not Found")
    }

    private suspend fun addRecipeImageToDBStorage(recipe: Recipe): Resource<Boolean> {
        val listUrl: ArrayList<String> = arrayListOf()
        val childRef = storageRef.child(auth.currentUser!!.uid)
        return try {
            for (i in recipe.recipeImage) {
                val imageRef = childRef.child(
                    i.substringAfterLast('/') + System.currentTimeMillis().toString()
                )
                imageRef.putFile(i.toUri()).await()
                val url = imageRef.downloadUrl.await()
                listUrl.add(url.toString())
            }
            recipe.recipeImage = listUrl
            addRecipeToFirestore(recipe)
        } catch (e: Exception) {
            Resource.Error(e.message +"")
        }
    }

    private suspend fun addRecipeToFirestore(recipe: Recipe):Resource<Boolean> {
        return try {
            val docRef = collection.document()
            recipe.recipeId = docRef.id
            docRef.set(recipe).await()
            return addRecipeToUserData(docRef.id)
        } catch (e: Exception) {
            Resource.Error(e.message +"")
        }
    }

    private suspend fun addRecipeToUserData(id: String):Resource<Boolean> {
        val user = userRepository.getCurrentUser()
        if (user != null) {
            user.recipeList.add(id)
            return userDao.updateCurrentUserData(user)
        }
        return Resource.Error("User Not Found")
    }

    suspend fun getRecipeById(recipeId: String): Recipe? {
        var recipe: Recipe? = null
        try {
            recipe = collection.document(recipeId).get().await().toObject(Recipe::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return recipe
    }

    suspend fun updateRecipeLikeData(recipeId: String, likedBy: java.util.ArrayList<String>) {
        try {
            collection.document(recipeId).update("likedBy", likedBy).await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun addCommentToRecipe(comment: Comment, recipeId: String) {
        try {
            val docRef = collection.document(recipeId).collection("Comments").document()
            comment.commentId = docRef.id
            docRef.set(comment).await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun getRecipeList(
        lastDocumentSnapshot: DocumentSnapshot?,
        following: ArrayList<String>
    ): Pair<ArrayList<Recipe>, DocumentSnapshot?> {
        val query = if (lastDocumentSnapshot == null) {
            collection.whereIn("createdBy", following).orderBy("createdAt").limit(10)
        } else {
            collection.whereIn("createdBy", following).orderBy("createdAt")
                .startAfter(lastDocumentSnapshot).limit(10)
        }
        try {
            val docRef = query.get().await()
            val recipeList = docRef.toObjects(Recipe::class.java) as ArrayList<Recipe>
            return if (docRef.documents.size > 0) {
                val newLastDocumentSnapShot = docRef.documents[docRef.documents.size - 1]
                Pair(recipeList, newLastDocumentSnapShot)
            } else {
                Pair(recipeList, lastDocumentSnapshot)
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
        return Pair(arrayListOf(), lastDocumentSnapshot)
    }
}