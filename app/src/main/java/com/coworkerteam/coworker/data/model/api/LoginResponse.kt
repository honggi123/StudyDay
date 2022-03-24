package com.coworkerteam.coworker.data.model.api

import com.google.gson.annotations.SerializedName

data class LoginResponse(
    @SerializedName("message")
    val message: String,
    @SerializedName("result")
    val result: List<Result>
    ){
        data class Result(
            @SerializedName("accessToken")
            val accessToken: String,
            @SerializedName("isInterest")
            val isInterest: Boolean,
            @SerializedName("nickname")
            val nickname: String,
            @SerializedName("refreshToken")
            val refreshToken: String
        )
}