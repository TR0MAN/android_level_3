package com.example.android_level_3

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import com.example.android_level_3.databinding.ActivityMainBinding
import com.example.android_level_3.viewmodel.MainViewModel

class MainActivity : AppCompatActivity(){

    private lateinit var binding: ActivityMainBinding
    val viewModel by lazy { ViewModelProvider(this).get(MainViewModel::class.java) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

}