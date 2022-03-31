package com.coworkerteam.coworker.data.model.api

import com.google.gson.annotations.SerializedName

class MainRankResponse (
    @SerializedName("message")
    val message: String,
    @SerializedName("result")
    val result: Result
){
    data class Result(
        @SerializedName("rank")
        val rank: List<Rank>,
        @SerializedName("myRank")
        val myRank: MyRank,
        @SerializedName("totalRank")
        val totalRank: Int,
    ){
        data class Rank(
            @SerializedName("nickname")
            val nickname: String,
            @SerializedName("time")
            val time: String,
            @SerializedName("rank")
            val rank: Int,
        )
        data class MyRank(
            @SerializedName("user_idx")
            val user_idx: Int,
            @SerializedName("time")
            val time: String,
            @SerializedName("rank")
            val rank: String,
            @SerializedName("percent")
            val percent: String,
        )
    }

}
