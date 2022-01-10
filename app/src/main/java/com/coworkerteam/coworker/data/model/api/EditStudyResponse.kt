package com.coworkerteam.coworker.data.model.api


import com.google.gson.annotations.SerializedName

data class EditStudyResponse(
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
        @SerializedName("profile")
        val profile: Profile,
        @SerializedName("studyInfo")
        val studyInfo: StudyInfo
    ){
        data class Dream(
            @SerializedName("dday_date")
            val ddayDate: Any,
            @SerializedName("dday_name")
            val ddayName: Any,
            @SerializedName("goal")
            val goal: String
        )

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


        data class StudyInfo(
            @SerializedName("category")
            val category: String,
            @SerializedName("img")
            val img: String,
            @SerializedName("introduce")
            val introduce: String,
            @SerializedName("max_num")
            val maxNum: Int,
            @SerializedName("name")
            val name: String,
            @SerializedName("pw")
            val pw: String?,
            @SerializedName("type")
            val type: String
        )
    }
}