package com.codingsp.recipebook.view.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.codingsp.recipebook.databinding.ActivityAddStoryBinding

class AddStoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddStoryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddStoryBinding.inflate(layoutInflater)
        setContentView(binding.root)


    }
}