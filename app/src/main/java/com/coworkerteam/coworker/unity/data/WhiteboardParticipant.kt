package com.coworkerteam.coworker.unity.data

import android.content.Context

class WhiteboardParticipant (context: Context, name: String) {
    val TAG = "WhiteboardParticipant"

    lateinit var name: String
    var list : ArrayList<Path_info> = ArrayList()

}