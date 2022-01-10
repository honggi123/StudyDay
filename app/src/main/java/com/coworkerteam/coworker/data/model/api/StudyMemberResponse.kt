package com.coworkerteam.coworker.data.model.api


import com.google.gson.annotations.SerializedName

data class StudyMemberResponse(
    @SerializedName("message")
    val message: String,
    @SerializedName("result")
    val result: List<Result>
){
    data class Result(
        @SerializedName("idx")
        val idx: Int,
        @SerializedName("img")
        val img: String,
        @SerializedName("nickname")
        val nickname: String
    )
}