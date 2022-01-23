package com.coworkerteam.coworker.data.model.other

import android.content.Context
import org.webrtc.*

class Participant(context: Context, name: String) {
    val TAG = "Participant"

    var idx: Int = -1

    var itemView: CamStudyItemView = CamStudyItemView(context)
    var timer: CamStudyTimer

    var peer: PeerConnection? = null

    var remoteVideoTrack: VideoTrack? = null
    var remoteAudioTrack: AudioTrack? = null

    //오디오와 비디오의 on/off 상태
    var isAudio: Boolean = true
    var isVideo: Boolean = true

    init {
        itemView.userNameView.text = name
        timer = CamStudyTimer(itemView.timerTextView, itemView.timerImageView)
    }

    fun settingDevice(isVideo: Boolean, isAudio: Boolean) {
        this.isVideo = isVideo
        this.isAudio = isAudio

        itemView.showProfileImage(isVideo.toString())
        itemView.changAudioImage(isVideo.toString())
    }

    //surfaceView에 받아온 비디오를 그리고, 받아온 오디오를 재생하는 함수
    fun startRender(remoteVideoTrack: VideoTrack?, remoteAudioTrack: AudioTrack?) {
        if (isVideo) {
            itemView.showProfileImage("off")
        } else {
            itemView.showProfileImage("on")
        }

        this.remoteVideoTrack = remoteVideoTrack
        this.remoteAudioTrack = remoteAudioTrack

        remoteAudioTrack?.setEnabled(isAudio)
        remoteVideoTrack?.setEnabled(isVideo)
        remoteVideoTrack?.addRenderer(VideoRenderer(itemView.surfaceView))
    }

    //Audio를 on/off 하는 함수
    fun toggleAudio(status: String) {
        if (status.equals("off")) {
            isAudio = false

            if (remoteAudioTrack != null) {
                remoteAudioTrack!!.setEnabled(false)
            }
        } else if (status.equals("on")) {
            isAudio = false

            if (remoteAudioTrack != null) {
                remoteAudioTrack!!.setEnabled(false)
            }
        }

        itemView.changAudioImage(status)
    }

    //Video를 on/off 하는 함수
    fun toggleVideo(status: String) {
        if (status.equals("off")) {
            isVideo = false

            if (remoteVideoTrack != null) {
                remoteVideoTrack!!.setEnabled(false)
            }
        } else if (status.equals("on")) {
            isVideo = true

            if (remoteVideoTrack != null) {
                remoteVideoTrack!!.setEnabled(true)
            }
        }

        itemView.showProfileImage(status)
    }

    //캠스터디 종료할때, 진행중이던 타이머와 비디오를 멈추기 위한 함수
    fun stopCamStduy() {
        timer.endTimer()

        if (remoteVideoTrack != null) {
            remoteVideoTrack!!.removeRenderer(VideoRenderer(itemView.surfaceView))
        }
    }
}