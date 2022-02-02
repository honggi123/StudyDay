package com.coworkerteam.coworker.data.model.api

import com.google.gson.annotations.SerializedName

data class StudyRequest(
    @SerializedName("message")
    val message: String,
    @SerializedName("result")
    val result: Result
){
    data class Result(
        @SerializedName("pw")
        val pw: String,
        @SerializedName("studyIdx")
        val studyIdx: Int
    )
}