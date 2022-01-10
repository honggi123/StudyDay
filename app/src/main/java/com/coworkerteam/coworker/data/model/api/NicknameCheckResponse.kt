package com.coworkerteam.coworker.data.model.api


import com.google.gson.annotations.SerializedName

data class NicknameCheckResponse(
    @SerializedName("isUse")
    val isUse: String,
    @SerializedName("message")
    val message: String
)