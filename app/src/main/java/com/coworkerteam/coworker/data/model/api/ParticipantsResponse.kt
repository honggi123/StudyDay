package com.coworkerteam.coworker.data.model.api


import com.google.gson.annotations.SerializedName

data class ParticipantsResponse(
    @SerializedName("maxNum")
    val maxNum: Int,
    @SerializedName("participantNum")
    val participantNum: Int,
    @SerializedName("participants")
    val participants: List<Participant>,
    @SerializedName("status")
    val status: String,
    @SerializedName("user")
    val user: String
){
    data class Participant(
        @SerializedName("img")
        val img: String,
        @SerializedName("is_leader")
        val isLeader: Boolean,
        @SerializedName("nickname")
        val nickname: String,
        @SerializedName("user_idx")
        val userIdx: Int
    )
}