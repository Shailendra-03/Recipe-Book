package com.codingsp.recipebook.view.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import com.codingsp.recipebook.R

import com.codingsp.recipebook.databinding.ActivityLoginBinding
import com.codingsp.recipebook.model.User
import com.codingsp.recipebook.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private var user : User?= null
    private var isLoading = true
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getCurrentUser()
        binding = ActivityLoginBinding.inflate(layoutInflater)
        installSplashScreen().apply {
            setKeepOnScreenCondition{
                isLoading
            }
        }
        setContentView(binding.root)
    }

    private fun getCurrentUser() {
        if(Firebase.auth.currentUser == null) isLoading=false
        Firebase.auth.currentUser?.let {
            Firebase.firestore.collection("Users").document(it.uid).get()
                .addOnSuccessListener { docSnapshot->
                    user = docSnapshot.toObject(User::class.java)
                    isLoading=false
                    goToMainActivity()
                }
                .addOnFailureListener{
                    isLoading=false
                }
        }
    }

    private fun goToMainActivity() {
        val intent = Intent(this,MainActivity::class.java)
        intent.putExtra(Constants.USER_DETAILS_FROM_LOGIN_ACTIVITY_TO_MAIN_ACTIVITY,user)
        startActivity(intent)
        finish()
    }
}