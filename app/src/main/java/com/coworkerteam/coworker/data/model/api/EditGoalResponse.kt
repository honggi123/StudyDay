package com.coworkerteam.coworker.data.model.api


import com.google.gson.annotations.SerializedName

data class EditGoalResponse(
    @SerializedName("message")
    val message: String,
    @SerializedName("result")
    val result: Result
){
    data class Result(
        @SerializedName("achieveTimeRate")
        val achieveTimeRate: Int,
        @SerializedName("aimTime")
        val aimTime: String,
        @SerializedName("dream")
        val dream: Dream
    ){
        data class Dream(
            @SerializedName("dday")
            val dday: String,
            @SerializedName("dday_date")
            val ddayDate: String,
            @SerializedName("dday_name")
            val ddayName: String,
            @SerializedName("goal")
            val goal: String
        )
    }
}