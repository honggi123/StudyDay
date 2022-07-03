package com.coworkerteam.coworker.data.model.api

import com.google.gson.annotations.SerializedName

data class SuccessPostResponse(
    @SerializedName("message")
    val message: String,
    @SerializedName("result")
    val result: List<Result>
){
    data class Result(
        @SerializedName("achieveTimeRate")
        val achieveTimeRate: Int,
        @SerializedName("achieveTodoRate")
        val achieveTodoRate: Any,
        @SerializedName("currentPage")
        val currentPage: Int,
        @SerializedName("dream")
        val dream: Dream,
        @SerializedName("isLeader")
        val isLeader: String,
        @SerializedName("profile")
        val profile: Profile,
        @SerializedName("successPosts")
        val successPosts: ArrayList<SuccessPost>,
        @SerializedName("totalPage")
        val totalPage: Int
    ){
        data class Dream(
            @SerializedName("dday")
            val dday: Any,
            @SerializedName("dday_date")
            val dday_date: Any,
            @SerializedName("dday_name")
            val dday_name: Any,
            @SerializedName("goal")
            val goal: String
        )
        data class  SuccessPost(
            @SerializedName("contents")
            val contents: String,
            @SerializedName("create_date")
            val create_date: String,
            @SerializedName("idx")
            val idx: Int,
            @SerializedName("nickname")
            val nickname: String,
            @SerializedName("success_time")
            val success_time: Int
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
    }
}
