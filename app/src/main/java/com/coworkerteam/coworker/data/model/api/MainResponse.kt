package com.coworkerteam.coworker.data.model.api


import com.google.gson.annotations.SerializedName

data class MainResponse(
    @SerializedName("message")
    val message: String,
    @SerializedName("result")
    val result: List<Result>
){
    data class Result(
        @SerializedName("achieveTimeRate")
        var achieveTimeRate: Int?,
        @SerializedName("achieveTodoRate")
        var achieveTodoRate: Int?,
        @SerializedName("aimTime")
        var aimTime: String,
        @SerializedName("dream")
        var dream: Dream,
        @SerializedName("groupRecommend")
        val groupRecommend: List<Study>,
        @SerializedName("myStudy")
        val myStudy: List<MyStudy>,
        @SerializedName("myStudyPage")
        val myStudyPage: Int,
        @SerializedName("newGroupStudy")
        val newGroupStudy: List<Study>,
        @SerializedName("newOpenStudy")
        val newOpenStudy: List<Study>,
        @SerializedName("openRecommend")
        val openRecommend: List<Study>,
        @SerializedName("profile")
        val profile: Profile,
        @SerializedName("studyTime")
        val studyTime: String,
        @SerializedName("todo")
        val todo: List<Todo>,
        @SerializedName("todoNum")
        val todoNum: Int,
        @SerializedName("userNum")
        val userNum: Int
    ){
        data class Dream(
            @SerializedName("dday")
            val dday: String?,
            @SerializedName("dday_date")
            var ddayDate: String?,
            @SerializedName("dday_name")
            var ddayName: String?,
            @SerializedName("goal")
            var goal: String?
        )
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
            @SerializedName("max_num")
            val maxNum: Int,
            @SerializedName("name")
            val name: String,
            @SerializedName("pw")
            val pw: String?,
            @SerializedName("type")
            val type: String,
            @SerializedName("user_num")
            val userNum: Int
        )
        data class MyStudy(
            @SerializedName("idx")
            val idx: Int,
            @SerializedName("img")
            val img: String,
            @SerializedName("is_leader")
            val isLeader: Boolean,
            @SerializedName("name")
            val name: String,
            @SerializedName("pw")
            val pw: String?
        )
        data class Profile(
            @SerializedName("img")
            val img: String,
            @SerializedName("login_type")
            val loginType: String,
            @SerializedName("nickname")
            val nickname: String
        )
        data class Todo(
            @SerializedName("idx")
            val idx: Int,
            @SerializedName("is_complete")
            val isComplete: Boolean,
            @SerializedName("todo")
            val todo: String,
            @SerializedName("todo_date")
            val todoDate: String
        )
    }
}