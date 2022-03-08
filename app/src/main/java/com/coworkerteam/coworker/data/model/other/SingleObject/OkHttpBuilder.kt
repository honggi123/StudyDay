package com.coworkerteam.coworker.data.model.other.SingleObject

import okhttp3.OkHttpClient
import org.webrtc.PeerConnectionFactory

object OkHttpBuilder {
    private  var Builder = OkHttpClient.Builder()

    fun getbuilder(): OkHttpClient.Builder {
        if(Builder ==null){
            Builder = OkHttpClient.Builder()
        }
        return Builder
    }
}

