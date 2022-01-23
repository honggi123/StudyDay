package com.coworkerteam.coworker.data.model.api


import com.google.gson.annotations.SerializedName

data class StudySearchStartResponse(
    @SerializedName("message")
    val message: String,
    @SerializedName("result")
    val result: Result
){
    data class Result(
        @SerializedName("achieveTimeRate")
        val achieveTimeRate: Int?,
        @SerializedName("achieveTodoRate")
        val achieveTodoRate: Int?,
        @SerializedName("currentPage")
        val currentPage: Int,
        @SerializedName("dream")
        val dream: Dream,
        @SerializedName("keyword")
        val keyword: String,
        @SerializedName("profile")
        val profile: Profile,
        @SerializedName("resultNum")
        val resultNum: Int,
        @SerializedName("study")
        val study: List<StudySearchResponse.Result.Study>,
        @SerializedName("totalPage")
        val totalPage: Int
    ){
        data class Dream(
            @SerializedName("dday")
            val dday: String?,
            @SerializedName("dday_date")
            val ddayDate: String?,
            @SerializedName("dday_name")
            val ddayName: String?,
            @SerializedName("goal")
            val goal: String?
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