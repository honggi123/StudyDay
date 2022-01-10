package com.coworkerteam.coworker.data.model.api


import com.google.gson.annotations.SerializedName

data class EditTodolistResponse(
    @SerializedName("message")
    val message: String,
    @SerializedName("result")
    val result: Result
){
    data class Result(
        @SerializedName("selectedDate")
        val selectedDate: String,
        @SerializedName("theDayTodo")
        val theDayTodo: List<TheDayTodo>
    ){
        data class TheDayTodo(
            @SerializedName("idx")
            val idx: Int,
            @SerializedName("is_complete")
            val isComplete: String,
            @SerializedName("todo")
            val todo: String,
            @SerializedName("todo_date")
            val todoDate: String
        )
    }
}