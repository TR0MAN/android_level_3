package com.example.android_level_3.retrofit.model

import java.io.File
import java.util.Date

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

data class UserAuthorisationEntity (
    val email: String,
    val password: String
)

data class UserData(
    val user: FullUserInfo,
    val accessToken: String,
    val refreshToken: String
)

data class AllUsers (
    val users: List<FullUserInfo>
)

data class FullUserInfo(
    val id: Int,
    val email: String,
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
//    val createdAt: Date?,       // пока не используется в проекте
//    val updatedAt: Date?        // пока не используется в проекте
)

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
    val image: File?            // в ДОКе не указанно что приходит, а по факту есть
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



// --------- CONTACTS ------------

data class ContactsServerResponse (
    val status: String,
    val code: Int,
    val message: String?,
    val data: AllContacts?
)

data class AllContacts (
    val contacts: List<FullUserInfo>
)

data class ContactId (
    val contactId: Int
)