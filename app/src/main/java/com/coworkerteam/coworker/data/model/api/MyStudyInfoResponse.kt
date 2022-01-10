package com.coworkerteam.coworker.data.model.api


import com.google.gson.annotations.SerializedName

data class MyStudyInfoResponse(
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
        @SerializedName("todayTime")
        val todayTime: TodayTime
    ) {
//        data class AimTime(
//            @SerializedName("hour")
//            val hour: Int,
//            @SerializedName("min")
//            val min: Int
//        )

        data class TodayTime(
            @SerializedName("hour")
            val hour: Int,
            @SerializedName("min")
            val min: Int
        )

        data class Dream(
            @SerializedName("dday")
            val dday: String?,
            @SerializedName("dday_date")
            val ddayDate: String?,
            @SerializedName("dday_name")
            val ddayName: String?
        )
    }
}