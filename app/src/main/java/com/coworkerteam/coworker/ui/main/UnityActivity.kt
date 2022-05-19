package com.coworkerteam.coworker.ui.main

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.coworkerteam.coworker.R
import com.coworkerteam.coworker.data.model.api.EnterCamstudyResponse
import com.coworkerteam.coworker.ui.camstudy.enter.EnterCamstudyActivity
import com.unity3d.player.UnityPlayer
import com.unity3d.player.UnityPlayerActivity

class UnityActivity : UnityPlayerActivity() {
    lateinit var mHandler: Handler
    lateinit var handler: Handler
    lateinit var mhandler: Handler

    lateinit var context: Context
    lateinit var roomlink : String
    var show : Boolean = true
    var dataIntent: EnterCamstudyResponse? = null
    lateinit var mBuilder : AlertDialog.Builder
    lateinit var builder : AlertDialog
    lateinit var thread : Thread

    override fun onCreate(savedInstanceState: Bundle?){
        Log.d("UnityActivity","oncreate")
        dataIntent = intent.getSerializableExtra("studyInfo") as EnterCamstudyResponse?
        var data = dataIntent
        context = this
        handler = object :  Handler(Looper.getMainLooper()){
            override fun handleMessage(msg: Message){
                Log.d("handleMessage : ","setData2")
                Log.d("Nickname : ",dataIntent!!.result.nickname)
                Log.d("Roomlink : ",dataIntent!!.result.studyInfo.link)
                //UnityPlayer.UnitySendMessage("Cube","move",dataIntent!!.result.nickname)
                UnityPlayer.UnitySendMessage("PlayerData","setNickname",dataIntent!!.result.nickname)
                UnityPlayer.UnitySendMessage("PlayerData","setRoomlink",dataIntent!!.result.studyInfo.link)
                super.handleMessage(msg)
            }
        }
        super.onCreate(savedInstanceState)
    }



    fun startStudy(str : String){
        var intent = Intent(this, EnterCamstudyActivity::class.java)
        intent.putExtra("studyInfo", dataIntent)
        startActivity(intent)
        finish()
    }

    fun exit(str : String){
        finish()
    }

    fun setData(str : String){
        Log.d("UnityActivity","setData")
        object : Thread() {
            override fun run() {
                handler.sendEmptyMessage(0);
            }
        }.start()
    }

    fun log(str : String){
        Log.d("UnityActivity : ","log")
    }


    fun endLoading(str : String){
        show = false
    }

    override fun onDestroy() {
        mUnityPlayer.quit()
        super.onDestroy()
    }


}


