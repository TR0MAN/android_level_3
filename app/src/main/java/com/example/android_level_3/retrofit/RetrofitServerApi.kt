package com.example.android_level_3.retrofit

import com.example.android_level_3.retrofit.model.CreateUserModel
import com.example.android_level_3.retrofit.model.ServerResponse
import com.example.android_level_3.retrofit.model.UserAuthorisationEntity
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST

interface RetrofitServerApi {

    // CREATE NEW USER - OK
    @Headers("Content-type: application/json")
    @POST("users")
    suspend fun registerNewUser(@Body registrationUserData: CreateUserModel): Response<ServerResponse>

    // LOGIN USER - OK
    @Headers("Content-type: application/json")
    @POST("login")
    suspend fun authoriseUser(@Body userLoginData: UserAuthorisationEntity): Response<ServerResponse>

    // REFRESH TOKEN - OK
    @Headers("Content-type: application/json")
    @POST("refresh")
    suspend fun refreshToken(
        @Header("RefreshToken") refreshToken: String
    ): ServerResponse

}