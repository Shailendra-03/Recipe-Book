package com.codingsp.recipebook.viewmodel.fragmentViewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codingsp.recipebook.model.User
import com.codingsp.recipebook.repositories.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class UserDetailsViewModel : ViewModel() {
    private val userRepository = UserRepository()
    private var _userDetails = MutableLiveData<User?>()
    val userDetails: LiveData<User?> get() = _userDetails


    fun getUserById(userId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val temp = userRepository.getUserById(userId)
            withContext(Dispatchers.Main) {
                _userDetails.value = temp
            }
        }
    }

}