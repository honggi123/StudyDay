package com.coworkerteam.coworker.data.model.api


import com.coworkerteam.coworker.data.model.dto.Dream
import com.coworkerteam.coworker.data.model.dto.MyStudy
import com.coworkerteam.coworker.data.model.dto.Profile
import com.google.gson.annotations.SerializedName

data class MyStudyManageResponse(
    @SerializedName("message")
    val message: String,
    @SerializedName("result")
    val result: Result
){
    data class Result(
        @SerializedName("achieveTimeRate")
        val achieveTimeRate: Int,
        @SerializedName("achieveTodoRate")
        val achieveTodoRate: Int,
        @SerializedName("dream")
        val dream: Dream,
        @SerializedName("myStudy")
        val myStudy: List<MyStudy>,
        @SerializedName("myStudyPage")
        val myStudyPage: Int,
        @SerializedName("profile")
        val profile: Profile
    )
}