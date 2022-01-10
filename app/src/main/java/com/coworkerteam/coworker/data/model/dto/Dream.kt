package com.coworkerteam.coworker.data.model.dto


import com.google.gson.annotations.SerializedName

data class Dream(
    @SerializedName("dday_date")
    val ddayDate: String?,
    @SerializedName("dday_name")
    val ddayName: String?,
    @SerializedName("goal")
    val goal: String?,
    @SerializedName("dday")
    val dday: String?
)