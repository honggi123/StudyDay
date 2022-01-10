package com.coworkerteam.coworker.data.model.api

data class NaverResponse(
    var message:String,
    var response:Response
) {
    data class Response(
        var email:String,
        var nickname:String,
        var profile_image:String,
        var age:String,
        var gender:String,
        var id:String,
        var name:String,
        var birthday:String,
        var birthyear:String,
        var mobile:String
    )
}