package com.codingsp.recipebook.repositories

import com.codingsp.recipebook.daos.RecipeDao
import com.codingsp.recipebook.model.Comment
import com.codingsp.recipebook.model.Recipe
import com.codingsp.recipebook.utils.Resource
import com.google.firebase.firestore.DocumentSnapshot
import java.util.ArrayList

class RecipeRepository {
    private val recipeDao = RecipeDao()
    suspend fun addRecipeToDB(recipe: Recipe): Resource<Boolean> {
        return recipeDao.addRecipeToDB(recipe)
    }

    suspend fun getRecipeById(recipeId: String): Recipe? {
        return recipeDao.getRecipeById(recipeId)
    }

    suspend fun updateRecipesLikesData(recipeId: String, likedBy: ArrayList<String>) {
        recipeDao.updateRecipeLikeData(recipeId, likedBy)
    }

    suspend fun addCommentToRecipe(comment: Comment, recipeId: String) {
        recipeDao.addCommentToRecipe(comment, recipeId)
    }

    suspend fun getRecipesList(
        lastDocumentSnapshot: DocumentSnapshot?,
        following: ArrayList<String>
    ): Pair<ArrayList<Recipe>, DocumentSnapshot?> {
        return recipeDao.getRecipeList(lastDocumentSnapshot, following)
    }


}