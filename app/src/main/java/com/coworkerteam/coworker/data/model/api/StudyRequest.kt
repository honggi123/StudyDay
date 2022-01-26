package com.coworkerteam.coworker.data.model.api

data class StudyRequest(
    var message: String,
    var result: Result
) {
    data class Result(
        var pw: String,
        var studyIdx: Int
    )
}
