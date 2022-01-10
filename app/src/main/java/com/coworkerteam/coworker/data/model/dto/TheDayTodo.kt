package com.coworkerteam.coworker.data.model.dto


import com.google.gson.annotations.SerializedName

data class TheDayTodo(
    @SerializedName("todo_date")
    val createDate: String,
    @SerializedName("idx")
    val idx: Int,
    @SerializedName("is_complete")
    val isComplete: Boolean,
    @SerializedName("todo")
    val todo: String
)