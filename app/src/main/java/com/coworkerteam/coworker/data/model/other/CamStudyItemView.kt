package com.coworkerteam.coworker.data.model.other

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.bumptech.glide.Glide
import com.coworkerteam.coworker.R
import com.coworkerteam.coworker.data.local.service.CamStudyService
import com.github.ybq.android.spinkit.SpinKitView
import com.wang.avi.AVLoadingIndicatorView
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import org.webrtc.EglRenderer
import org.webrtc.SurfaceViewRenderer

class CamStudyItemView : ConstraintLayout {
    lateinit var view: View
    lateinit var surfaceView: SurfaceViewRenderer
    lateinit var profileView: CircleImageView
    lateinit var timerImageView: ImageView
    lateinit var timerTextView: TextView
    lateinit var audioView: ImageView
    lateinit var userNameView: TextView
    lateinit var highlightView: View
    lateinit var speakAnimation : AVLoadingIndicatorView
    //생성자
    constructor(context: Context) : super(context) {
        init()
    }

    fun init() {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        view = inflater.inflate(R.layout.item_camstudy, this, true)

        //camstudy item에 대한 요소 초기화
        speakAnimation = view.findViewById(R.id.speak_animation)
        surfaceView = view.findViewById(R.id.cam_surface_view)
        profileView = view.findViewById(R.id.item_camstudy_profile)
        timerImageView = view.findViewById(R.id.item_camstudy_img_timer)
        timerTextView = view.findViewById(R.id.item_camstudy_txt_time)
        audioView = view.findViewById(R.id.item_camstudy_mic)
        userNameView = view.findViewById(R.id.item_camstudy_txt_name)
        highlightView = view.findViewById(R.id.cam_highlight)
        //surfaceView에 관련한 설정
        CoroutineScope(Dispatchers.Main).async {
            surfaceView.init(CamStudyService.rootEglBase?.eglBaseContext, null)

            surfaceView.setEnableHardwareScaler(true)
            surfaceView.setMirror(true)

        }
    }

    //프로필 이미지 세팅 함수
    fun setProfileImage(imageUrl: String) {
        CoroutineScope(Dispatchers.Main).async {
            Glide.with(context).load(imageUrl).into(profileView)
        }
    }

    //비디오 표시 여부에 따라 비디오가 off면 프로필을 보이고, on이면 프로필을 숨기는 함수
    //비디오 표시 여부에 따라 비디오가 off면 프로필을 보이고, on이면 프로필을 숨기는 함수
    fun showProfileImage(status: String) {
        if (status == "off") {
            //비디오가 off일 경우, 프로필 사진을 보여준다.
            CoroutineScope(Dispatchers.Main).async {
                profileView.visibility = View.VISIBLE
            }
        } else if (status == "on") {
            //비디오가 on일 경우, 프로필 사진을 숨긴다.
            CoroutineScope(Dispatchers.Main).async {
                profileView.visibility = View.GONE
            }
        }
    }

    //비디오 표시 여부에 따라 비디오가 off면 프로필을 보이고, on이면 프로필을 숨기는 함수
    fun showProfileImage(status: Boolean) {
        if (!status) {
            //비디오가 off일 경우, 프로필 사진을 보여준다.
            CoroutineScope(Dispatchers.Main).async {
                profileView.visibility = View.VISIBLE
            }
        } else if (status) {
            //비디오가 on일 경우, 프로필 사진을 숨긴다.
            CoroutineScope(Dispatchers.Main).async {
                profileView.visibility = View.GONE
            }
        }
    }

    //오디오 이미지 변경 함수
    fun changAudioImage(status: String) {
        if (status == "off") {
            //오디오 off 아이콘으로 변경
            audioView.isSelected = true
        } else if (status == "on") {
            //오디오 on 아이콘으로 변경
            audioView.isSelected = false
        }
    }

    //오디오 이미지 변경 함수
    fun changAudioImage(status: Boolean) {
        if (!status) {
            //오디오 off 아이콘으로 변경
            audioView.isSelected = true
        } else if (status) {
            //오디오 on 아이콘으로 변경
            audioView.isSelected = false
        }
    }

    // 하이라이트 온/오프
    fun changeHighlight(status: Boolean){
        if(status){
            audioView.visibility = GONE
            highlightView.visibility = View.VISIBLE
            speakAnimation.visibility = View.VISIBLE
        }else{
            audioView.visibility = VISIBLE
            highlightView.visibility = View.GONE
            speakAnimation.visibility = View.GONE
        }
    }


}