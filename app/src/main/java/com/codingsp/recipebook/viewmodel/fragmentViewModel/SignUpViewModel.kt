package com.codingsp.recipebook.viewmodel.fragmentViewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codingsp.recipebook.model.User
import com.codingsp.recipebook.repositories.AuthRepository
import com.codingsp.recipebook.utils.Resource
import com.codingsp.recipebook.utils.UserDetailsState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SignUpViewModel : ViewModel() {
    private val authRepository = AuthRepository()
    private var _errorSignUpMessage = MutableLiveData("")
    val errorSignupMessage: LiveData<String> get() = _errorSignUpMessage

    private var _isProgressDialogVisible = MutableSharedFlow<Boolean>()
    val isProgressDialogVisible = _isProgressDialogVisible.asSharedFlow()

    private var _signedInUser = MutableSharedFlow<User>()
    val signedInUser = _signedInUser.asSharedFlow()

    fun checkForDetails(userName: String, user: User, userPassword: String): UserDetailsState {
        when {
            user.userImageUrl.isEmpty() -> {
                return UserDetailsState.UserProfileImageError("Please add a profile image")
            }
            userName.isEmpty() || userName.contains(" ") -> {
                return UserDetailsState.UserNameError("Please Enter valid username. Username should not contain any spaces")
            }
            user.userDisplayName.isEmpty() -> {
                return UserDetailsState.UserDisplayNameError("Please Enter Your Name")
            }
            user.userEmail.isEmpty() || !user.userEmail.contains(
                "@",
                true
            ) || !user.userEmail.contains(".com") -> {
                return UserDetailsState.UserEmailError("Please Enter valid email address")
            }
            user.userDOB.isEmpty() -> {
                _errorSignUpMessage.value = "Enter Your Date of birth"
                return UserDetailsState.UserDOBError("Please select your birth date")
            }
            userPassword.length < 8 -> {
                return UserDetailsState.UserPasswordError("Please enter password in 8 words or more")
            }
            else -> {
                return UserDetailsState.Success()
            }
        }
    }

    fun createUserWithEmailAndPassword(user: User, userPassword: String) {
        viewModelScope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Main) {
                _isProgressDialogVisible.emit(true)
            }
            val loginState = authRepository.createUserWithEmailAndPassword(user, userPassword)
            withContext(Dispatchers.Main){
                observeLoginState(loginState)
            }
        }
    }

    private suspend fun observeLoginState(loginState: Resource<User>) {
        when (loginState) {
            is Resource.Success -> {
                _signedInUser.emit(loginState.data!!)
            }
            is Resource.Error -> {
                _errorSignUpMessage.value = loginState.message
            }
        }
        _isProgressDialogVisible.emit(false)
    }
}