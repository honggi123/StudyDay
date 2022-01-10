package com.coworkerteam.coworker

import android.widget.Toast

import android.app.Service
import android.content.Intent
import android.os.*
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.coworkerteam.coworker.data.UserRepository
import com.coworkerteam.coworker.data.local.prefs.AppPreferencesHelper
import com.coworkerteam.coworker.data.local.prefs.PreferencesHelper
import com.coworkerteam.coworker.data.model.api.EnterCamstudyResponse
import com.coworkerteam.coworker.data.model.api.ParticipantsResponse
import com.coworkerteam.coworker.data.model.other.CamStudyHandler
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
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import org.koin.androidx.compose.get
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

    var videoCapturer: VideoCapturer? = null

    var socket: Socket? = null

    var member = JSONArray()
    var adaperDate = ArrayList<String>()

    var hostname = "hyunju"
    var room = "https://test/study/dsf-xjsdfdjx"

    var audioConstraints: MediaConstraints? = null
    var audioSource: AudioSource? = null
    var localAudioTrack: AudioTrack? = null

    var isVideo: Boolean? = null
    var isAudio: Boolean? = null
    var camearaSwith: String? = null
    var timer: Int? = null

    companion object {
        var rootEglBase: EglBase? = null
        var handler: Handler? = null
        var chatDate = ArrayList<Chat>()
        var isLeader = false

        //스터디 입장전 데이터
        private val _participantLiveData = MutableLiveData<ParticipantsResponse>()
        val participantLiveData: LiveData<ParticipantsResponse>
            get() = _participantLiveData
    }

    //    lateinit var binding: ActivitySamplePeerConnectionBinding
    var peerConnection = HashMap<String, Participant>()

    lateinit var factory: PeerConnectionFactory
    lateinit var videoTrackFromCamera: VideoTrack

    //서비스가 최초 생성될 때 호출
    override fun onCreate() {
        super.onCreate()

        handler = Handler(Looper.getMainLooper()) {
            when (it.what) {
                0 -> {
                    //모두에게 채팅 보내기
                    val message = JSONObject()
                    val chatting = it.obj as String
                    message.put("id", "sendMessage")
                    message.put("room", room)
                    message.put("sender", hostname)
                    message.put("msg", chatting)
                    sendMessage(message)
                }
                1 -> {
                    //귓속말 보내기
                    val message = JSONObject()
                    val chatting = it.obj as Chat
                    message.put("id", "whisperMessage")
                    message.put("room", room)
                    message.put("sender", hostname)
                    message.put("msg", chatting.msg)
                    message.put("receiver", member.get(0))
                    sendMessage(message)
                }
                2 -> {
                    //내 마이크 on/off
                    peerConnection.get(hostname)!!.toggleAudio(it.obj as String)
                    val message = JSONObject()
                    message.put("id", "sendDeviceSwitch")
                    message.put("room", room)
                    message.put("sender", hostname)
                    message.put("device", "audio")
                    message.put("status", it.obj as String)
                    sendMessage(message)
                }
                3 -> {
                    //내 카메라 on/off
                    peerConnection.get(hostname)!!.toggleVideo(it.obj as String)
                    val message = JSONObject()
                    message.put("id", "sendDeviceSwitch")
                    message.put("room", room)
                    message.put("sender", hostname)
                    message.put("device", "video")
                    message.put("status", it.obj as String)
                    sendMessage(message)
                }
                4 -> {
                    //카메라 좌우 반전
                    switchCamera()
                }
                5 -> {
                    //내 타이머 상태를 다른 참여자들에게 보낸다 (타이머 시작)
                    peerConnection.get(hostname)!!.startHostTimer()
                    val message = JSONObject()
                    message.put("id", "sendStopwatchStatus")
                    message.put("room", room)
                    message.put("sender", hostname)
                    message.put("stopwatchStatus", "run")
                    sendMessage(message)
                }

                6 -> {
                    //내 타이머 상태를 다른 참여자들에게 보낸다 (타이머 멈춤)
                    peerConnection.get(hostname)!!.stopHostTimer()
                    val message = JSONObject()
                    message.put("id", "sendStopwatchStatus")
                    message.put("room", room)
                    message.put("sender", hostname)
                    message.put("stopwatchStatus", "pause")
                    sendMessage(message)
                }

                7 -> {
                    //퇴장처리
                    var msg = Message()
                    msg.what = 2

                    var timerResult =
                        peerConnection.get(hostname)!!.timerPresentTime - peerConnection.get(
                            hostname
                        )!!.timerStratTime
                    msg.arg1 = timerResult.toInt()
                    msg.arg2 = peerConnection.get(hostname)!!.timerRestTime.toInt()

                    CamStudyActivity.handler!!.sendMessage(msg)

                    Log.d("핸들러", "핸들러 메시지 테스트 Service")
                }

                8 -> {
                    //리더가 멤버 스터디 추방
                    val message = JSONObject()
                    message.put("id", "sendForcedExit")
                    message.put("receiver", it.obj as String)
                    sendMessage(message)
                }

                9 -> {
                    //멤버 리더가 마이크 off
                    val message = JSONObject()
                    message.put("id", "sendForcedDeviceOff")
                    message.put("room", room)
                    message.put("receiver", it.obj as String)
                    message.put("device", "audio")
                    sendMessage(message)
                }

                10 -> {
                    //멤버 리더가 카메라 off
                    val message = JSONObject()
                    message.put("id", "sendForcedDeviceOff")
                    message.put("room", room)
                    message.put("receiver", it.obj)
                    message.put("device", "video")
                    sendMessage(message)
                }
            }
            true
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand")
        if (intent != null) {
            getData(intent)
            CoroutineScope(Dispatchers.IO).async {
                connectToSignallingServer()
                initializeSurfaceViews()
                initializePeerConnectionFactory()
                createVideoTrackFromCameraAndShowIt()
                initializePeerConnections(hostname)
                startStreamingVideo(hostname)
                socket!!.connect()

                chatDate.clear()
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    //bindService()로 바인딩을 실행할 때 호출
    override fun onBind(intent: Intent): IBinder {
        TODO("Return the communication channel to the service.")
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

    private fun sendHandlerMessage() {
        var message = Message()
        message.what = 0
        message.obj = CamStudyHandler(adaperDate.toMutableList(), peerConnection)
        CamStudyActivity.handler?.sendMessage(message)
    }

    private fun sendHandlerChatMessage() {
        var message = Message()
        message.what = 1
        message.obj = chatDate
        CamStudyActivity.handler?.sendMessage(message)
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
                                    handler!!.sendEmptyMessage(7)
                                }
                                "entry" -> {
                                    //참여자 정보 받는 이벤트
                                    val message = args[0] as JSONObject
                                    Log.d(TAG, "entry")

                                    var participantsResponse =
                                        Gson().fromJson(
                                            message.toString(),
                                            ParticipantsResponse::class.java
                                        )
                                    _participantLiveData.postValue(participantsResponse)

                                    for (par in participantsResponse.participants) {
                                        peerConnection.get(par.nickname)!!.setImgUrl(par.img, this)
                                    }
                                }
                                "existingParticipants" -> {
                                    //맨 처음 방에 들어갔을때
                                    Log.d(TAG, "existingParticipants")
                                    member = message.getJSONArray("data")
                                    member.put(member.length(), hostname)

                                    var i = 0
                                    while (i < member.length()) {
                                        foreach(member.getString(i))
                                        adaperDate.add(member.getString(i))
                                        i++
                                    }
                                    sendHandlerMessage()
                                }
                                "newParticipantArrived" -> {
                                    //새로운 참가자가 들어왔을때
                                    Log.d(TAG, "newParticipantArrived")
                                    member.put(member.length(), message.getString("name"))
                                    adaperDate.add(member.getString(member.length() - 1))
                                    foreach(message.getString("name"))
                                    sendHandlerMessage()

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
                                    member.remove(index)
                                    adaperDate.removeAt(index)
                                    getParticipant(message.getString("name")).stopCamStduy()
                                    peerConnection.remove(message.getString("name"))

                                    sendHandlerMessage()
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

                                    chatDate.add(
                                        Chat(
                                            message.getString("type"),
                                            message.getString("sender"),
                                            message.getString("msg"),
                                            message.getString("time")
                                        )
                                    )
                                    sendHandlerChatMessage()
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
                                    handler?.sendEmptyMessage(7)
                                }
                                "receiveForcedDeviceOff" -> {
                                    //리더가 내 미디어 장치 강제로 OFF 시킴
                                    Log.d(TAG, "receiveForcedDeviceOff")
                                    var par = getParticipant(message.getString("receiver"))

                                    if (message.getString("device").equals("video")) {
                                        par.toggleVideo(message.getString("status"))
                                        if (message.getString("receiver").equals(hostname)) {
                                            CamStudyActivity.handler?.sendEmptyMessage(4)
                                        }
                                    } else if (message.getString("device").equals("audio")) {
                                        par.toggleAudio(message.getString("status"))
                                        if (message.getString("receiver").equals(hostname)) {
                                            CamStudyActivity.handler?.sendEmptyMessage(3)
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

    private fun initializeSurfaceViews() {
        rootEglBase = EglBase.create()
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
                    //                    candidate.put("sdpMid", iceCandidate.sdpMid);
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

}