package com.example.android_level_3

import java.io.Serializable

data class User(
    val userName: String,
    val userCareer: String,
    val userEmail: String = "default.mail@mail.com",
    val userPhoneNumber: String = "+38-012-345-67-89",
    val userAddress: String = "Lake Village, Sweet Home, 124",
    val userBirthday: String = "01/01/2023",
    val userImage: String
) : Serializable
