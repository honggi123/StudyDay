package com.coworkerteam.coworker.data.local.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjection
import android.os.*
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.coworkerteam.coworker.R
import com.coworkerteam.coworker.data.local.prefs.AppPreferencesHelper
import com.coworkerteam.coworker.data.local.prefs.PreferencesHelper
import com.coworkerteam.coworker.data.model.api.EnterCamstudyResponse
import com.coworkerteam.coworker.data.model.api.ParticipantsResponse
import com.coworkerteam.coworker.data.model.other.ChatData
import com.coworkerteam.coworker.data.model.other.Participant
import com.coworkerteam.coworker.data.model.other.SingleObject.OkHttpBuilder
import com.coworkerteam.coworker.data.model.other.SingleObject.SinglePeerConnectionFactory
import com.coworkerteam.coworker.ui.camstudy.cam.CamStudyActivity
import com.coworkerteam.coworker.ui.main.VoiceRecorder
import com.google.gson.Gson
import com.konovalov.vad.VadConfig
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import okhttp3.OkHttpClient
import org.json.JSONException
import org.json.JSONObject
import org.webrtc.*
import java.net.URISyntaxException
import java.security.KeyManagementException
import java.security.NoSuchAlgorithmException
import java.security.cert.X509Certificate
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager


class CamStudyService : Service(),
    VoiceRecorder.Listener{
    val TAG = "CamStudyService"

    val VIDEO_TRACK_ID = "ARDAMSv0"
    val VIDEO_RESOLUTION_WIDTH = 1280
    val VIDEO_RESOLUTION_HEIGHT = 720
    val FPS = 30

    val NOTIFICATION_ID = 1004

    var socket: Socket? = null

    var participantMe : Participant? = null

    lateinit var hostname: String
    lateinit var room: String
    var instance: String? = null

    lateinit var screenUserName_whenstart : String

    var videosource :VideoSource? = null

    lateinit var audioConstraints: MediaConstraints
    var videoCapturer: VideoCapturer? = null
    var audioSource: AudioSource? = null
    var localAudioTrack: AudioTrack? = null
    var okHttpClient : OkHttpClient? = null
    var factory : PeerConnectionFactory? = null

    private var recorder: VoiceRecorder? = null

    private val DEFAULT_SAMPLE_RATE = VadConfig.SampleRate.SAMPLE_RATE_8K
    private val DEFAULT_FRAME_SIZE = VadConfig.FrameSize.FRAME_SIZE_80
    private val DEFAULT_MODE = VadConfig.Mode.NORMAL

    private val DEFAULT_SILENCE_DURATION = 500
    private val DEFAULT_VOICE_DURATION = 500

    var speakStatus : Boolean = false
    var noiseStatus : Boolean = false

    companion object {
        const val MSG_CLIENT_CONNECT = 0
        const val MSG_CLIENT_DISCNNECT = 1
        const val MSG_SERVICE_CONNECT = 2
        const val MSG_TOTAL_MESSAGE = 3
        const val MSG_WHISPER_MESSAGE = 4
        const val MSG_HOST_VIDEO_ON_OFF = 5
        const val MSG_HOST_AUDIO_ON_OFF = 6
        const val MSG_SWITCH_CAMERA = 7
        const val MSG_HOST_TIMER_RUN = 8
        const val MSG_HOST_TIMER_PAUSE = 9
        const val MSG_COMSTUDY_LEFT = 10
        const val MSG_LEADER_FORCED_EXIT = 11
        const val MSG_LEADER_FORCED_AUDIO_OFF = 12
        const val MSG_LEADER_FORCED_VIDEO_OFF = 13
        const val MSG_RECEIVED_MESSAGE = 14
        const val MSG_PARTICIPANTS_ITEM = 15
        const val MSG_EXISTINGPARTICIPANNTS = 16
        const val MSG_NEWPARTICIPANTARRIVED = 17
        const val MSG_PARTICIPANTLEFT = 18
        const val MSG_SERVICE_FINISH = 19

        // 화면공유 변수
        lateinit var screencaptureintent : Intent
        const val REQUEST_MEDIA_PROJECTION = 20
        const val REQUEST_SCREEN_SHARE = 21
        const val REQUEST_STOP_SHARE = 22
        const val RECEIVE_STOP_SHARE = 22
        var onScreen: Boolean? = false

        var isVideo: Boolean? = null
        var isAudio: Boolean? = null
        var isPermissions = false
        var isPlay = true
        var timer: Int? = null
        var forcedexit : Boolean = false

        lateinit var rootEglBase: EglBase
        lateinit var rootEglBaseScreen: EglBase

        var chatDate = ArrayList<ChatData>()

        var participantsResponses: ParticipantsResponse? = null
        var peerConnection = HashMap<String, Participant>()
    }


    private var mClientCallbacks = ArrayList<Messenger>()
    val mMessenger = Messenger(CallbackHandler(Looper.getMainLooper()))

    var videoTrackFromCamera: VideoTrack? = null
    var localvideoTrack : VideoTrack? = null
    var videoShareTrackFromCamera:VideoTrack? = null
    var audioShareTrack: AudioTrack? = null
    var trackTrackingThread : Thread? = null

    //서비스가 시작될 때 호출
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG,"service 시작")
        if (socket == null) {
            val notification = notification.createNotification(this)
            startForeground(NOTIFICATION_ID, notification)
        }

        var config = VadConfig.newBuilder()
            .setSampleRate(DEFAULT_SAMPLE_RATE)
            .setFrameSize(DEFAULT_FRAME_SIZE)
            .setMode(DEFAULT_MODE)
            .setSilenceDurationMillis(DEFAULT_SILENCE_DURATION)
            .setVoiceDurationMillis(DEFAULT_VOICE_DURATION)
            .build()

        recorder = VoiceRecorder(this, config)

        return START_STICKY
    }


    //bindService()로 바인딩을 실행할 때 호출
    override fun onBind(intent: Intent): IBinder {
        if (socket == null) {
            getData(intent)
        }
        return mMessenger.binder
    }

    //unbindService()로 바인딩을 해제할 때 호출
    override fun onUnbind(intent: Intent?): Boolean {
        return super.onUnbind(intent)
        Log.d(TAG,"onUnbind")
    }

    //이미 onUnbind()가 호출된 후에 bindService()로 바인딩을 실행할 때 호출
    override fun onRebind(intent: Intent?){
        super.onRebind(intent)
    }

    //서비스가 소멸될 때 호출
    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG,"onDestroy")
        if(recorder != null){
            recorder?.stop()
            recorder = null
        }
        rootEglBase.release()
        // webrtc 관련
        if(videoTrackFromCamera != null){
            videoTrackFromCamera = null
        }

        if(localAudioTrack != null){
            localAudioTrack = null
        }

        if(videoCapturer != null){
            videoCapturer!!.dispose()
            videoCapturer = null
        }

        if(videosource != null){
            videosource!!.dispose()
            videosource = null
        }

        if(audioSource != null){
            audioSource!!.dispose()
            audioSource = null
        }

        peerConnection.keys.forEach{
            peerConnection.get(it)?.stopCamStduy()
            peerConnection.get(it)?.itemViewScreen?.surfaceView?.release()
            Log.d(TAG,"peerconnection.stopcamstudy")
        }

        val handlerMessage = Message.obtain(null, MSG_SERVICE_FINISH)
        sendHandlerMessage(handlerMessage)

        peerConnection.clear()
        mClientCallbacks.clear()

        chatDate.clear()
        if(socket != null){
            socket?.disconnect()
            socket = null
        }

        onScreen = false
    }


    private fun startCamStudy() {
        connectToSignallingServer()
        initializePeerConnectionFactory()
        if(isPermissions){
            createVideoTrackFromCameraAndShowIt()
        }else{
            getParticipant(hostname)
        }
        initializePeerConnections(hostname)
        if (isPermissions){
            startStreamingVideo(hostname)
        }
        socket!!.connect()
        localvideoTrack = videoTrackFromCamera
    }



    private fun getData(intent: Intent?) {
        var enterCamstudyResponse =
            intent!!.getSerializableExtra("studyInfo") as EnterCamstudyResponse

        var pref: PreferencesHelper = AppPreferencesHelper(this, "studyday")
        hostname = pref.getCurrentUserName()!!
        room = enterCamstudyResponse.result.studyInfo.link
        instance = intent.getStringExtra("instance")
    }

    private fun connectToSignallingServer() {
        try {
            val myHostnameVerifier =
                HostnameVerifier { hostname, session -> true }
            val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
                override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {}

                override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {}

                override fun getAcceptedIssuers(): Array<X509Certificate> {
                    return arrayOf()
                }
            })
            var mySSLContext: SSLContext? = null
            try {
                mySSLContext = SSLContext.getInstance("TLS")
                try {
                    mySSLContext.init(null, trustAllCerts, null)
                } catch (e: KeyManagementException) {
                    e.printStackTrace()
                }
            } catch (e: NoSuchAlgorithmException) {
                e.printStackTrace()
            }
            val okHttpClient =
                OkHttpBuilder.getbuilder().hostnameVerifier(myHostnameVerifier).sslSocketFactory(
                    mySSLContext!!.socketFactory
                ).build()

            // default settings for all sockets
            IO.setDefaultOkHttpWebSocketFactory(okHttpClient)
            IO.setDefaultOkHttpCallFactory(okHttpClient)

            // set as an option
            val opts = IO.Options()
            opts.transports = arrayOf(io.socket.engineio.client.transports.WebSocket.NAME)
            opts.callFactory = okHttpClient
            opts.webSocketFactory = okHttpClient

            // $ hostname -I
            var URL = getString(R.string.midea_url)

            if(instance != null){
                URL += "?id=$instance"
            }
            Log.e(TAG, "REPLACE ME: IO Socket:$URL")
            socket = IO.socket(URL, opts)
            socket!!.on(
                Socket.EVENT_CONNECT,
                Emitter.Listener { args: Array<Any> ->
                    //소켓이 연결 됨
                    Log.d(TAG, "connectToSignallingServer: connect")
                    sendMessage(getSendMessage("joinRoom"))
                })
                .on(Socket.EVENT_RECONNECT,
                    Emitter.Listener { args: Array<Any> ->
                        //재연결 처리
                        Log.d(TAG, "reconnectToSignallingServer: reconnect")
                        sendMessage(getSendMessage("reconnectJoinRoom"))
                    })

                .on(Socket.EVENT_CONNECT_ERROR,
                    Emitter.Listener { args: Array<Any> ->
                        Log.d(TAG, "소켓연결 에러")

                    }).on("message",
                    Emitter.Listener { args: Array<Any> ->
                        try {
                            if (args[0] is String) {
                            } else {
                                val message = args[0] as JSONObject
                                Log.d(TAG, "connectToSignallingServer: got message $message")

                                when (message.getString("id")) {
                                    "reconnect failed" -> {
                                        //재연결 처리 실패
                                        Log.d(TAG, "reconnect failed")
                                        val handlerMessage = Message.obtain(null, MSG_COMSTUDY_LEFT)
                                        mMessenger.send(handlerMessage)
                                    }
                                    "existingParticipants" -> {
                                        //맨 처음 방에 들어갔을때
                                        Log.d(TAG, "existingParticipants")

                                        //미디어 서버에서 넘겨준 스터디 참가자 '전원'의 정보가 담긴 객체로 매핑
                                        var participantsResponse =
                                            Gson().fromJson(
                                                message.getString("entry"),
                                                ParticipantsResponse::class.java
                                            )

                                        //매핑된 참여자리스트 전역변수에 저장
                                        participantsResponses = participantsResponse

                                        //참여자 프로필 사진을 세팅
                                        participantsResponse.participants.forEach {
                                            Log.d(TAG,"nickname : " + it.nickname)
                                            foreach(it.nickname)
                                            peerConnection.get(it.nickname)?.itemView?.setProfileImage(
                                                it.img
                                            )
                                        }

                                        if(!message.getString("screenUserName").toString().equals("null")){
                                            foreachscreen(message.getString("screenUserName"))

                                            //CamStudyActivity의 비디오 Item을 그려주는 리사이클러뷰 다시 그리기
                                            val handlerMessage =
                                                Message.obtain(null, REQUEST_SCREEN_SHARE)
                                            var bundle = Bundle()
                                            bundle.putString("name", message.getString("screenUserName"))
                                            handlerMessage.data = bundle

                                            sendHandlerMessage(handlerMessage)
                                            onScreen = true
                                        }else{
                                            val handlerMessage =
                                                Message.obtain(null, MSG_EXISTINGPARTICIPANNTS)
                                            sendHandlerMessage(handlerMessage)
                                        }
                                    }

                                    "newParticipantArrived" -> {
                                        //새로운 참가자가 들어왔을때
                                        Log.d(TAG, "newParticipantArrived")
                                        val name = message.getString("name")
                                        foreach(name)

                                        //CamStudyActivity의 비디오 Item을 그려주는 리사이클러뷰 다시 그리기
                                        val handlerMessage =
                                            Message.obtain(null, MSG_NEWPARTICIPANTARRIVED)
                                        sendHandlerMessage(handlerMessage)

                                        //미디어 서버에서 넘겨준 스터디 참가자 '전원'의 정보가 담긴 객체로 매핑
                                        var participantsResponse =
                                            Gson().fromJson(
                                                message.getString("entry"),
                                                ParticipantsResponse::class.java
                                            )

                                        participantsResponses = participantsResponse

                                        //참여자 정보를 확인하고 있었다면, 다시 그려주기 -> ParticipantsActivity
                                        val handlerMessageParticipants =
                                            Message.obtain(null, MSG_PARTICIPANTS_ITEM)
                                        sendHandlerMessage(handlerMessageParticipants)

                                        //새로운 참여자의 프로필을 참여자 전원의 정보가 있는 객체에서 찾아서 넣는다.
                                        participantsResponse.participants.forEach {
                                            if (it.nickname.equals(name)) {
                                                getParticipant(message.getString("name")).itemView.setProfileImage(
                                                    it.img
                                                )
                                                return@forEach
                                            }
                                        }

                                        //안드로이드에서 참여자의 정보를 저장한 객체를 불러옴
                                        val par = getParticipant(hostname)

                                        //새로운 참여자가 오면 기존참여자인 '나'는 그 사람에게 스탑워치에 대한 정보를 넘겨준다.
                                        val message_send = JSONObject()

                                        message_send.put("id", "sendStopwatchTime")
                                        message_send.put("stopwatchTime", par.timer.timerStudyTime)
                                        message_send.put("stopwatchStatus", par.timer.getTimerStatus())
                                        message_send.put("sender", hostname)
                                        message_send.put("receiver", message.getString("name"))
                                        message_send.put(
                                            "videoStatus",
                                            if (isVideo == true) "on" else "off"
                                        )
                                        message_send.put(
                                            "audioStatus",
                                            if (isAudio == true) "on" else "off"
                                        )
                                        sendMessage(message_send)
                                    }

                                    "participantLeft" -> {
                                        //참여자가 방을 떠났을 경우
                                        getParticipant(message.getString("name")).stopCamStduy()
                                        peerConnection.remove(message.getString("name"))

                                        val handlerMessage = Message.obtain(null, MSG_PARTICIPANTLEFT)
                                        sendHandlerMessage(handlerMessage)

                                        //참여자 목록에 대한 정보에서 나간사람 빼기
                                        val refreshParticipantsResponses =
                                            participantsResponses!!.participants.toMutableList()

                                        var item: ParticipantsResponse.Participant? = null

                                        refreshParticipantsResponses.forEach {
                                            if (it.nickname.equals(message.getString("name"))) {
                                                item = it
                                                return@forEach
                                            }
                                        }

                                        refreshParticipantsResponses.remove(item)

                                        participantsResponses!!.participants =
                                            refreshParticipantsResponses

                                        //참여자 정보를 확인하고 있었다면, 다시 그려주기
                                        val handlerMessageParticipants =
                                            Message.obtain(null, MSG_PARTICIPANTS_ITEM)
                                        sendHandlerMessage(handlerMessageParticipants)
                                    }
                                    "receiveVideoAnswer" -> {
                                        //Answer을 받았을 경우
                                        Log.d(TAG, "receiveVideoAnswer")
                                        peerConnection.get(message.getString("name"))?.peer
                                            ?.setRemoteDescription(
                                                SimpleSdpObserver(),
                                                SessionDescription(
                                                    SessionDescription.Type.ANSWER,
                                                    message.getString("sdpAnswer")
                                                )
                                            )
                                    }
                                    "iceCandidate" -> {
                                        //iceCandidate를 받았을 경우
                                        Log.d(TAG, "iceCandidate")
                                        val candidatedate =
                                            JSONObject(message.getString("candidate"))

                                        val candidate = IceCandidate(
                                            candidatedate.getString("sdpMid"),
                                            candidatedate.getInt("sdpMLineIndex"),
                                            candidatedate.getString("candidate")
                                        )
                                        peerConnection.get(message.getString("name"))?.peer
                                            ?.addIceCandidate(candidate)
                                    }
                                    "receivedMessage" -> {
                                        //채팅 받는 이벤트
                                        Log.d(TAG, "receivedMessage")
                                        val receiver =
                                            if (message.getString("type").equals("total")) null else "나"

                                        chatDate.add(
                                            ChatData(
                                                message.getString("type"),
                                                message.getString("sender"),
                                                receiver,
                                                message.getString("msg"),
                                                message.getString("time")
                                            )
                                        )
                                        val handlerMessage: Message =
                                            Message.obtain(null, CamStudyService.MSG_RECEIVED_MESSAGE)
                                        sendHandlerMessage(handlerMessage)
                                    }
                                    "requestStopwatchTime" -> {
                                        //기존 참여자가 새로운 참여자의 타이머 시간 받기
                                        Log.d(TAG, "requestStopwatchTime")
                                        var par = getParticipant(message.getString("receiver"))
                                        par.timer.init(message.getInt("stopwatchTime").toDouble())
                                        par.toggleAudio(message.getString("audioStatus"))
                                        par.toggleVideo(message.getString("videoStatus"))

                                        par.timer.setOtherTimer("run")
                                    }
                                    "receiveStopwatchTime" -> {
                                        //새로운 참여자가 캠스터디에 참여하고 있는 모든 참여자들의 타이머 시간/ 스탑워치 상태를 받는다
                                        Log.d(TAG, "receiveStopwatchTime")
                                        var par = getParticipant(message.getString("sender"))
                                        par.timer.init(message.getInt("stopwatchTime").toDouble())
                                        par.timer.setOtherTimer(message.getString("stopwatchStatus"))
                                        par.toggleAudio(message.getString("audioStatus"))
                                        par.toggleVideo(message.getString("videoStatus"))
                                    }
                                    "receiveStopwatchStatus" -> {
                                        //다른 참여자 타이머 상태 받기
                                        Log.d(TAG, "receiveStopwatchStatus")
                                        getParticipant(message.getString("sender")).timer.setOtherTimer(
                                            message.getString("stopwatchStatus")
                                        )
                                    }
                                    "receiveForcedexit" -> {
                                        //강퇴 당하기
                                        Log.d(TAG, "receive forcedexit")
                                        forcedexit = true
                                        val handlerMessage: Message =
                                            Message.obtain(null, CamStudyService.MSG_LEADER_FORCED_EXIT)
                                        sendHandlerMessage(handlerMessage)
                                    }
                                    "receiveForcedDeviceOff" -> {
                                        //리더가 내 미디어 장치 강제로 OFF 시킴
                                        Log.d(TAG, "receiveForcedDeviceOff")
                                        var par = getParticipant(message.getString("receiver"))

                                        if (message.getString("device").equals("video")) {
                                            par.toggleVideo(message.getString("status"))
                                            if (message.getString("receiver").equals(hostname)) {
                                                val handlerMessage: Message = Message.obtain(
                                                    null,
                                                    CamStudyService.MSG_LEADER_FORCED_VIDEO_OFF
                                                )
                                                sendHandlerMessage(handlerMessage)
                                            }
                                        } else if (message.getString("device").equals("audio")) {
                                            par.toggleAudio(message.getString("status"))
                                            if (message.getString("receiver").equals(hostname)) {
                                                val handlerMessage: Message = Message.obtain(
                                                    null,
                                                    CamStudyService.MSG_LEADER_FORCED_AUDIO_OFF
                                                )
                                                sendHandlerMessage(handlerMessage)

                                                recorder?.stop()
                                                if(speakStatus){
                                                    speakStatus = false
                                                    noiseStatus = true
                                                }
                                                var hmessage = mHandler.obtainMessage()
                                                var bundle = Bundle()
                                                bundle.putString("id", "receiveStopRecognition")
                                                bundle.putString("sender", hostname)
                                                hmessage.data = bundle
                                                mHandler.sendMessage(hmessage)

                                            }
                                        }
                                    }
                                    "receiveDeviceSwitch" -> {
                                        //다른 사람의 비디오 ON/OFF 상태값 수신
                                        //(내가 비디오 상태값을 서버에 보내도 나도 데이터를 수신한다)
                                        val message = args[0] as JSONObject
                                        Log.d(
                                            TAG,
                                            "receive device switch::::::::::::::::::::::::" + message.toString()
                                        )
                                        var par = getParticipant(message.getString("sender"))

                                        if (message.getString("device").equals("video")) {
                                            par.toggleVideo(message.getString("status"))
                                        } else if (message.getString("device").equals("audio")) {
                                            par.toggleAudio(message.getString("status"))
                                        }
                                    }
                                    "receiveStartRecognition" -> {
                                        // 음성인식 시작
                                        Log.d(TAG,"receiveStartRecognition")
                                        var hmessage = mHandler.obtainMessage()
                                        var bundle = Bundle()
                                        bundle.putString("id", "receiveStartRecognition")
                                        bundle.putString("sender", message.getString("sender"))
                                        hmessage.data = bundle
                                        mHandler.sendMessage(hmessage)
                                    }
                                    "receiveStopRecognition" -> {
                                        // 음성인식 정지
                                        Log.d(TAG,"receiveStopRecognition")
                                        var hmessage = mHandler.obtainMessage()
                                        var bundle = Bundle()
                                        bundle.putString("id", "receiveStopRecognition")
                                        bundle.putString("sender", message.getString("sender"))
                                        hmessage.data = bundle
                                        mHandler.sendMessage(hmessage)
                                    }
                                    "startScreenOffer" -> {
                                        //화면공유 시작
                                        myscreen(message.getString("name"))
                                        foreachscreen(message.getString("name"))

                                        //CamStudyActivity의 비디오 Item을 그려주는 리사이클러뷰 다시 그리기
                                        val handlerMessage =
                                            Message.obtain(null, REQUEST_SCREEN_SHARE)
                                        var bundle = Bundle()

                                        if(message.getString("name").equals(hostname)){
                                            bundle.putBoolean("shareHost", true)
                                        }else{
                                            bundle.putBoolean("shareHost", false)
                                        }
                                        bundle.putString("name", message.getString("name"))
                                        handlerMessage.data = bundle

                                        sendHandlerMessage(handlerMessage)

                                        onScreen = true
                                    }
                                    "receiveScreenAnswer" ->{
                                        //Answer을 받았을 경우
                                        Log.d(TAG, "receiveScreenAnswer")
                                        peerConnection.get(message.getString("name"))?.peerScreen?.setRemoteDescription(
                                            SimpleSdpObserver(),
                                            SessionDescription(
                                                SessionDescription.Type.ANSWER,
                                                message.getString("sdpAnswer")
                                            )
                                        )
                                    }
                                    "iceCandidateScreen" -> {
                                        //iceCandidate를 받았을 경우
                                        val candidatedate =
                                            JSONObject(message.getString("candidate"))

                                        val candidate = IceCandidate(
                                            candidatedate.getString("sdpMid"),
                                            candidatedate.getInt("sdpMLineIndex"),
                                            candidatedate.getString("candidate")
                                        )
                                        peerConnection.get(message.getString("name"))?.peerScreen?.addIceCandidate(candidate)

                                        Log.d(TAG, "iceCandidateScreen")
                                    }
                                    "newParticipantScreenArrived" -> {
                                        Log.d(TAG,"newParticipantScreenArrived")
                                        foreachscreen(message.getString("name"))

                                        //CamStudyActivity의 비디오 Item을 그려주는 리사이클러뷰 다시 그리기
                                        val handlerMessage =
                                            Message.obtain(null, REQUEST_SCREEN_SHARE)
                                        var bundle = Bundle()
                                        bundle.putString("name", message.getString("name"))
                                        handlerMessage.data = bundle

                                        sendHandlerMessage(handlerMessage)
                                        onScreen = true
                                    }
                                    "receiveStopScreen" -> {
                                        // 화면공유 중지
                                        peerConnection[message.getString("name")]?.stopCamStduyScreen()

                                        //CamStudyActivity의 비디오 Item을 그려주는 리사이클러뷰 다시 그리기
                                        val handlerMessage =
                                            Message.obtain(null, RECEIVE_STOP_SHARE)
                                        var bundle = Bundle()
                                        bundle.putString("name", message.getString("name"))
                                        handlerMessage.data = bundle

                                        sendHandlerMessage(handlerMessage)

                                    }

                                }
                            }
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                    }).on(io.socket.client.Socket.EVENT_DISCONNECT,
                    Emitter.Listener { args: Array<Any?>? ->
                        Log.d(
                            TAG,
                            "connectToSignallingServer: disconnect"
                        )
                    })
        } catch (e: URISyntaxException) {
            Log.e(TAG, "연결이 안됨")
            e.printStackTrace()
        }
    }

    private fun existingShareParticipants(name: String) {
        val sdpMediaConstraints = MediaConstraints()
        sdpMediaConstraints.mandatory.add(
            MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true")
        )
        sdpMediaConstraints.mandatory.add(
            MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true")
        )
        peerConnection.get(name)?.peerScreen?.createOffer(object : SimpleSdpObserver() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            override fun onCreateSuccess(sessionDescription: SessionDescription) {
                Log.d(TAG, "onCreateSuccessScreen: ")
                peerConnection.get(name)?.peerScreen
                    ?.setLocalDescription(SimpleSdpObserver(), sessionDescription)
                val message = JSONObject()
                try {
                    message.put("id", "receiveScreenFrom")
                    message.put("sender", name)
                    message.put("sdpOffer", sessionDescription.description)
                    Log.d(
                        TAG,
                        sessionDescription.description
                    )
                    sendMessage(message)
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
        }, sdpMediaConstraints)
    }

    private fun existingParticipants(name: String) {
        val sdpMediaConstraints = MediaConstraints()
        sdpMediaConstraints.mandatory.add(
            MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true")
        )
        sdpMediaConstraints.mandatory.add(
            MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true")
        )
        peerConnection[name]?.peer?.createOffer(object : SimpleSdpObserver() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            override fun onCreateSuccess(sessionDescription: SessionDescription) {
                Log.d(TAG, "onCreateSuccess: ")
                peerConnection[name]?.peer
                    ?.setLocalDescription(SimpleSdpObserver(), sessionDescription)
                val message = JSONObject()
                try {
                    message.put("id", "receiveVideoFrom")
                    message.put("sender", name)
                    message.put("sdpOffer", sessionDescription.description)
                    Log.d(
                        TAG,
                        sessionDescription.description
                    )
                    sendMessage(message)
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
        }, sdpMediaConstraints)
    }

    private fun foreach(name: String) {
        //initializePeerConnectionFactory()
        initializePeerConnections(name)
        startStreamingVideo(name)
        existingParticipants(name)
    }

    private fun myscreen(name: String) {
        initializeSharePeerConnections(name)
        createVideoTrackFromCameraAndShowItScreen(name)
        startStreamingVideoScreen(name)
    }

    private fun foreachscreen(name: String) {
        if (hostname != name){
            initializeSharePeerConnections(name)
            startStreamingVideoScreen(name)
        }
        existingShareParticipants(name)
    }

    private fun initializeSharePeerConnections(name: String) {
        var peer = factory?.let { createSharePeerConnection(it, name) }

        if (peer != null) {
            var part = getParticipant(name)
            part?.peerScreen = peer
            Log.d(TAG, name + "의 screenshare 추가")
        }
    }

    private fun createVideoTrackFromCameraAndShowItScreen(name: String){
        Log.d(TAG, "startStreamingShareVideo()")
        val mediaStream: MediaStream = factory!!.createLocalMediaStream("ARDAMS")

        audioConstraints = MediaConstraints()
        var videoShareCapturer = ScreenCapturerAndroid(
            screencaptureintent,
            object : MediaProjection.Callback() {
                override fun onStop() {
                    super.onStop()
                    Log.e(TAG, "User has revoked media projection permissions")
                }
            })

        var videosource = factory?.createVideoSource(videoShareCapturer)

        videoShareCapturer?.startCapture(
            VIDEO_RESOLUTION_WIDTH,
            VIDEO_RESOLUTION_HEIGHT,
            FPS
        )

        videoShareTrackFromCamera = factory?.createVideoTrack(
            VIDEO_TRACK_ID,
            videosource
        )

        //create an AudioSource instance

        var audioSource = factory?.createAudioSource(audioConstraints)
        audioShareTrack = factory?.createAudioTrack("102", audioSource)
        audioShareTrack?.setEnabled(false)
        if (isPermissions) {
            mediaStream.addTrack(videoShareTrackFromCamera)
            mediaStream.addTrack(audioShareTrack)
        }

        //  peerConnection.get(name)?.startRenderScreen(videoShareTrackFromCamera, localAudioTrack)
    }

    private fun startStreamingVideoScreen(name: String){
        Log.d(TAG, "startStreamingVideoScreen()")
        val mediaStream: MediaStream = factory!!.createLocalMediaStream("ARDAMS")
        if (isPermissions) {
            if (hostname != name){
                mediaStream.addTrack(videoTrackFromCamera)
                var audioSource = factory?.createAudioSource(audioConstraints)
                audioShareTrack = factory?.createAudioTrack("102", audioSource)
                audioShareTrack?.setEnabled(false)
            }else{
                mediaStream.addTrack(videoShareTrackFromCamera)
            }
            mediaStream.addTrack(audioShareTrack)
        }
        peerConnection[name]?.peerScreen?.addStream(mediaStream)
    }


    private fun sendMessage(message: JSONObject){
        try {
            Log.d(TAG, "sendMessage: $message")
            socket!!.emit("message", message)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    private fun initializePeerConnectionFactory() {
        PeerConnectionFactory.initializeAndroidGlobals(this, true, true, true)
        //factory = PeerConnectionFactory(null)
        factory = SinglePeerConnectionFactory.getfactory()

        factory?.setVideoHwAccelerationOptions(
            rootEglBase?.eglBaseContext,
            rootEglBase?.eglBaseContext
        )
    }

    private fun createVideoTrackFromCameraAndShowIt() {
        Log.d("디버그태그", "createVideoTrackFromCameraAndShowIt")
        audioConstraints = MediaConstraints()
        videoCapturer = createVideoCapturer()

        videosource = factory?.createVideoSource(videoCapturer)

        videoCapturer?.startCapture(
            VIDEO_RESOLUTION_WIDTH,
            VIDEO_RESOLUTION_HEIGHT,
            FPS
        )

        videoTrackFromCamera = factory?.createVideoTrack(
            VIDEO_TRACK_ID,
            videosource
        )

        //create an AudioSource instance
        audioSource = factory?.createAudioSource(audioConstraints)
        localAudioTrack = factory?.createAudioTrack("101", audioSource)

        var participant = getParticipant(hostname)
        participant.remoteVideoTrack = videoTrackFromCamera
        participant.remoteAudioTrack = localAudioTrack

        participant.startRender(videoTrackFromCamera, localAudioTrack)
    }

    private fun makeMe(): Participant {
        participantMe = Participant(this, hostname)

        if(isAudio == true){
            recorder!!.start()
        }
        participantMe!!.settingDevice(isVideo!!, isAudio!!)
        participantMe!!.timer.init(timer!!.toDouble());
        participantMe!!.timer.startStudyTimer()
        participantMe!!.timer.setTextTime(timer!!.toDouble())

        return participantMe as Participant
    }

    private fun getParticipant(name: String): Participant {
        var participant = peerConnection.get(name)
        Log.d(TAG," : getParticipant")

        if (participant != null) {
            Log.d(TAG," paticipant : notnull")
            return participant
        }else if (name == hostname){
            Log.d(TAG," name = hostname")
            participant = makeMe()
            peerConnection[name] = participant
            return participant
        }else {
            participant = Participant(this, name)
            peerConnection[name] = participant
            return participant
        }
    }

    private fun initializePeerConnections(name: String) {
        var peer = factory?.let { createPeerConnection(it, name) }

        if (name != hostname) {
            var part = getParticipant(name)
            part.peer = peer
            Log.d(TAG, name + "의 participant 객체 추가(peerConnection)")
        } else {
            peerConnection.get(name)?.peer = peer
            Log.d(TAG, name + "의 participant 객체 추가(peerConnection)")
        }
    }




    private fun startStreamingVideo(name: String){
        Log.d(TAG, "startStreamingVideo()")
        val mediaStream: MediaStream = factory!!.createLocalMediaStream("ARDAMS")
        if (isPermissions) {
            mediaStream.addTrack(videoTrackFromCamera)
            mediaStream.addTrack(localAudioTrack)
        }
        peerConnection[name]?.peer?.addStream(mediaStream)
    }


    private fun createPeerConnection(
        factory: PeerConnectionFactory,
        name: String
    ): PeerConnection? {
        val iceServers = ArrayList<PeerConnection.IceServer>()
        val stunURL = getString(R.string.midea_stun_url)
        val turnURL = getString(R.string.midea_turn_url)
        iceServers.add(PeerConnection.IceServer(stunURL))
        iceServers.add(PeerConnection.IceServer(turnURL, "kurento", "kurento"))
        val rtcConfig = PeerConnection.RTCConfiguration(iceServers)
        val pcConstraints = MediaConstraints()

        val pcObserver: PeerConnection.Observer = object : PeerConnection.Observer {
            override fun onSignalingChange(signalingState: PeerConnection.SignalingState) {
                Log.d(TAG, "onSignalingChange: ")
            }

            override fun onIceConnectionChange(iceConnectionState: PeerConnection.IceConnectionState) {
                Log.d(
                    TAG,
                    "onIceConnectionChange: "
                )
            }

            override fun onIceConnectionReceivingChange(b: Boolean) {
                Log.d(
                    TAG,
                    "onIceConnectionReceivingChange: "
                )
            }

            override fun onIceGatheringChange(iceGatheringState: PeerConnection.IceGatheringState) {
                Log.d(
                    TAG,
                    "onIceGatheringChange: "
                )
            }

            override fun onIceCandidate(iceCandidate: IceCandidate) {
                Log.d(TAG, "onIceCandidate: ")
                val message = JSONObject()
                val candidate = JSONObject()
                try {
                    candidate.put("candidate", iceCandidate.sdp)
                    candidate.put("sdpMid", iceCandidate.sdpMid)
                    candidate.put("sdpMLineIndex", iceCandidate.sdpMLineIndex)
                    message.put("id", "onIceCandidate")
                    message.put("candidate", candidate)
                    message.put("sender", name)
                    Log.d(
                        TAG,
                        "onIceCandidate: sending candidate $message"
                    )
                    sendMessage(message)
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }

            override fun onIceCandidatesRemoved(iceCandidates: Array<IceCandidate>) {
                Log.d(TAG, "onIceCandidatesRemoved: ")
            }

            override fun onAddStream(mediaStream: MediaStream) {
                Log.d(TAG, "onAddStream: ")
                val remoteVideoTrack = mediaStream.videoTracks[0]
                val remoteAudioTrack = mediaStream.audioTracks[0]

                if (name != hostname) {
                    peerConnection[name]?.startRender(remoteVideoTrack, remoteAudioTrack)
                }
            }

            override fun onRemoveStream(mediaStream: MediaStream) {
                Log.d(TAG, "onRemoveStream: ")
            }

            override fun onDataChannel(dataChannel: DataChannel) {
                Log.d(TAG, "onDataChannel: ")
            }

            override fun onRenegotiationNeeded() {
                Log.d(
                    TAG,
                    "onRenegotiationNeeded: "
                )
            }
        }
        return factory.createPeerConnection(rtcConfig, pcConstraints, pcObserver)
    }

    private fun createSharePeerConnection(
        factory: PeerConnectionFactory,
        name: String
    ): PeerConnection? {
        val iceServers = ArrayList<PeerConnection.IceServer>()
        val stunURL = getString(R.string.midea_stun_url)
        val turnURL = getString(R.string.midea_turn_url)
        iceServers.add(PeerConnection.IceServer(stunURL))
        iceServers.add(PeerConnection.IceServer(turnURL, "kurento", "kurento"))
        val rtcConfig = PeerConnection.RTCConfiguration(iceServers)
        val pcConstraints = MediaConstraints()

        val pcObserver: PeerConnection.Observer = object : PeerConnection.Observer {
            override fun onSignalingChange(signalingState: PeerConnection.SignalingState) {
                Log.d(TAG, "onSignalingScreenChange: ")
            }

            override fun onIceConnectionChange(iceConnectionState: PeerConnection.IceConnectionState) {
                Log.d(
                    TAG,
                    "onIceConnectionChange: "
                )
            }

            override fun onIceConnectionReceivingChange(b: Boolean) {
                Log.d(
                    TAG,
                    "onIceConnectionReceivingChange: "
                )
            }

            override fun onIceGatheringChange(iceGatheringState: PeerConnection.IceGatheringState) {
                Log.d(
                    TAG,
                    "onIceGatheringChange: "
                )
            }

            override fun onIceCandidate(iceCandidate: IceCandidate) {
                Log.d(TAG, "onIceCandidateScreen: ")
                val message = JSONObject()
                val candidate = JSONObject()
                try {
                    candidate.put("candidate", iceCandidate.sdp)
                    candidate.put("sdpMid", iceCandidate.sdpMid)
                    candidate.put("sdpMLineIndex", iceCandidate.sdpMLineIndex)
                    message.put("id", "onIceCandidateScreen")
                    message.put("candidate", candidate)
                    message.put("sender", name)
                    Log.d(
                        TAG,
                        "onIceCandidate: sending candidate $message"
                    )
                    sendMessage(message)
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }

            override fun onIceCandidatesRemoved(iceCandidates: Array<IceCandidate>) {
                Log.d(TAG, "onIceCandidatesRemoved: ")
            }

            override fun onAddStream(mediaStream: MediaStream) {
                Log.d(TAG, "onAddStreamScreen: ")

                val remoteVideoTrack = mediaStream.videoTracks[0]
                val remoteAudioTrack = mediaStream.audioTracks[0]
                if (name != hostname) {
                    peerConnection.get(name)?.startRenderScreen(remoteVideoTrack, remoteAudioTrack)
                }
            }

            override fun onRemoveStream(mediaStream: MediaStream) {
                Log.d(TAG, "onRemoveStream: ")
            }

            override fun onDataChannel(dataChannel: DataChannel) {
                Log.d(TAG, "onDataChannel: ")
            }

            override fun onRenegotiationNeeded() {
                Log.d(
                    TAG,
                    "onRenegotiationNeeded: "
                )
            }
        }
        return factory.createPeerConnection(rtcConfig, pcConstraints, pcObserver)
    }


    private fun getSendMessage(event: String): JSONObject {
        val message = JSONObject()
        when (event) {
            "joinRoom" -> {
                message.put("id", "joinRoom")
                message.put("name", hostname)
                message.put("roomName", room)
                message.put("stopwatchTime", timer)
                message.put("videoStatus", if (isVideo == true) "on" else "off")
                message.put("audioStatus", if (isAudio == true) "on" else "off")
            }
            "reconnectJoinRoom" -> {
                message.put("id", "reconnectJoinRoom")
                message.put("name", hostname)
                message.put("roomName", room)
                message.put("stopwatchTime", timer)
                message.put("videoStatus", if (isVideo == true) "on" else "off")
                message.put("audioStatus", if (isAudio == true) "on" else "off")
            }
        }
        Log.d(TAG,"name : " + hostname)
        return message
    }

    private fun switchCamera() {
        Log.d(TAG," : switchCamera")
        if (videoCapturer != null) {
            if (videoCapturer is CameraVideoCapturer) {
                val cameraVideoCapturer = videoCapturer as CameraVideoCapturer
                participantMe!!.horizontalflipcamera()
                cameraVideoCapturer.switchCamera(null)
            } else {
                // Will not switch camera, video capturer is not a camera
            }
        }
    }

    private fun createVideoCapturer(): VideoCapturer? {
        return createCameraCapturer(Camera1Enumerator(true))
    }

    private fun createCameraCapturer(enumerator: CameraEnumerator): VideoCapturer? {
        val deviceNames = enumerator.deviceNames
        for (deviceName in deviceNames) {
            if (enumerator.isFrontFacing(deviceName)) {
                val videoCapturer: VideoCapturer? = enumerator.createCapturer(deviceName, null)
                if (videoCapturer != null) {
                    return videoCapturer
                }
            }
        }
        for (deviceName in deviceNames) {
            if (!enumerator.isFrontFacing(deviceName)) {
                val videoCapturer: VideoCapturer? = enumerator.createCapturer(deviceName, null)
                if (videoCapturer != null) {
                    return videoCapturer
                }
            }
        }
        return null
    }


    // 갤럭시 특정 기기 카메라 전환 시 camera2 api에서 오류 이슈 발생으로 인해 사용 하지 않음
    private fun useCamera2(): Boolean {
        return Camera2Enumerator.isSupported(this)
    }


    private fun sendHandlerMessage(msg: Message) {
        if (mClientCallbacks.size > 0) {
            try {
                //bind된 Activicy들에게 메시지 전송
                mClientCallbacks.forEach {
                    val snedMsg = Message()
                    snedMsg.copyFrom(msg)
                    it.send(snedMsg)
                }
            } catch (e: RemoteException) {
                e.printStackTrace()
            }
            Log.d(TAG, "Send MSG_ADD_VALUE message to Service ")
        }
    }

    inner class CallbackHandler(looper: Looper) : Handler(looper) {
        override fun handleMessage(msg: Message) {
            Log.d(TAG, "Service에 메세지 도착")
            when (msg.what) {
                MSG_CLIENT_CONNECT -> {
                    Log.d(TAG, "Received MSG_CLIENT_CONNECT message from client");
                    mClientCallbacks.add(msg.replyTo);

                    if (socket == null) {
                        startCamStudy()
                    }

                    val handlerMessage =
                        Message.obtain(null, MSG_SERVICE_CONNECT)
                    sendHandlerMessage(handlerMessage)
                }
                MSG_CLIENT_DISCNNECT -> {
                    Log.d(TAG, "Received MSG_CLIENT_DISCONNECT message from client");
                    mClientCallbacks.remove(msg.replyTo);
                }
                MSG_TOTAL_MESSAGE -> {
                    //모두에게 채팅 보내기
                    val message = JSONObject()
                    val chatting = msg.obj as Bundle
                    message.put("id", "sendMessage")
                    message.put("room", room)
                    message.put("sender", hostname)
                    message.put("msg", chatting.getString("msg"))
                    sendMessage(message)
                }
                MSG_WHISPER_MESSAGE -> {
                    //귓속말 보내기
                    val message = JSONObject()
                    val chatting = msg.obj as Bundle
                    message.put("id", "whisperMessage")
                    message.put("room", room)
                    message.put("sender", hostname)
                    message.put("msg", chatting.getString("msg"))
                    message.put("receiver", chatting.getString("receiver"))
                    sendMessage(message)
                }
                MSG_HOST_AUDIO_ON_OFF -> {
                    //내 마이크 on/off
                    peerConnection[hostname]!!.toggleAudio(msg.obj as String)
                    val message = JSONObject()
                    message.put("id", "sendDeviceSwitch")
                    message.put("room", room)
                    message.put("sender", hostname)
                    message.put("device", "audio")
                    message.put("status", msg.obj as String)
                    sendMessage(message)

                    if(msg.obj as String == "off"){
                        recorder?.stop()
                        if(speakStatus){
                            speakStatus = false
                            noiseStatus = true
                            val message_send = JSONObject()
                            message_send.put("id", "sendStopRecognition")
                            message_send.put("room", room)
                            message_send.put("sender", hostname)
                            sendMessage(message_send)
                        }
                    }else{
                        Log.d(TAG,"recorder.start")
                        recorder?.start()
                    }
                }
                MSG_HOST_VIDEO_ON_OFF -> {
                    //내 카메라 on/off
                    peerConnection[hostname]!!.toggleVideo(msg.obj as String)

                    val message = JSONObject()
                    message.put("id", "sendDeviceSwitch")
                    message.put("room", room)
                    message.put("sender", hostname)
                    message.put("device", "video")
                    message.put("status", msg.obj as String)
                    sendMessage(message)
                }
                MSG_SWITCH_CAMERA -> {
                    //카메라 좌우 반전
                    switchCamera()
                }
                MSG_HOST_TIMER_RUN -> {
                    //내 타이머 상태를 다른 참여자들에게 보낸다 (타이머 시작)
                    peerConnection[hostname]!!.timer.startStudyTimer()
                    val message = JSONObject()
                    message.put("id", "sendStopwatchStatus")
                    message.put("room", room)
                    message.put("sender", hostname)
                    message.put("stopwatchStatus", "run")
                    sendMessage(message)
                }
                MSG_HOST_TIMER_PAUSE -> {
                    //내 타이머 상태를 다른 참여자들에게 보낸다 (타이머 멈춤)
                    peerConnection[hostname]!!.timer.startRestTimer()
                    val message = JSONObject()
                    message.put("id", "sendStopwatchStatus")
                    message.put("room", room)
                    message.put("sender", hostname)
                    message.put("stopwatchStatus", "pause")
                    sendMessage(message)
                }
                MSG_COMSTUDY_LEFT -> {
                    //퇴장처리
                    Log.d(TAG,"MSG_COMSTUDY_LEFT")
                    val handlerMessage = Message.obtain(null, MSG_COMSTUDY_LEFT)

                    var timerResult =
                        peerConnection[hostname]!!.timer.timerStudyTime - peerConnection[hostname]!!.timer.timerStratTime
                    handlerMessage.arg1 = timerResult.toInt()
                    handlerMessage.arg2 = peerConnection[hostname]!!.timer.timerRestTime.toInt()

                    sendHandlerMessage(handlerMessage)
                }
                MSG_LEADER_FORCED_EXIT -> {
                    //리더가 멤버 스터디 추방
                    val message = JSONObject()
                    message.put("id", "sendForcedExit")
                    message.put("receiver", msg.obj as String)
                    sendMessage(message)
                }
                MSG_LEADER_FORCED_AUDIO_OFF -> {
                    //멤버 리더가 마이크 off
                    val message = JSONObject()
                    message.put("id", "sendForcedDeviceOff")
                    message.put("room", room)
                    message.put("receiver", msg.obj as String)
                    message.put("device", "audio")
                    sendMessage(message)
                }
                MSG_LEADER_FORCED_VIDEO_OFF -> {
                    //멤버 리더가 카메라 off
                    val message = JSONObject()
                    message.put("id", "sendForcedDeviceOff")
                    message.put("room", room)
                    message.put("receiver", msg.obj as String)
                    message.put("device", "video")
                    sendMessage(message)
                }
                REQUEST_MEDIA_PROJECTION -> {
                    // 화면공유 시작
                    val message = JSONObject()
                    Log.d(TAG,"REQUEST_MEDIA_PROJECTION")
                    message.put("id", "sendStartScreen")
                    message.put("name", hostname)
                    message.put("room", room)
                    sendMessage(message)
                }
                REQUEST_STOP_SHARE -> {
                    // 화면공유 중지
                    val message = JSONObject()
                    Log.d(TAG,"REQUEST_STOP_SHARE")
                    message.put("id", "sendStopScreen")
                    message.put("room", room)
                    message.put("name", hostname)
                    sendMessage(message)

                }

            }
        }
    }

    override fun onSpeechDetected() {
        if(speakStatus==false){
            Log.d(TAG,"onSpeechDetected")
            speakStatus = true
            val message_send = JSONObject()
            message_send.put("id", "sendStartRecognition")
            message_send.put("room", room)
            message_send.put("sender", hostname)
            sendMessage(message_send)
        }else{
            noiseStatus = false
        }
    }

    override fun onNoiseDetected() {

        if(noiseStatus==false){
            Log.d(TAG,"onNoiseDetected")

            val message_send = JSONObject()
            message_send.put("id", "sendStopRecognition")
            message_send.put("room", room)
            message_send.put("sender", hostname)
            sendMessage(message_send)
            noiseStatus = true
        }else{
            speakStatus = false
        }
    }

    val mHandler: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            var bundle = msg.data
            if (bundle.getString("id") == "receiveStartRecognition") {
                getParticipant(bundle.getString("sender")!!).itemView.changeHighlight(true)
            }else{
                getParticipant(bundle.getString("sender")!!).itemView.changeHighlight(false)
            }
        }
    }

}


object notification {
    const val CHANNEL_ID = "foreground_service_channel" // 임의의 채널 ID
    fun createNotification(
        context: Context
    ): Notification {
        // 알림 클릭시 MainActivity로 이동됨
        val notificationIntent = Intent(context, CamStudyActivity::class.java)
        notificationIntent.putExtra("audio", CamStudyService.isAudio)
        notificationIntent.putExtra("video", CamStudyService.isVideo)
        notificationIntent.putExtra("studyInfo", CamStudyActivity.studyInfo)
        notificationIntent.putExtra("timer", CamStudyService.timer)
//      notificationIntent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP

        val pendingIntent = PendingIntent
            .getActivity(context, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        // Oreo 부터는 Notification Chan3nel을 만들어야 함
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "StudyDay", // 채널표시명
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = context.getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(serviceChannel)
        }

        // 알림
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("StudyDay")
            .setContentText("캠스터디 진행중")
            .setSmallIcon(R.mipmap.ic_studyday)
            .setOngoing(true) // true 일경우 알림 리스트에서 클릭하거나 좌우로 드래그해도 사라지지 않음
            //  .setContentIntent(pendingIntent)
            .build()

        return notification
    }



}