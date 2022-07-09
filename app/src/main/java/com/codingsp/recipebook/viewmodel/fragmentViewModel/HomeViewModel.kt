package com.codingsp.recipebook.viewmodel.fragmentViewModel

import android.content.Intent
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codingsp.recipebook.model.Notification
import com.codingsp.recipebook.model.Recipe
import com.codingsp.recipebook.model.UserStories
import com.codingsp.recipebook.repositories.NotificationRepository
import com.codingsp.recipebook.repositories.RecipeRepository
import com.codingsp.recipebook.repositories.StoryRepository
import com.codingsp.recipebook.utils.Resource
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeViewModel : ViewModel() {

    private val recipeRepository = RecipeRepository()
    private val notificationRepository = NotificationRepository()
    private val storyRepository = StoryRepository()
    private val _recipeList = MutableLiveData<ArrayList<Recipe>>(arrayListOf())
    val recipeList: LiveData<ArrayList<Recipe>> get() = _recipeList
    private var _uploadStoryImagesList = MutableLiveData<ArrayList<String>>()

    private var _userStories = MutableLiveData<ArrayList<UserStories>>()
    val userStories: LiveData<ArrayList<UserStories>> get() = _userStories

    private var _message = MutableSharedFlow<String>()
    val message = _message.asSharedFlow()

    private var lastDocumentSnapshot: DocumentSnapshot? = null

    fun getRecipeList(following: ArrayList<String>) {
        viewModelScope.launch(Dispatchers.IO) {
            val (list, newLastDocumentSnapshot) = recipeRepository.getRecipesList(
                lastDocumentSnapshot,
                following
            )
            if (list.size > 0) {
                val tempList = _recipeList.value
                for (i in list) tempList?.add(i)
                withContext(Dispatchers.Main) {
                    _recipeList.value = tempList!!
                    lastDocumentSnapshot = newLastDocumentSnapshot
                }
            }
        }
    }

    fun updateRecipesLikesData(recipeId: String, likedBy: java.util.ArrayList<String>) {
        viewModelScope.launch(Dispatchers.IO) {
            recipeRepository.updateRecipesLikesData(recipeId, likedBy)
        }
    }

    fun sendNotification(notification: Notification, notificationToId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            notificationRepository.addNotification(notification, notificationToId)
        }
    }

    fun getImagesAndAddToUserStories(intent: Intent) {
        val tempList: ArrayList<String> = arrayListOf()
        val clipData = intent.clipData
        var i = 0;
        if (clipData != null) {
            while (i < clipData.itemCount) {
                tempList.add(clipData.getItemAt(i).uri.toString())
                i++;
            }
        } else {
            intent.data?.let { tempList.add(it.toString()) }
        }
        _uploadStoryImagesList.value = tempList

        uploadStoryToDatabase()
        Log.i("InHomeViewModel", _uploadStoryImagesList.toString())
    }

    private fun uploadStoryToDatabase() {
        viewModelScope.launch(Dispatchers.IO) {
            _message.emit("Uploading Stories...")
            val state = storyRepository.addUserStories(_uploadStoryImagesList.value)
            withContext(Dispatchers.Main){
                observeAddStoryState(state)
            }
        }
    }

    private suspend fun observeAddStoryState(state: Resource<String>) {
        when (state){
            is Resource.Success ->{
                _message.emit(state.data.toString())
            }
            is Resource.Error -> {
                _message.emit(state.message.toString())
            }
        }
    }

    fun getStories(following: ArrayList<String>) {
        viewModelScope.launch(Dispatchers.IO) {
            val dataState = storyRepository.getStories(following)
            withContext(Dispatchers.Main) {
                observeGetStories(dataState)
            }
        }
    }

    private suspend fun observeGetStories(dataState: Resource<java.util.ArrayList<UserStories>>) {
        when(dataState){
            is Resource.Success ->{
                _userStories.value = dataState.data!!
            }
            is Resource.Error ->{
                _message.emit(dataState.message.toString())
            }
        }
    }

}