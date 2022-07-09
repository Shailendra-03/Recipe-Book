package com.codingsp.recipebook.viewmodel.fragmentViewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codingsp.recipebook.model.Recipe
import com.codingsp.recipebook.model.User
import com.codingsp.recipebook.repositories.RecipeRepository
import com.codingsp.recipebook.repositories.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProfileViewModel : ViewModel() {

    private val recipeRepository = RecipeRepository()
    private val userRepository = UserRepository()

    private var _recipe = MutableLiveData<Recipe>()
    val recipe: LiveData<Recipe> get() = _recipe

    private var _currentUser = MutableLiveData<User?>()
    val currentUser: LiveData<User?> get() = _currentUser

    fun getCurrentUser() {
        viewModelScope.launch(Dispatchers.IO) {
            val user = userRepository.getCurrentUser()
            withContext(Dispatchers.Main) {
                _currentUser.value = user
            }
        }
    }

}