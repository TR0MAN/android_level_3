package com.example.android_level_3.data.retrofit.model

data class ExtensionServerResponse (
    val status: String,
    val code: Int,
    val message: String?,
    val data: AllUsers?
)

data class ServerResponse (
    val status: String,
    val code: Int,
    val message: String?,
    val data: UserData?
)