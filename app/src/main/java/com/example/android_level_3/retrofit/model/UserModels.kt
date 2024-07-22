package com.example.android_level_3.retrofit.model

import java.io.File
import java.util.Date

data class UserAuthorisationEntity (
    val email: String,
    val password: String
)

data class UserData(
    val user: ContactInfo,
    val accessToken: String,
    val refreshToken: String
)

data class AllUsers (
    val users: List<Contact>
)

data class CreateUserModel(
    val email: String,
    val password: String,
    val name: String?,
    val phone: String?,
    val address: String?,
    val career: String?,
    val birthday: Date?,
    val facebook: String? = "facebook.com",
    val instagram: String? = "instagram.com",
    val twitter: String? = "twitter.com",
    val linkedin: String? = "linkedin.com",
    val image: File?
)

// NOT USED NOW
// additionally comes the parameter image: File? (not specified in documentation)
data class EditUserModel(
    val name: String?,
    val phone: String?,
    val address: String?,
    val career: String?,
    val birthday: Date?,
    val facebook: String?,
    val instagram: String?,
    val twitter: String?,
    val linkedin: String?,
    val image: File?
)


