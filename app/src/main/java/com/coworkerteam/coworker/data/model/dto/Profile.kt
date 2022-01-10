package com.coworkerteam.coworker.data.model.dto


import com.google.gson.annotations.SerializedName

data class Profile(
    @SerializedName("category")
    val category: String,
    @SerializedName("email")
    val email: String,
    @SerializedName("img")
    val img: String,
    @SerializedName("login_type")
    val loginType: String,
    @SerializedName("nickname")
    val nickname: String
)