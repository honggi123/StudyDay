package com.coworkerteam.coworker.data.model.api


import com.google.gson.annotations.SerializedName

data class MyStudyResponse(
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
        @SerializedName("group")
        val group: List<Group>,
        @SerializedName("groupPage")
        val groupPage: Int,
        @SerializedName("open")
        val `open`: List<Any>,
        @SerializedName("openPage")
        val openPage: Int,
        @SerializedName("profile")
        val profile: Profile,
        @SerializedName("todoNum")
        val todoNum: Int
    ){

        data class Dream(
            @SerializedName("dday_date")
            val ddayDate: Any,
            @SerializedName("dday_name")
            val ddayName: Any,
            @SerializedName("goal")
            val goal: Any
        )

        data class Group(
            @SerializedName("idx")
            val idx: Int,
            @SerializedName("img")
            val img: String,
            @SerializedName("is_leader")
            val isLeader: Boolean,
            @SerializedName("max_num")
            val maxNum: Int,
            @SerializedName("name")
            val name: String,
            @SerializedName("user_num")
            val userNum: Int,
            @SerializedName("category")
            val category: String
        )

        data class Profile(
            @SerializedName("img")
            val img: String,
            @SerializedName("login_type")
            val loginType: String,
            @SerializedName("nickname")
            val nickname: String
        )
    }
}