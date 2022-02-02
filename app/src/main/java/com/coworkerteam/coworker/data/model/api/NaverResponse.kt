package com.coworkerteam.coworker.data.model.api

import com.google.gson.annotations.SerializedName

data class NaverResponse(
    @SerializedName("message")
    val message: String,
    @SerializedName("response")
    val response: Response,
    @SerializedName("resultcode")
    val resultcode: String
){
    data class Response(
        @SerializedName("age")
        val age: String,
        @SerializedName("birthday")
        val birthday: String,
        @SerializedName("birthyear")
        val birthyear: String,
        @SerializedName("email")
        val email: String,
        @SerializedName("gender")
        val gender: String,
        @SerializedName("id")
        val id: String,
        @SerializedName("mobile")
        val mobile: String,
        @SerializedName("name")
        val name: String,
        @SerializedName("nickname")
        val nickname: String,
        @SerializedName("profile_image")
        val profileImage: String
    )
}