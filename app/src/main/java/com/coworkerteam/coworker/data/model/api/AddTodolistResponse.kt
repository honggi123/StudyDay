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
        val theDayTodo: List<TheDayTodo>,
        @SerializedName("todoDate")
        val todoDate: List<String>
    )

    data class TheDayTodo(
        @SerializedName("idx")
        val idx: Int,
        @SerializedName("is_complete")
        val isComplete: Boolean,
        @SerializedName("todo")
        val todo: String,
        @SerializedName("todo_date")
        val todoDate: String
    )
}