package com.coworkerteam.coworker.data.model.api

data class MoodPostResponse(
    val message: String,
    val result: List<Result>
){
    data class Result(
        val achieveTimeRate: Int,
        val achieveTodoRate: Int?,
        val currentPage: Int,
        val dream: Dream,
        val isLeader: String,
        val profile: Profile,
        val moodPosts: ArrayList<MoodPost>,
        val totalPage: Int
    ){
        data class Dream(
            val dday: String?,
            val dday_date: String?,
            val dday_name: String?,
            val goal: String
        )

        data class Profile(
            val category: String,
            val email: String,
            val img: String,
            val login_type: String,
            val nickname: String
        )

        data class MoodPost(
            val idx: Int,
            val mood: Int,
            val nickname: String,
            val create_date: String,
            val contents: String,
            var is_empathy: String,
            var empathy_kinds: String,
            var total_empathy: Int
        )
    }
}

