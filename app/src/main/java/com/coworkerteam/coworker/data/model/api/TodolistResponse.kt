package com.coworkerteam.coworker.data.model.api


import com.coworkerteam.coworker.data.model.dto.*
import com.google.gson.annotations.SerializedName

data class TodolistResponse(
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
    )
}