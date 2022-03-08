package com.coworkerteam.coworker.data.model.other.SingleObject

import org.webrtc.PeerConnectionFactory

object SinglePeerConnectionFactory {
    private  var factory : PeerConnectionFactory? = null

    fun getfactory(): PeerConnectionFactory{
        if(factory==null){
            factory = PeerConnectionFactory(null)
        }
        return factory as PeerConnectionFactory
    }

    fun destroyfactory(){
        if(factory != null){
            factory!!.dispose()
        }

    }


}