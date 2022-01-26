package com.coworkerteam.coworker.data.model.api


import com.google.gson.annotations.SerializedName

data class StudySearchResponse(
    @SerializedName("message")
    val message: String,
    @SerializedName("result")
    val result: Result
){
    data class Result(
        @SerializedName("currentPage")
        val currentPage: Int,
        @SerializedName("keyword")
        val keyword: String,
        @SerializedName("resultNum")
        val resultNum: Int,
        @SerializedName("study")
        val study: List<Study>,
        @SerializedName("totalPage")
        val totalPage: Int
    ){
        data class Study(
            @SerializedName("category")
            val category: String,
            @SerializedName("idx")
            val idx: Int,
            @SerializedName("img")
            val img: String,
            @SerializedName("is_leader")
            val isLeader: Boolean,
            @SerializedName("is_pw")
            val isPw: Boolean,
            @SerializedName("pw")
            val pw: String?,
            @SerializedName("max_num")
            val maxNum: Int,
            @SerializedName("name")
            val name: String,
            @SerializedName("type")
            val type: String,
            @SerializedName("user_num")
            val userNum: Int
        )
    }
}