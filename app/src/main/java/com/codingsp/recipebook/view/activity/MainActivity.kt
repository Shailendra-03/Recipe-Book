package com.codingsp.recipebook.view.activity

import android.os.Bundle
import android.util.Log
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.codingsp.recipebook.R
import com.codingsp.recipebook.databinding.ActivityMainBinding
import com.codingsp.recipebook.interfaces.DataProviderFromActivity
import com.codingsp.recipebook.model.User
import com.codingsp.recipebook.repositories.UserRepository
import com.codingsp.recipebook.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity(), DataProviderFromActivity {

    private lateinit var binding: ActivityMainBinding
    var user: User? = null
    private lateinit var userRepository: UserRepository
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if(intent.hasExtra(Constants.USER_DETAILS_FROM_LOGIN_ACTIVITY_TO_MAIN_ACTIVITY)){
            user = intent.getParcelableExtra(Constants.USER_DETAILS_FROM_LOGIN_ACTIVITY_TO_MAIN_ACTIVITY)
        }
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        userRepository = UserRepository()
        db.collection("Users").document(auth.currentUser!!.uid).addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.i("MainActivity", e.message.toString())
                return@addSnapshotListener
            }
            if (snapshot != null && snapshot.exists()) {
                user = snapshot.toObject(User::class.java)
            } else {
                Log.i("MainActivity", "Failed")
            }
        }
    }

    override fun onStart() {
        super.onStart()

        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home,
                R.id.navigation_notifications,
                R.id.navigationAddPost,
                R.id.navigationProfile,
                R.id.navigation_explore
            )
        )
        navView.setupWithNavController(navController)
    }

    override fun getUserFromActivity(): User? {
        return this.user
    }
}