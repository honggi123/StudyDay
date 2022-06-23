package com.coworkerteam.coworker.ui.unity.data

import android.content.Context
import android.graphics.Path

class WhiteboardParticipant (context: Context, name: String) {
    val TAG = "WhiteboardParticipant"

    lateinit var name: String
    var list : ArrayList<Path_info> = ArrayList()

}