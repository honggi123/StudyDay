package com.coworkerteam.coworker.data.model.api


import com.google.gson.annotations.SerializedName

data class CamStudyJoinResponse(
    @SerializedName("message")
    val message: String,
    @SerializedName("result")
    val result: Result
){
    data class Result(
        @SerializedName("studyTime")
        val studyTime: String,
        @SerializedName("studyTimeSec")
        val studyTimeSec: Int
    )
}