package com.coworkerteam.coworker.data.model.api

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class EnterCamstudyResponse(
    @SerializedName("message")
    val message: String,
    @SerializedName("result")
    val result: Result
) : Serializable {
    data class Result(
        @SerializedName("studyInfo")
        val studyInfo: StudyInfo,
        @SerializedName("userImg")
        val userImg: String
        ,@SerializedName("nickname")
        val nickname: String
    ) : Serializable {
        data class StudyInfo(
            @SerializedName("category")
            val category: String,
            @SerializedName("introduce")
            val introduce: String,
            @SerializedName("join_num")
            val joinNum: Int,
            @SerializedName("link")
            val link: String,
            @SerializedName("max_num")
            val maxNum: Int,
            @SerializedName("name")
            val name: String,
            @SerializedName("type")
            val type: String,
            @SerializedName("user_num")
            val userNum: Int,
            var idx: Int?
        ) : Serializable
    }
}