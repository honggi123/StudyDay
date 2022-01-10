package com.coworkerteam.coworker.data.model.api


import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class ProfileManageResponse(
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
        @SerializedName("isLeader")
        val isLeader: String,
        @SerializedName("profile")
        val profile: Profile
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

        data class Profile(
            @SerializedName("category")
            val category: String,
            @SerializedName("email")
            val email: String,
            @SerializedName("img")
            var img: String,
            @SerializedName("login_type")
            val loginType: String,
            @SerializedName("nickname")
            val nickname: String
        ) : Serializable
    }


}