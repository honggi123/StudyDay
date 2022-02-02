package com.coworkerteam.coworker.data.model.api


import com.google.gson.annotations.SerializedName

data class InstanceIDResponse(
    @SerializedName("instanceId")
    val instanceId: String?,
    @SerializedName("message")
    val message: String
)