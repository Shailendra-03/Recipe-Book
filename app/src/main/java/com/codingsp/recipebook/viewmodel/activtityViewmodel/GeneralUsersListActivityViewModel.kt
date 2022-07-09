package com.codingsp.recipebook.viewmodel.activtityViewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codingsp.recipebook.model.User
import com.codingsp.recipebook.repositories.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.ArrayList

class GeneralUsersListActivityViewModel:ViewModel() {
    private val userRepository=UserRepository()

    private var _currentUser= MutableLiveData<User>()
    val currentUser: LiveData<User> get() = _currentUser

    fun getCurrentUser() {
        viewModelScope.launch(Dispatchers.IO){
            val user=userRepository.getCurrentUser()
            withContext(Dispatchers.Main){
                user?.let {
                    _currentUser.value=it
                }
            }
        }
    }


    fun updateUsersFollowersData(userId: String, followers: ArrayList<String>) {
        viewModelScope.launch(Dispatchers.IO){
            userRepository.updateUserFollowersData(userId,followers)
        }

    }

    fun updateUsersFollowingData(userId: String, following: ArrayList<String>) {
        viewModelScope.launch(Dispatchers.IO){
            userRepository.updateUserFollowingData(userId,following)
        }
    }
}