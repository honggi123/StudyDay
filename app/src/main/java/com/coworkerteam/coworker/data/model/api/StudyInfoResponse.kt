package com.coworkerteam.coworker.data.model.api


import com.google.gson.annotations.SerializedName

data class StudyInfoResponse(
    @SerializedName("message")
    val message: String,
    @SerializedName("result")
    val result: Result
) {
    data class Result(
        @SerializedName("achieveTimeRate")
        val achieveTimeRate: Int,
        @SerializedName("aimTime")
        val aimTime: String,
        @SerializedName("dream")
        val dream: Dream,
        @SerializedName("studyInfo")
        val studyInfo: List<StudyInfo>,
        @SerializedName("todayTime")
        val todayTime: TodayTime
    ) {

        data class Dream(
            @SerializedName("dday")
            val dday: String?,
            @SerializedName("dday_date")
            val ddayDate: String?,
            @SerializedName("dday_name")
            val ddayName: String?
        )

        data class StudyInfo(
            @SerializedName("category")
            val category: String,
            @SerializedName("introduce")
            val introduce: String,
            @SerializedName("is_leader")
            val isLeader: String,
            @SerializedName("link")
            val link: String,
            @SerializedName("name")
            val name: String
        )

        data class TodayTime(
            @SerializedName("hour")
            val hour: Int,
            @SerializedName("min")
            val min: Int
        )
    }
}