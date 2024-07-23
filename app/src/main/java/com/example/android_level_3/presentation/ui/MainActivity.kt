package com.example.android_level_3.presentation.ui

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.android_level_3.databinding.ActivityMainBinding
import com.example.android_level_3.presentation.ui.viewmodel.SharedViewModel
import com.example.android_level_3.presentation.ui.viewmodel.SharedViewModelFactory

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

        Log.d("TAG", "MainActivity -> onCreate")
        Log.d("TAG", "MainActivity -> USE[$viewModel]")
        Log.d("TAG", "MainActivity -> DATA STORAGE -> [${viewModel.dataStorage}]")
        Log.d("TAG", "MainActivity [END] -> -----------------------------------")
    }

}