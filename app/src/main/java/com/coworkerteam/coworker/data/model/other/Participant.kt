package com.coworkerteam.coworker.data.model.other

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Looper
import android.util.Log

import android.view.SurfaceView
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.coworkerteam.coworker.ui.camstudy.cam.CamStudyActivity
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import org.webrtc.*
import java.util.*
import java.util.logging.Handler
import kotlin.math.roundToInt

class Participant {
    val TAG = "Participant"

    var profileView: CircleImageView? = null

    //    lateinit var name: String
    var nickname: String = "hyunju"
    var img: String?= null
    var idx: Int? = null
    var is_leader: Boolean? = false
    var is_me: Boolean? = false
    var time: String = "00:00:00"
    var txt_time: TextView? = null

    var timer = Timer()
    var timerStratTime: Double = 0.0
    var timerPresentTime: Double = 0.0
    var timerRestTime: Double = 0.0
    var isStartTimer: Boolean? = null
    var playStateImage: ImageView? = null

    var image_mic: ImageView? = null

    var peer: PeerConnection? = null
    var surfaceView: org.webrtc.SurfaceViewRenderer? = null
    var remoteVideoTrack: VideoTrack? = null
    var remoteAudioTrack: AudioTrack? = null
    var isAudio: Boolean = true
    var isVideo: Boolean = true

    fun setImgUrl(imgUrl:String, context:Context){
        this.img = imgUrl
        if(profileView!=null){
            CoroutineScope(Dispatchers.Main).async {
                Glide.with(context).load(img).into(profileView!!)
            }
        }
    }

    fun setRender(surfaceView: SurfaceViewRenderer) {
        if (this.surfaceView != null) {
            remoteVideoTrack!!.removeRenderer(VideoRenderer(this.surfaceView))
        }

        this.surfaceView = surfaceView
    }

    fun startRender() {
        if (isVideo) {
            CoroutineScope(Dispatchers.Main).async {
                profileView?.visibility = View.GONE
            }
        } else {
            CoroutineScope(Dispatchers.Main).async {
                profileView?.visibility = View.VISIBLE
            }
        }
        remoteAudioTrack!!.setEnabled(isAudio)
        remoteVideoTrack!!.setEnabled(isVideo)
        remoteVideoTrack!!.addRenderer(VideoRenderer(surfaceView))
    }

    fun toggleAudio(status: String) {
        if (status.equals("off")) {
            Log.d("Participant", "toggleAudio() 끔")
            isAudio = false
            Log.d("Participant", isAudio.toString())
            remoteAudioTrack!!.setEnabled(isAudio)
            image_mic?.isSelected = true
        } else if (status.equals("on")) {
            Log.d("Participant", "toggleAudio() 킴")
            isAudio = true
            Log.d("Participant", isAudio.toString())
            remoteAudioTrack!!.setEnabled(isAudio)
            image_mic?.isSelected = false
        }
//        if (isAudio) {
//            Log.d("Participant", "toggleAudio() 끔")
//            isAudio = false
//            Log.d("Participant", isAudio.toString())
//            remoteAudioTrack!!.setEnabled(isAudio)
//            image_mic?.isSelected = true
//        } else {
//            Log.d("Participant", "toggleAudio() 킴")
//            isAudio = true
//            Log.d("Participant", isAudio.toString())
//            remoteAudioTrack!!.setEnabled(isAudio)
//            image_mic?.isSelected = false
//        }
    }

    fun toggleVideo(status: String) {
        if (status.equals("off")) {
            Log.d("Participant", "toggleVideo() 끔")
            isVideo = false
            CoroutineScope(Dispatchers.Main).async {
                profileView?.visibility = View.VISIBLE
            }
            remoteVideoTrack!!.setEnabled(isVideo)
        } else if (status.equals("on")) {
            Log.d("Participant", "toggleVideo() 킴")
            isVideo = true
            CoroutineScope(Dispatchers.Main).async {
                profileView?.visibility = View.GONE
            }
            remoteVideoTrack!!.setEnabled(isVideo)
        }
//        if (isVideo) {
//            Log.d("Participant", "toggleVideo() 끔")
//            isVideo = false
//            CoroutineScope(Dispatchers.Main).async {
//                profileView?.visibility = View.VISIBLE
//            }
//            remoteVideoTrack!!.setEnabled(isVideo)
//        } else {
//            Log.d("Participant", "toggleVideo() 킴")
//            isVideo = true
//            CoroutineScope(Dispatchers.Main).async {
//                profileView?.visibility = View.GONE
//            }
//            remoteVideoTrack!!.setEnabled(isVideo)
//        }
    }

    fun startHostTimer() {
        if (isStartTimer == null) {
            timer = Timer()
            isStartTimer = true
            if (timerPresentTime > 0.0) {
                val time = timerPresentTime
                timer.scheduleAtFixedRate(TimeTask(time), 0, 1000)
            } else {
                val time = timerStratTime
                timer.scheduleAtFixedRate(TimeTask(time), 0, 1000)
            }
            playStateImage?.isSelected = false
        } else if (isStartTimer == false) {
            timer.cancel()
            timer = Timer()

            isStartTimer = true
            if (timerPresentTime > 0.0) {
                val time = timerPresentTime
                timer.scheduleAtFixedRate(TimeTask(time), 0, 1000)
            } else {
                val time = timerStratTime
                timer.scheduleAtFixedRate(TimeTask(time), 0, 1000)
            }
            playStateImage?.isSelected = false
        }
    }

    fun stopHostTimer() {
        if (isStartTimer == true) {
            timer.cancel()

            isStartTimer = false
            playStateImage?.isSelected = true
            timer = Timer()
            timer.scheduleAtFixedRate(TimeTask(timerRestTime), 0, 1000)
        }
    }

    fun setTimer(status: String) {
        if (status.equals("run")) {

            timer = Timer()
            isStartTimer = true
            if (timerPresentTime > 0.0) {
                val time = timerPresentTime
                timer.scheduleAtFixedRate(TimeTask(time), 0, 1000)
            } else {
                val time = timerStratTime
                timer.scheduleAtFixedRate(TimeTask(time), 0, 1000)
            }
            playStateImage?.isSelected = false

        } else if (status.equals("pause")) {
            timer.cancel()

            isStartTimer = false
            playStateImage?.isSelected = true
        }
    }

    fun getTimerStatus() : String {
        if(isStartTimer == true){
            return "run"
        }else{
            return "pause"
        }
    }

    private inner class TimeTask(private var time: Double) : TimerTask() {
        override fun run() {
            time++
            Log.d(TAG, time.toString())

            CoroutineScope(Dispatchers.Main).async {
                if (isStartTimer == true) {
                    setTextTime(time)
                } else if (isStartTimer == false) {
                    setRestTime(time)
                }
            }
        }
    }

    fun setTextTime(time: Double) {
        timerPresentTime = time

        var timeString = getTimeStringFromDouble(timerPresentTime)
        this.time = timeString
        txt_time?.text = timeString
    }

    fun setRestTime(time: Double) {
        timerRestTime = time
    }

    fun setStudyTime(time: Int) {
        timerStratTime = time.toDouble()
        this.time = getTimeStringFromDouble(timerStratTime)
    }

    fun stopCamStduy() {
        if (isStartTimer != null) {
            timer.cancel()
        }
        remoteVideoTrack?.removeRenderer(VideoRenderer(surfaceView))
    }

    private fun getTimeStringFromDouble(time: Double): String {
        val resultInt = time.roundToInt()
        val hours = resultInt % 86400 / 3600
        val minutes = resultInt % 86400 % 3600 / 60
        val seconds = resultInt % 86400 % 3600 % 60

        return makeTimeString(hours, minutes, seconds)
    }

    private fun makeTimeString(hour: Int, min: Int, sec: Int): String =
        String.format("%02d:%02d:%02d", hour, min, sec)
}