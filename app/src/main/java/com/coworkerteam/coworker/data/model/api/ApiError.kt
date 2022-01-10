package com.coworkerteam.coworker.data.model.api


import com.google.gson.annotations.SerializedName

data class ApiError(
    @SerializedName("message")
    val message: String
)