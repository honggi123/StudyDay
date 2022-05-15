package com.coworkerteam.coworker.data.model.api

import com.google.gson.annotations.SerializedName

data class PostDeleteResponse(
    @SerializedName("message")
    val message: String,
    @SerializedName("deleteIdx")
    val deleteIdx: Int

)

