package com.example.android_level_3.presentation.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.android_level_3.databinding.ActivityMainBinding
import com.example.android_level_3.presentation.ui.viewmodel.SharedViewModelFactory
import com.example.android_level_3.viewmodel.SharedViewModel

class MainActivity : AppCompatActivity(){

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val viewModel = ViewModelProvider(
            this,
            SharedViewModelFactory(this)
        ).get(SharedViewModel::class.java)
    }

}