package com.coworkerteam.coworker.data.model.api


import com.google.gson.annotations.SerializedName

data class MyStudyDailyPagingResponse(
    @SerializedName("message")
    val message: String,
    @SerializedName("result")
    val result: Result
){
    data class Result(
        @SerializedName("currentPage")
        val currentPage: Int,
        @SerializedName("open")
        val open: List<Open>,
        @SerializedName("totalPage")
        val totalPage: Int
    ){
        data class Open(
            @SerializedName("category")
            val category: String,
            @SerializedName("idx")
            val idx: Int,
            @SerializedName("img")
            val img: String,
            @SerializedName("is_leader")
            val isLeader: Boolean,
            @SerializedName("is_pw")
            val isPw: String,
            @SerializedName("max_num")
            val maxNum: Int,
            @SerializedName("name")
            val name: String,
            @SerializedName("pw")
            val pw: String?,
            @SerializedName("user_num")
            val userNum: Int
        )
    }
}