package com.example.android_level_3

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.lifecycle.ViewModelProvider
import com.example.android_level_3.databinding.ActivityMainBinding
import com.example.android_level_3.viewmodel.MainViewModel

class MainActivity : AppCompatActivity(){

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

//    startActivity(Intent(this, RegistrationActivity::class.java).apply {
//        val email = sharedPreferences?.getString(Const.PREFERENCES_EMAIL,Const.PREFERENCES_DEFAULT_TEXT)
//        putExtra(Const.EMAIL, email)
//    })

}


