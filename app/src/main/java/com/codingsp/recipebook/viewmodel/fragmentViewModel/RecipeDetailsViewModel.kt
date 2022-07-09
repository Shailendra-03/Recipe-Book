package com.codingsp.recipebook.viewmodel.fragmentViewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codingsp.recipebook.model.Comment
import com.codingsp.recipebook.model.Notification
import com.codingsp.recipebook.model.Recipe
import com.codingsp.recipebook.model.User
import com.codingsp.recipebook.repositories.NotificationRepository
import com.codingsp.recipebook.repositories.RecipeRepository
import com.codingsp.recipebook.repositories.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RecipeDetailsViewModel : ViewModel() {
    private var recipeRepository = RecipeRepository()
    private var userRepository = UserRepository()
    private var notificationRepository = NotificationRepository()
    private var _recipeOwner = MutableLiveData<User>()
    val recipeOwner: LiveData<User> get() = _recipeOwner

    private var _currentUser = MutableLiveData<User>()
    val currentUser: LiveData<User> get() = _currentUser

    private var _recipe = MutableLiveData<Recipe?>()
    val recipe: LiveData<Recipe?> get() = _recipe


    fun updateRecipesLikesData(recipeId: String, likedBy: ArrayList<String>) {
        viewModelScope.launch(Dispatchers.IO) {
            recipeRepository.updateRecipesLikesData(recipeId, likedBy)
        }
    }

    fun getCurrentRecipeOwner(createdBy: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val user = userRepository.getUserById(createdBy)
            withContext(Dispatchers.Main) {
                user?.let {
                    _recipeOwner.value = it
                }
            }
        }
    }

    fun getCurrentUser() {
        viewModelScope.launch(Dispatchers.IO) {
            val user = userRepository.getCurrentUser()
            withContext(Dispatchers.Main) {
                user?.let {
                    _currentUser.value = it
                }
            }
        }
    }

    fun updateUsersFollowersData(userId: String, followers: ArrayList<String>) {
        viewModelScope.launch(Dispatchers.IO) {
            userRepository.updateUserFollowersData(userId, followers)
        }
    }

    fun updateUsersFollowingData(userId: String, following: ArrayList<String>) {
        viewModelScope.launch(Dispatchers.IO) {
            userRepository.updateUserFollowingData(userId, following)
        }
    }

    fun addCommentToRecipe(comment: Comment, recipeId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            recipeRepository.addCommentToRecipe(comment, recipeId)
        }
    }

    fun getRecipeById(recipeId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val temp = recipeRepository.getRecipeById(recipeId)
            withContext(Dispatchers.Main) {
                _recipe.value = temp
            }
        }
    }

    fun sendNotification(notification: Notification, notificationToID: String) {
        viewModelScope.launch(Dispatchers.IO) {
            notificationRepository.addNotification(notification, notificationToID)
        }
    }
}