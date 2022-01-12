package com.coworkerteam.coworker.data.model.api


import com.google.gson.annotations.SerializedName

data class MainMyStudyPagingResponse(
    @SerializedName("message")
    val message: String,
    @SerializedName("result")
    val result: Result
){
    data class Result(
        @SerializedName("currentPage")
        val currentPage: Int,
        @SerializedName("myStudy")
        val myStudy: List<MyStudy>,
        @SerializedName("totalPage")
        val totalPage: Int
    ){
        data class MyStudy(
            @SerializedName("category")
            val category: String,
            @SerializedName("idx")
            val idx: Int,
            @SerializedName("img")
            val img: String,
            @SerializedName("is_leader")
            val isLeader: Boolean,
            @SerializedName("name")
            val name: String,
            @SerializedName("pw")
            val pw: Any
        )
    }
}