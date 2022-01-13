package com.coworkerteam.coworker.data.local.service

import android.app.*
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.*
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.coworkerteam.coworker.R
import com.coworkerteam.coworker.data.local.prefs.AppPreferencesHelper
import com.coworkerteam.coworker.data.local.prefs.PreferencesHelper
import com.coworkerteam.coworker.data.model.api.EnterCamstudyResponse
import com.coworkerteam.coworker.data.model.api.ParticipantsResponse
import com.coworkerteam.coworker.data.model.other.CamStudyServiceData
import com.coworkerteam.coworker.data.model.other.Chat
import com.coworkerteam.coworker.data.model.other.Participant
import com.coworkerteam.coworker.ui.camstudy.cam.CamStudyActivity
import com.google.gson.Gson
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import okhttp3.OkHttpClient
import org.json.JSONException
import org.json.JSONObject
import org.webrtc.*
import java.net.URISyntaxException
import java.security.KeyManagementException
import java.security.NoSuchAlgorithmException
import java.security.cert.X509Certificate
import java.util.ArrayList
import java.util.HashMap
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager
import org.webrtc.CameraVideoCapturer

class CamStudyService : Service() {
    val TAG = "CamStudyService"
    val VIDEO_TRACK_ID = "ARDAMSv0"
    val VIDEO_RESOLUTION_WIDTH = 1280
    val VIDEO_RESOLUTION_HEIGHT = 720
    val FPS = 30
    
    val NOTIFICATION_ID = 1004

    var videoCapturer: VideoCapturer? = null

    var socket: Socket? = null

    var hostname = "hyunju"
    var room = "https://test/study/dsf-xjsdfdjx"

    var audioConstraints: MediaConstraints? = null
    var audioSource: AudioSource? = null
    var localAudioTrack: AudioTrack? = null

    companion object {
        val MSG_CLIENT_CONNECT = 0
        val MSG_CLIENT_DISCNNECT = 1
        val MSG_CAMSTUDY_ITEM = 2
        val MSG_TOTAL_MESSAGE = 3
        val MSG_WHISPER_MESSAGE = 4
        val MSG_HOST_VIDEO_ON_OFF = 5
        val MSG_HOST_AUDIO_ON_OFF = 6
        val MSG_SWITCH_CAMERA = 7
        val MSG_HOST_TIMER_RUN = 8
        val MSG_HOST_TIMER_PAUSE = 9
        val MSG_COMSTUDY_LEFT = 10
        val MSG_LEADER_FORCED_EXIT = 11
        val MSG_LEADER_FORCED_AUDIO_OFF = 12
        val MSG_LEADER_FORCED_VIDEO_OFF = 13
        val MSG_RECEIVED_MESSAGE = 14
        val MSG_PARTICIPANTS_ITEM = 15

        var isVideo: Boolean? = null
        var isAudio: Boolean? = null
        var camearaSwith: String? = null
        var timer: Int? = null

        var rootEglBase: EglBase = EglBase.create()
        var chatDate = ArrayList<Chat>()
        var isLeader = false

        //스터디 입장전 데이터
        private val _participantLiveData = MutableLiveData<ParticipantsResponse>()
        val participantLiveData: LiveData<ParticipantsResponse>
            get() = _participantLiveData

        var peerConnection = HashMap<String, Participant>()
        var adaperDate = ArrayList<String>()
    }

    private var mClientCallbacks = ArrayList<Messenger>()
    val mMessenger = Messenger(CallbackHandler(Looper.getMainLooper()))

    lateinit var factory: PeerConnectionFactory
    lateinit var videoTrackFromCamera: VideoTrack

    //서비스가 시작될 때 호출
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (socket == null) {
            val notification = MusicNotification.createNotification(this)
            startForeground(NOTIFICATION_ID, notification)
        }
        return START_STICKY
    }

    //포그라운드 서비스 시작시
    override fun startForegroundService(service: Intent?): ComponentName? {
//        val notification = MusicNotification.createNotification(this)
//        startForeground(NOTIFICATION_ID, notification)

        return super.startForegroundService(service)
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
    }

    //이미 onUnbind()가 호출된 후에 bindService()로 바인딩을 실행할 때 호출
    override fun onRebind(intent: Intent?) {
        super.onRebind(intent)
    }

    //서비스가 소멸될 때 호출
    override fun onDestroy() {
        super.onDestroy()
        socket!!.disconnect()
        peerConnection.clear()
        chatDate.clear()
        Log.d(TAG,"service 끝")
    }

    private fun getStudyData(camstudyData: CamStudyServiceData) {
        var enterCamstudyResponse = camstudyData.studyInfo

        var pref: PreferencesHelper = AppPreferencesHelper(this, "studyday")
        hostname = pref.getCurrentUserName()!!
        room = enterCamstudyResponse.result.studyInfo.link

        isAudio = camstudyData.isAudio
        isVideo = camstudyData.isVideo
        timer = camstudyData.timer

        camearaSwith = camstudyData.cameraSwith
    }

    private fun startCamStudy() {
        CoroutineScope(Dispatchers.IO).async {
            connectToSignallingServer()
            initializePeerConnectionFactory()
            createVideoTrackFromCameraAndShowIt()
            initializePeerConnections(hostname)
            startStreamingVideo(hostname)
            socket!!.connect()
        }
    }

    private fun getData(intent: Intent?) {
        var enterCamstudyResponse =
            intent!!.getSerializableExtra("studyInfo") as EnterCamstudyResponse

        var pref: PreferencesHelper = AppPreferencesHelper(this, "studyday")
        hostname = pref.getCurrentUserName()!!
        room = enterCamstudyResponse.result.studyInfo.link

        isAudio = intent.getBooleanExtra("audio", false)
        isVideo = intent.getBooleanExtra("video", false)
        timer = intent.getIntExtra("timer", 0)

        camearaSwith = intent.getStringExtra("cameraSwith")
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
                OkHttpClient.Builder().hostnameVerifier(myHostnameVerifier).sslSocketFactory(
                    mySSLContext!!.socketFactory
                ).build()

            // default settings for all sockets
            IO.setDefaultOkHttpWebSocketFactory(okHttpClient)
            IO.setDefaultOkHttpCallFactory(okHttpClient)

            // set as an option
            val opts = IO.Options()
            opts.callFactory = okHttpClient
            opts.webSocketFactory = okHttpClient

            // $ hostname -I
            val URL = getString(R.string.midea_url)
            Log.e(
                TAG,
                "REPLACE ME: IO Socket:$URL"
            )
            socket = IO.socket(URL, opts)
            socket!!.on(
                Socket.EVENT_CONNECT,
                Emitter.Listener { args: Array<Any> ->
                    //소켓이 연결 됨
                    Log.d(TAG, "connectToSignallingServer: connect")
                    sendMessage(getSendMessage("joinRoom"))

                }).on(Socket.EVENT_RECONNECT,
                Emitter.Listener { args: Array<Any> ->
                    //재연결 처리
                    Log.d(TAG, "reconnectToSignallingServer: reconnect")
                    sendMessage(getSendMessage("reconnectJoinRoom"))

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

                                    var participantsResponse =
                                        Gson().fromJson(
                                            message.getString("entry"),
                                            ParticipantsResponse::class.java
                                        )
                                    _participantLiveData.postValue(participantsResponse)

                                    for (par in participantsResponse.participants) {
                                        foreach(par.nickname)
                                        adaperDate.add(par.nickname)
                                        peerConnection.get(par.nickname)!!.setImgUrl(par.img, this)
                                    }

                                    Log.d(TAG,"existingParticipants adpater size"+ adaperDate.size)
                                    val handlerMessage = Message.obtain(null, MSG_CAMSTUDY_ITEM)
                                    sendHandlerMessage(handlerMessage)
                                }
                                "newParticipantArrived" -> {
                                    //새로운 참가자가 들어왔을때
                                    Log.d(TAG, "newParticipantArrived")
                                    val name = message.getString("name")
                                    foreach(name)
                                    adaperDate.add(name)

                                    Log.d(TAG,"newParticipantArrived adpater size"+ adaperDate.size)
                                    val handlerMessage = Message.obtain(null, MSG_CAMSTUDY_ITEM)
                                    sendHandlerMessage(handlerMessage)

                                    var participantsResponse =
                                        Gson().fromJson(
                                            message.getString("entry"),
                                            ParticipantsResponse::class.java
                                        )
                                    _participantLiveData.postValue(participantsResponse)

                                    for (par in participantsResponse.participants) {
                                        if(par.nickname.equals(name)) {
                                            getParticipant(message.getString("name"))
                                                .setImgUrl(par.img, this)
                                            return@Listener
                                        }
                                    }

                                    val par = getParticipant(message.getString("name"))

                                    val message_send = JSONObject()

                                    message_send.put("id", "sendStopwatchTime")
                                    message_send.put("stopwatchTime", par.timerPresentTime)
                                    message_send.put("stopwatchStatus", par.getTimerStatus())
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
                                    Log.d(TAG, "participantLeft")
                                    var index = adaperDate.indexOf(message.getString("name"))
                                    adaperDate.removeAt(index)
                                    getParticipant(message.getString("name")).stopCamStduy()
                                    peerConnection.remove(message.getString("name"))

                                    val handlerMessage = Message.obtain(null, MSG_CAMSTUDY_ITEM)
                                    sendHandlerMessage(handlerMessage)
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
                                    val receiver = if(message.getString("type").equals("total")) null else "나"

                                    chatDate.add(
                                        Chat(
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
                                    par.setStudyTime(message.getInt("stopwatchTime"))
                                    par.toggleAudio(message.getString("audioStatus"))
                                    par.toggleVideo(message.getString("videoStatus"))
                                }
                                "receiveStopwatchTime" -> {
                                    //새로운 참여자가 캠스터디에 참여하고 있는 모든 참여자들의 타이머 시간/ 스탑워치 상태를 받는다
                                    Log.d(TAG, "receiveStopwatchTime")
                                    var par = getParticipant(message.getString("sender"))
                                    par.setStudyTime(message.getInt("stopwatchTime"))
                                    par.setTimer(message.getString("stopwatchStatus"))
                                    par.toggleAudio(message.getString("audioStatus"))
                                    par.toggleVideo(message.getString("videoStatus"))
                                }
                                "receiveStopwatchStatus" -> {
                                    //다른 참여자 타이머 상태 받기
                                    Log.d(TAG, "receiveStopwatchStatus")
                                    getParticipant(message.getString("sender")).setTimer(
                                        message.getString("stopwatchStatus")
                                    )
                                }
                                "receiveForcedexit" -> {
                                    //강퇴 당하기
                                    Log.d(TAG, "receive forcedexit")
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

    private fun existingParticipants(name: String) {
        val sdpMediaConstraints = MediaConstraints()
        sdpMediaConstraints.mandatory.add(
            MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true")
        )
        sdpMediaConstraints.mandatory.add(
            MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true")
        )
        peerConnection.get(name)?.peer?.createOffer(object : SimpleSdpObserver() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            override fun onCreateSuccess(sessionDescription: SessionDescription) {
                Log.d(TAG, "onCreateSuccess: ")
                peerConnection.get(name)?.peer
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
        initializePeerConnectionFactory()
        initializePeerConnections(name)
        startStreamingVideo(name)
        existingParticipants(name)
    }

    private fun sendMessage(message: JSONObject) {
        try {
            Log.d(TAG, "sendMessage: " + message.toString())
            socket!!.emit("message", message)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    private fun initializePeerConnectionFactory() {
        PeerConnectionFactory.initializeAndroidGlobals(this, true, true, true)
        factory = PeerConnectionFactory(null)
        factory.setVideoHwAccelerationOptions(
            rootEglBase?.getEglBaseContext(),
            rootEglBase?.getEglBaseContext()
        )
    }

    private fun createVideoTrackFromCameraAndShowIt() {
        Log.d("디버그태그", "실행테스트 createVideoTrackFromCameraAndShowIt")
        audioConstraints = MediaConstraints()
        videoCapturer = createVideoCapturer()
        val videoSource: VideoSource = factory.createVideoSource(videoCapturer)
        videoCapturer!!.startCapture(
            VIDEO_RESOLUTION_WIDTH,
            VIDEO_RESOLUTION_HEIGHT,
            FPS
        )
        videoTrackFromCamera = factory.createVideoTrack(
            VIDEO_TRACK_ID,
            videoSource
        )

        //create an AudioSource instance
        audioSource = factory.createAudioSource(audioConstraints)
        localAudioTrack = factory.createAudioTrack("101", audioSource)

        var participant = getParticipant(hostname)
        participant.remoteVideoTrack = videoTrackFromCamera
        participant.remoteAudioTrack = localAudioTrack
    }

    private fun makeMe(): Participant {
        var participantMe = Participant()

        participantMe.nickname = hostname
        participantMe.isAudio = isAudio!!
        participantMe.isVideo = isVideo!!
        participantMe.setStudyTime(timer!!)

        return participantMe
    }

    private fun getParticipant(name: String): Participant {
        var participant = peerConnection.get(name)

        if (participant != null) {
            return participant
        } else if (name.equals(hostname)) {
            participant = makeMe()
            peerConnection.put(name, participant)
            return participant
        } else {
            participant = Participant()
            peerConnection.put(name, participant)
            return participant
        }
    }

    private fun initializePeerConnections(name: String) {
        var peer = createPeerConnection(factory, name)

        if (!name.equals(hostname)) {
            var part = getParticipant(name)
            part.peer = peer
            Log.d(TAG, name + "의 participant 객체 추가(peerConnection)")
        } else {
            peerConnection.get(name)?.peer = peer
            Log.d(TAG, name + "의 participant 객체 추가(peerConnection)")
        }
    }

    private fun startStreamingVideo(name: String) {
        val mediaStream: MediaStream = factory.createLocalMediaStream("ARDAMS")
        mediaStream.addTrack(videoTrackFromCamera)
        mediaStream.addTrack(localAudioTrack)
        peerConnection.get(name)?.peer?.addStream(mediaStream)
        Log.d(TAG, "startStreamingVideo()")
    }

    private fun createPeerConnection(
        factory: PeerConnectionFactory,
        name: String
    ): PeerConnection? {
        val iceServers = ArrayList<PeerConnection.IceServer>()
        val URL = "stun:stun.l.google.com:19302"
        iceServers.add(PeerConnection.IceServer(URL))
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

                if (!name.equals(hostname)) {
                    peerConnection.get(name)?.remoteVideoTrack = remoteVideoTrack
                    peerConnection.get(name)?.remoteAudioTrack = remoteAudioTrack

                    peerConnection.get(name)?.startRender()
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
        return message
    }

    private fun switchCamera() {
        if (videoCapturer != null) {
            if (videoCapturer is CameraVideoCapturer) {
                val cameraVideoCapturer = videoCapturer as CameraVideoCapturer
                cameraVideoCapturer.switchCamera(null)
            } else {
                // Will not switch camera, video capturer is not a camera
            }
        }
    }

    private fun createVideoCapturer(): VideoCapturer? {
        val videoCapturer: VideoCapturer?
        videoCapturer = if (useCamera2()) {
            createCameraCapturer(Camera2Enumerator(this))
        } else {
            createCameraCapturer(Camera1Enumerator(true))
        }
        return videoCapturer
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

    private fun useCamera2(): Boolean {
        return Camera2Enumerator.isSupported(this)
    }

    private fun sendHandlerMessage(msg: Message) {
        if (mClientCallbacks.size > 0) {
            try {
                //bind된 Activicy들에게 메시지 전송
                mClientCallbacks.forEach {
                    it.send(msg)
                }
            } catch (e: RemoteException) {
                e.printStackTrace()
            }
            Log.d(TAG, "Send MSG_ADD_VALUE message to Service")
        }
    }

    inner class CallbackHandler(looper: Looper) : Handler(looper) {
        override fun handleMessage(msg: Message) {
            Log.d(TAG,"Service에 메세지 도착")
            when (msg.what) {
                MSG_CLIENT_CONNECT -> {
                    Log.d(TAG, "Received MSG_CLIENT_CONNECT message from client");
                    mClientCallbacks.add(msg.replyTo);
                    if(socket==null){
                        startCamStudy()
                    }
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
                    peerConnection.get(hostname)!!.toggleAudio(msg.obj as String)
                    val message = JSONObject()
                    message.put("id", "sendDeviceSwitch")
                    message.put("room", room)
                    message.put("sender", hostname)
                    message.put("device", "audio")
                    message.put("status", msg.obj as String)
                    sendMessage(message)
                }
                MSG_HOST_VIDEO_ON_OFF -> {
                    //내 카메라 on/off
                    peerConnection.get(hostname)!!.toggleVideo(msg.obj as String)
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
                    peerConnection.get(hostname)!!.startHostTimer()
                    val message = JSONObject()
                    message.put("id", "sendStopwatchStatus")
                    message.put("room", room)
                    message.put("sender", hostname)
                    message.put("stopwatchStatus", "run")
                    sendMessage(message)
                }

                MSG_HOST_TIMER_PAUSE -> {
                    //내 타이머 상태를 다른 참여자들에게 보낸다 (타이머 멈춤)
                    peerConnection.get(hostname)!!.stopHostTimer()
                    val message = JSONObject()
                    message.put("id", "sendStopwatchStatus")
                    message.put("room", room)
                    message.put("sender", hostname)
                    message.put("stopwatchStatus", "pause")
                    sendMessage(message)
                }

                MSG_COMSTUDY_LEFT -> {
                    //퇴장처리
                    val handlerMessage = Message.obtain(null, MSG_COMSTUDY_LEFT)

                    var timerResult =
                        peerConnection.get(hostname)!!.timerPresentTime - peerConnection.get(
                            hostname
                        )!!.timerStratTime
                    handlerMessage.arg1 = timerResult.toInt()
                    handlerMessage.arg2 = peerConnection.get(hostname)!!.timerRestTime.toInt()

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
            }
        }
    }

}

object MusicNotification {
    const val CHANNEL_ID = "foreground_service_channel" // 임의의 채널 ID
    fun createNotification(
        context: Context
    ): Notification {
        // 알림 클릭시 MainActivity로 이동됨
        val notificationIntent = Intent(context, CamStudyActivity::class.java)
//        notificationIntent.action = Actions.MAIN
        notificationIntent.putExtra("audio", CamStudyService.isAudio)
        notificationIntent.putExtra("video", CamStudyService.isVideo)
        notificationIntent.putExtra("cameraSwith", "front")
        notificationIntent.putExtra("studyInfo", CamStudyActivity.studyInfo)
        notificationIntent.putExtra("timer", CamStudyService.timer)
        notificationIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_ACTIVITY_CLEAR_TASK

        val pendingIntent = PendingIntent
            .getActivity(context, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        // Oreo 부터는 Notification Channel을 만들어야 함
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
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setOngoing(true) // true 일경우 알림 리스트에서 클릭하거나 좌우로 드래그해도 사라지지 않음
            .setContentIntent(pendingIntent)
            .build()

        return notification
    }
}