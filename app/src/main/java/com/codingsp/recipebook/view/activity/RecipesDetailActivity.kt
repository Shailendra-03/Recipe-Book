package com.codingsp.recipebook.view.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import com.codingsp.recipebook.R
import com.codingsp.recipebook.adapters.DishImagesViewPagerAdapter
import com.codingsp.recipebook.databinding.ActivityRecipesDetailBinding
import com.codingsp.recipebook.model.Recipe
import com.codingsp.recipebook.utils.Constants
import com.codingsp.recipebook.utils.TimeToTimeAgo
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import java.lang.Exception

class RecipesDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRecipesDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecipesDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}