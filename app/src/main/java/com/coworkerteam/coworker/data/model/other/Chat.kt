package com.coworkerteam.coworker.data.model.other

data class Chat(
    var type:String,
    var sender:String,
    var receiver:String?,
    var msg:String,
    var time:String
)
