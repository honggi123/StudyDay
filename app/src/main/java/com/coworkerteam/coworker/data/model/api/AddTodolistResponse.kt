package com.coworkerteam.coworker.data.model.api


import com.google.gson.annotations.SerializedName

data class AddTodolistResponse(
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
        val theDayAcheiveRate: Int,
        @SerializedName("theDayTodo")
        val theDayTodo: List<TodolistResponse.Result.TheDayTodo>,
        @SerializedName("todoDate")
        val todoDate: List<String>
    )
}