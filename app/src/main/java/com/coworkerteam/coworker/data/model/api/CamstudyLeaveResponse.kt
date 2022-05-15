package com.coworkerteam.coworker.data.model.api

import com.google.gson.annotations.SerializedName

data class CamstudyLeaveResponse(
    @SerializedName("message")
    val message: String,
    @SerializedName("result")
    val result: List<Result>
){
    data class Result(
        @SerializedName("isSuccess")
        val isSuccess: Boolean,
        @SerializedName("successTime")
        val successTime: Int,
        @SerializedName("isWrite")
        val isWrite: Boolean
    )
}

