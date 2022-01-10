package com.coworkerteam.coworker.data.model.api


import com.google.gson.annotations.SerializedName

data class AutoLoginResponse(
    @SerializedName("message")
    val message: String,
    @SerializedName("result")
    val result: Result
){
    data class Result(
        @SerializedName("accessToken")
        val accessToken: String,
        @SerializedName("refreshToken")
        val refreshToken: String
    )
}