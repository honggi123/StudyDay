package com.coworkerteam.coworker.data.model.api


import com.google.gson.annotations.SerializedName

data class CheckTodolistRequest(
    @SerializedName("message")
    val message: String,
    @SerializedName("result")
    val result: Result
){
    data class Result(
        @SerializedName("achieveTodoRate")
        val achieveTodoRate: Int,
        @SerializedName("selectedDate")
        val selectedDate: String,
        @SerializedName("theDayAcheiveRate")
        val theDayAcheiveRate: Int
    )
}