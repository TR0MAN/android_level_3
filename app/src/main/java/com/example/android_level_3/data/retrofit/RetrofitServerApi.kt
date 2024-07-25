package com.example.android_level_3.data.retrofit

import com.example.android_level_3.data.retrofit.model.ContactId
import com.example.android_level_3.data.retrofit.model.ContactsServerResponse
import com.example.android_level_3.data.retrofit.model.CreateUserModel
import com.example.android_level_3.data.retrofit.model.EditUserModel
import com.example.android_level_3.data.retrofit.model.ExtensionServerResponse
import com.example.android_level_3.data.retrofit.model.ServerResponse
import com.example.android_level_3.data.retrofit.model.UserAuthorisationEntity
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface RetrofitServerApi {

    @Headers("Content-type: application/json")
    @POST("users")
    suspend fun registerNewUser(@Body registrationUserData: CreateUserModel): Response<ServerResponse>

    @Headers("Content-type: application/json")
    @POST("login")
    suspend fun authoriseUser(@Body userLoginData: UserAuthorisationEntity): Response<ServerResponse>

    @Headers("Content-type: application/json")
    @GET("users")
    suspend fun getAllUsers(@Header("Authorization") token: String): Response<ExtensionServerResponse>

    // NOT USED NOW
    @Headers("Content-type: application/json")
    @POST("refresh")
    suspend fun refreshToken(
        @Header("RefreshToken") refreshToken: String
    ): Response<ServerResponse>

    // NOT USED NOW
    @GET("users/{userId}")
    suspend fun getUserData(
        @Header("Authorization") token: String,
        @Path("userId") userId: Int): ServerResponse

    // NOT USED NOW
    @Headers("Content-type: application/json")
    @PUT("users/{userId}")
    suspend fun editUserData(
        @Path("userId") userId: Int,
        @Header("Authorization") accessToken: String,
        @Body newUserData: EditUserModel
    ): ServerResponse


    @GET("users/{userId}/contacts")
    suspend fun getUserContacts(
        @Path("userId") userId: Int,
        @Header("Authorization") accessToken: String
    ): Response<ContactsServerResponse>

    @Headers("Content-type: application/json")
    @PUT("users/{userId}/contacts")
    suspend fun addContactToUserContactList(
        @Path("userId") userId: Int,
        @Header("Authorization") accessToken: String,
        @Body contactId: ContactId
    ): Response<ContactsServerResponse>

    @DELETE("users/{userId}/contacts/{contactId}")
    suspend fun deleteContactFromUserList(
        @Path("userId") userId: Int,
        @Path("contactId") contactIdForDelete: Int,
        @Header("Authorization") accessToken: String
    ): Response<ContactsServerResponse>

}