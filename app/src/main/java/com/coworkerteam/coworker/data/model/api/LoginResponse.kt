package com.coworkerteam.coworker.data.model.api

data class LoginResponse(
    var message:String,
    var result:List<LoginResult>
) {
    data class LoginResult(
        var isInterest:Boolean,
        var accessToken:String,
        var refreshToken:String,
        var nickname:String
    )
}
