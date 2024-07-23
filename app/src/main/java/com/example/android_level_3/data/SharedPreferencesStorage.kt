package com.example.android_level_3.data

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.util.Log
import com.example.android_level_3.presentation.ui.MainActivity
import com.example.android_level_3.domain.constants.PreferencesConst
import com.example.android_level_3.data.retrofit.model.UserData

class SharedPreferencesStorage(context: Context) {

    private val sharedPreferences by lazy {
        context.getSharedPreferences(PreferencesConst.PREFERENCES_SETTINGS, MODE_PRIVATE) }

    // authorisation activity
    fun checkingDataForAutoLogin(): Boolean {
        if (sharedPreferences?.contains(PreferencesConst.PREFERENCES_EMAIL) == true) {
            sharedPreferences?.edit()?.apply {
                putBoolean(PreferencesConst.PREFERENCES_AUTOLOGIN, true)
            }?.apply()
            return true
        }
        return false
    }

    // in authorisation activity
    fun saveDataForAutoLogin(email: String, password: String) {
        sharedPreferences?.edit()?.apply {
            putString(PreferencesConst.PREFERENCES_EMAIL, email)
            putString(PreferencesConst.PREFERENCES_PASSWORD, password)
        }?.apply()
    }

    // authorisation activity AND registration activity
    fun saveUserDataToPreferences(responseData: UserData?, autoLogin: Boolean? = null,
                                  email: String? = null, password: String? = null) {
        sharedPreferences?.edit()?.apply {
            putString(PreferencesConst.PREFERENCES_ACCESS_TOKEN, responseData?.accessToken)
            putString(PreferencesConst.PREFERENCES_REFRESH_TOKEN, responseData?.refreshToken)
            putString(PreferencesConst.PREFERENCES_USER_NAME, responseData?.user?.name)
            putString(PreferencesConst.PREFERENCES_USER_CAREER, responseData?.user?.career)
            putString(PreferencesConst.PREFERENCES_USER_ADDRESS, responseData?.user?.address)
            putInt(PreferencesConst.PREFERENCES_USER_ID, responseData?.user?.id!!)

            autoLogin?.let {
                putString(PreferencesConst.PREFERENCES_EMAIL, email)
                putString(PreferencesConst.PREFERENCES_PASSWORD, password)
            }
        }?.apply()
    }


    // in Settings Fragment
    fun checkingAuthorizationNeed(): Boolean {
        return sharedPreferences?.getBoolean(PreferencesConst.PREFERENCES_AUTOLOGIN, false) == true
    }

    // in Settings Fragment
    fun updateTokens(accessToken: String, refreshToken: String) {
        sharedPreferences.edit().apply {
            putString(PreferencesConst.PREFERENCES_ACCESS_TOKEN, accessToken)
            putString(PreferencesConst.PREFERENCES_REFRESH_TOKEN, refreshToken)
        }.apply()
    }

    // in Settings Fragment
    fun getUserIdFromStorage(): Int {
        return sharedPreferences.getInt(PreferencesConst.PREFERENCES_USER_ID, 0)
    }

    fun getUserNameFromStorage(): String {
        return sharedPreferences.getString(PreferencesConst.PREFERENCES_USER_NAME, null).toString()
    }

    fun getUserCareerFromStorage(): String {
        return sharedPreferences.getString(PreferencesConst.PREFERENCES_USER_CAREER, null).toString()
    }

    fun getUserAddressFromStorage(): String {
        return sharedPreferences.getString(PreferencesConst.PREFERENCES_USER_ADDRESS, null).toString()
    }

    fun getUserAccessTokenFromStorage(): String {
        return "Bearer ${sharedPreferences.getString(PreferencesConst.PREFERENCES_ACCESS_TOKEN, "")}"
    }

    fun getUserEmailFromStorage(): String {
        return sharedPreferences?.getString(PreferencesConst.PREFERENCES_EMAIL, null).toString()
    }

    fun getUserPasswordFromStorage(): String {
        return sharedPreferences.getString(PreferencesConst.PREFERENCES_PASSWORD, null).toString()
    }

    // in Settings Fragment
    fun clearSharedPreferencesData() {
        sharedPreferences.edit()?.apply {
            clear()
        }?.apply()
    }
}