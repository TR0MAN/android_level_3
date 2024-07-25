package com.example.android_level_3.presentation.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.android_level_3.data.SharedPreferencesStorage
import com.example.android_level_3.viewmodel.SharedViewModel

class SharedViewModelFactory(context: Context): ViewModelProvider.Factory {

    private val dataStorage = SharedPreferencesStorage(context = context)

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SharedViewModel(dataStorage = dataStorage) as T
    }

}