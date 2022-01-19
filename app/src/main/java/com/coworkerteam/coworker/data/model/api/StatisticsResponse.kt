package com.coworkerteam.coworker.data.model.api


import com.google.gson.annotations.SerializedName

data class StatisticsResponse(
    @SerializedName("achieveTimeRate")
    val achieveTimeRate: Int?,
    @SerializedName("achieveTodoRate")
    val achieveTodoRate: Int?,
    @SerializedName("aimTime")
    val aimTime: String,
    @SerializedName("dream")
    val dream: Dream,
    @SerializedName("profile")
    val profile: Profile,
    @SerializedName("restRate")
    val restRate: Int?,
    @SerializedName("selectDate")
    val selectDate: String,
    @SerializedName("studyRate")
    val studyRate: Int?,
    @SerializedName("studyTime")
    val studyTime: String,
    @SerializedName("theDayAcheiveRate")
    val theDayAcheiveRate: Int?,
    @SerializedName("theDayAcheiveTimeRate")
    val theDayAcheiveTimeRate: Int?,
    @SerializedName("weekTimeAVG")
    val weekTimeAVG: WeekTimeAVG,
    @SerializedName("weekTimeAcheive")
    val weekTimeAcheive: List<WeekTimeAcheive>,
    @SerializedName("weekTodoAVG")
    val weekTodoAVG: String,
    @SerializedName("weekTodoAcheive")
    val weekTodoAcheive: List<WeekTodoAcheive>,
    @SerializedName("monthTimeAVG")
    val monthTimeAVG: MonthTimeAVG,
    @SerializedName("monthTimeAcheive")
    val monthTimeAcheive: List<MonthTimeAcheive>,
    @SerializedName("monthTodoAVG")
    val monthTodoAVG: String,
    @SerializedName("monthTodoAcheive")
    val monthTodoAcheive: List<MonthTodoAcheive>
) {
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
        val img: String,
        @SerializedName("login_type")
        val loginType: String,
        @SerializedName("nickname")
        val nickname: String
    )

    data class WeekTimeAcheive(
        @SerializedName("date")
        val date: String,
        @SerializedName("hour")
        val hour: Float,
        @SerializedName("time")
        val time: String,
        @SerializedName("time_rate")
        val timeRate: Int
    )

    data class MonthTimeAcheive(
        @SerializedName("date")
        val date: String,
        @SerializedName("hour")
        val hour: Float,
        @SerializedName("time")
        val time: String,
        @SerializedName("time_rate")
        val timeRate: String
    )

    data class WeekTimeAVG(
        @SerializedName("hour")
        val hour: String,
        @SerializedName("min")
        val min: String
    )

    data class MonthTimeAVG(
        @SerializedName("hour")
        val hour: String,
        @SerializedName("min")
        val min: String
    )

    data class WeekTodoAcheive(
        @SerializedName("acheive_rate")
        val acheiveRate: String,
        @SerializedName("todo_date")
        val todoDate: String
    )

    data class MonthTodoAcheive(
        @SerializedName("acheive_rate")
        val acheiveRate: String,
        @SerializedName("date")
        val date: String
    )
}