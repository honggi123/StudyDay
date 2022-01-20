package com.coworkerteam.coworker.data.model.api

import com.google.gson.annotations.SerializedName

data class TodolistResponse(
    @SerializedName("message")
    val message: String,
    @SerializedName("result")
    val result: Result
) {
    data class Result(
        @SerializedName("achieveTimeRate")
        val achieveTimeRate: Int,
        @SerializedName("achieveTodoRate")
        val achieveTodoRate: Int,
        @SerializedName("aimTime")
        val aimTime: String,
        @SerializedName("dream")
        val dream: Dream,
        @SerializedName("profile")
        val profile: Profile,
        @SerializedName("selectedDate")
        val selectedDate: String,
        @SerializedName("studyTime")
        val studyTime: String,
        @SerializedName("theDayAcheiveRate")
        val theDayAcheiveRate: Int,
        @SerializedName("theDayTodo")
        var theDayTodo: List<TheDayTodo>,
        @SerializedName("todoDate")
        val todoDate: List<String>
    ) {
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

        data class TheDayTodo(
            @SerializedName("todo_date")
            val createDate: String,
            @SerializedName("idx")
            val idx: Int,
            @SerializedName("is_complete")
            var isComplete: Boolean,
            @SerializedName("todo")
            val todo: String
        )
    }
}