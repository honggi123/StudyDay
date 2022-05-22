package com.coworkerteam.coworker.data.model.api

import com.google.gson.annotations.SerializedName

data class MoodPostResponse(
    @SerializedName("message")
    val message: String,
    @SerializedName("result")
    val result: List<Result>
){
    data class Result(
        @SerializedName("achieveTimeRate")
        val achieveTimeRate: Int,
        @SerializedName("achieveTodoRate")
        val achieveTodoRate: Int?,
        @SerializedName("currentPage")
        val currentPage: Int,
        @SerializedName("dream")
        val dream: Dream,
        @SerializedName("isLeader")
        val isLeader: String,
        @SerializedName("profile")
        val profile: Profile,
        @SerializedName("moodPosts")
        val moodPosts: ArrayList<MoodPost>,
        @SerializedName("totalPage")
        val totalPage: Int

    ){
        data class Dream(
            @SerializedName("dday")
            val dday: String?,
            @SerializedName("dday_date")
            val dday_date: String?,
            @SerializedName("dday_name")
            val dday_name: String?,
            @SerializedName("goal")
            val goal: String
        )

        data class Profile(
            @SerializedName("category")
            val category: String,
            @SerializedName("email")
            val email: String,
            @SerializedName("img")
            val img: String,
            @SerializedName("login_type")
            val login_type: String,
            @SerializedName("nickname")
            val nickname: String
        )

        data class MoodPost(
            @SerializedName("idx")
            val idx: Int,
            @SerializedName("mood")
            val mood: Int,
            @SerializedName("nickname")
            val nickname: String,
            @SerializedName("create_date")
            val create_date: String,
            @SerializedName("contents")
            val contents: String,
            @SerializedName("is_empathy")
            var is_empathy: String,
            @SerializedName("my_empathy")
            var my_empathy: Int,
            @SerializedName("empathy_kinds")
            var empathy_kinds: String,
            @SerializedName("total_empathy")
            var total_empathy: Int
        )
    }
}

