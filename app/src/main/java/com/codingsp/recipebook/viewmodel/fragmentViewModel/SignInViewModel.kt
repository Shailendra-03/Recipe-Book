package com.codingsp.recipebook.viewmodel.fragmentViewModel

import android.content.Intent
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codingsp.recipebook.model.User
import com.codingsp.recipebook.repositories.AuthRepository
import com.codingsp.recipebook.utils.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SignInViewModel : ViewModel() {
    private val authRepository = AuthRepository()

    private var _isProgressBarVisible: MutableLiveData<Boolean> = MutableLiveData(false)
    val isProgressBarVisible: LiveData<Boolean> get() = _isProgressBarVisible

    private var _message = MutableLiveData<String>()
    val message: LiveData<String> get() = _message

    private var _signedInUser =  MutableSharedFlow<User>()
    val signedInUser = _signedInUser.asSharedFlow()


    fun signInWithGmail(intentData: Intent) {
        _isProgressBarVisible.value = true
        viewModelScope.launch(Dispatchers.IO) {
            val loginState = authRepository.signInWithGmail(intentData)
            withContext(Dispatchers.Main) {
                observeLoginState(loginState)
            }
        }
    }

    fun sendResetPasswordEmail(email: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val message = authRepository.sendResetPasswordEmail(email)
            withContext(Dispatchers.Main) {
                _message.value = message!!
            }
        }
    }

    fun checkForEmailCorrectness(email: String): Boolean {
        if (!email.contains(".com") || !email.contains("@")) {
            _message.value = "Please Enter Correct Email Address"
            return false
        }
        return true
    }

    fun signInWithEmailAndPassword(email: String, password: String) {
        _isProgressBarVisible.value = true
        viewModelScope.launch {
            val loginState  = authRepository.signInWithEmailAndPassword(email, password)
            withContext(Dispatchers.Main){
                observeLoginState(loginState)
            }
        }
    }

    private suspend fun observeLoginState(loginState: Resource<User>) {
        when (loginState) {
            is Resource.Success -> {
                loginState.data?.let { _signedInUser.emit(it) }
            }
            is Resource.Error -> {
                loginState.message?.let { _message.value = it }
            }
        }
        _isProgressBarVisible.value = false
    }
}