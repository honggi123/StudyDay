package com.coworkerteam.coworker.data.model.api


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
    ){
        data class MyStudy(
            @SerializedName("category")
            val category: String,
            @SerializedName("idx")
            val idx: Int,
            @SerializedName("img")
            val img: String,
            @SerializedName("introduce")
            val introduce: String,
            @SerializedName("is_leader")
            val isLeader: Boolean,
            @SerializedName("name")
            val name: String
        )
        data class Dream(
            @SerializedName("dday_date")
            val ddayDate: String?,
            @SerializedName("dday_name")
            val ddayName: String?,
            @SerializedName("goal")
            val goal: String?,
            @SerializedName("dday")
            val dday: String?
        )
        data class Profile(
            @SerializedName("category")
            val category: String,
            @SerializedName("email")
            val email: String,
            @SerializedName("img")
            val img: String,
            @SerializedName("login_type")
            val loginType: String,
            @SerializedName("nickname")
            val nickname: String
        )
    }
}