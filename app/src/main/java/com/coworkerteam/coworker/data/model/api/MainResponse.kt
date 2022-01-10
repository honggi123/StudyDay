package com.coworkerteam.coworker.data.model.api

import com.google.gson.annotations.SerializedName

data class MainResponse(
    @SerializedName("message")
    val message: String,
    @SerializedName("result")
    val result: List<Result>
) {

    data class Result(
        @SerializedName("achieveTimeRate")
        val achieveTimeRate: Int,
        @SerializedName("achieveTodoRate")
        val achieveTodoRate: Int,
        @SerializedName("dream")
        val dream: Dream,
        @SerializedName("groupRecommend")
        val groupRecommend: List<Study>,
        @SerializedName("myStudy")
        val myStudy: List<MyStudy>,
        @SerializedName("myStudyPage")
        val myStudyPage: Int,
        @SerializedName("newGroupStudy")
        val newGroupStudy: List<Study>,
        @SerializedName("newOpenStudy")
        val newOpenStudy: List<Study>,
        @SerializedName("openRecommend")
        val openRecommend: List<Study>,
        @SerializedName("profile")
        val profile: Profile,
        @SerializedName("todo")
        val todo: List<Any>,
        @SerializedName("todoNum")
        val todoNum: Int,
        @SerializedName("userNum")
        val userNum: Int
    ) {

        data class MyStudy(
            @SerializedName("idx")
            val idx: Int,
            @SerializedName("img")
            val img: String,
            @SerializedName("is_leader")
            val isLeader: Boolean,
            @SerializedName("name")
            val name: String
        )

        data class Profile(
            @SerializedName("img")
            val img: String,
            @SerializedName("login_type")
            val loginType: String,
            @SerializedName("nickname")
            val nickname: String
        )

        data class Study(
            @SerializedName("category")
            val category: String,
            @SerializedName("idx")
            val idx: Int,
            @SerializedName("img")
            val img: String,
            @SerializedName("is_leader")
            val isLeader: Boolean,
            @SerializedName("is_pw")
            val isPw: Boolean,
            @SerializedName("max_num")
            val maxNum: Int,
            @SerializedName("name")
            val name: String,
            @SerializedName("type")
            val type: String,
            @SerializedName("user_num")
            val userNum: Int
        )

        data class Dream(
            @SerializedName("dday_date")
            val ddayDate: Any,
            @SerializedName("dday_name")
            val ddayName: Any,
            @SerializedName("goal")
            val goal: String
        )
    }
}