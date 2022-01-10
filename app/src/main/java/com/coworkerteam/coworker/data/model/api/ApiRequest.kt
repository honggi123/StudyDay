package com.coworkerteam.coworker.data.model.api


import com.google.gson.annotations.SerializedName

data class ApiRequest(
    @SerializedName("message")
    val message: String
)