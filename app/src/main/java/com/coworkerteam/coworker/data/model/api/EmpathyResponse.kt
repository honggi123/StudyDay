package com.coworkerteam.coworker.data.model.api


import com.google.gson.annotations.SerializedName

data class EmpathyResponse(
    @SerializedName("empathy")
    val empathy: Empathy,
    @SerializedName("message")
    val message: String
){
    data class Empathy(
        @SerializedName("moodPost")
        val moodPost: List<MoodPost>
    )
    data class MoodPost(
        @SerializedName("contents")
        val contents: String,
        @SerializedName("create_date")
        val createDate: String,
        @SerializedName("empathy_kinds")
        val empathyKinds: String,
        @SerializedName("idx")
        val idx: Int,
        @SerializedName("is_empathy")
        val isEmpathy: String,
        @SerializedName("mood")
        val mood: Int,
        @SerializedName("nickname")
        val nickname: String,
        @SerializedName("total_empathy")
        val totalEmpathy: Int,
        @SerializedName("my_empathy")
        val my_empathy: Int
    )
}