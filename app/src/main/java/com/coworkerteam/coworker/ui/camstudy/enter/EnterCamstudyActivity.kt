package com.coworkerteam.coworker.ui.camstudy.enter

import android.Manifest
import android.content.Intent
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.coworkerteam.coworker.data.local.service.CamStudyService

import com.coworkerteam.coworker.R
import com.coworkerteam.coworker.data.model.api.EnterCamstudyResponse
import com.coworkerteam.coworker.data.model.other.SingleObject.SinglePeerConnectionFactory
import com.coworkerteam.coworker.databinding.ActivityEnterCamstudyBinding
import com.coworkerteam.coworker.ui.base.BaseActivity
import com.coworkerteam.coworker.ui.camstudy.CamStudyCategotyAdapter
import com.coworkerteam.coworker.ui.camstudy.cam.CamStudyActivity
import com.coworkerteam.coworker.ui.camstudy.info.ParticipantsActivity
import com.coworkerteam.coworker.ui.main.MainActivity
import com.google.firebase.analytics.FirebaseAnalytics
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.normal.TedPermission
import org.json.JSONObject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.webrtc.*

class EnterCamstudyActivity : BaseActivity<ActivityEnterCamstudyBinding, EnterCamstudyViewModel>() {

    val TAG = "EnterCamstudyActivity"

    override val layoutResourceID: Int
        get() = R.layout.activity_enter_camstudy
    override val viewModel: EnterCamstudyViewModel by viewModel()

    lateinit var surface: SurfaceViewRenderer

    private val VIDEO_TRACK_ID = "ARDAMSv0"
    private val VIDEO_RESOLUTION_WIDTH = 1280
    private val VIDEO_RESOLUTION_HEIGHT = 720
    private val FPS = 30

    lateinit var renderer : VideoRenderer

    var videoCapturer: VideoCapturer? = null

    var factory: PeerConnectionFactory? = null
    var videoTrackFromCamera: VideoTrack? = null

    var videoSource : VideoSource? = null

    var studyIndex: Int? = null

    var enterCamstudy : Boolean = false

    var isVideo: Boolean? = true
    var isAudio: Boolean? = true

    var instanceID: String? = null

    var dataIntent: EnterCamstudyResponse? = null

    override fun initStartView() {
        dataIntent = intent.getSerializableExtra("studyInfo") as EnterCamstudyResponse?
        var data = dataIntent

        Glide.with(this).load(data?.result?.userImg).into(viewDataBinding.enterCamstudyProfile)

        viewDataBinding.studyInfo = data
        studyIndex = data?.result?.studyInfo?.idx
        initRV(data?.result?.studyInfo!!.category)

        var toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolber_enter_camstudy)

        setSupportActionBar(toolbar) // 툴바를 액티비티의 앱바로 지정
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // 드로어를 꺼낼 홈 버튼 활성화
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_ios_new_24_black) // 홈버튼 이미지 변경
        supportActionBar?.title = data.result.studyInfo.name

        val permissionListener: PermissionListener = object: PermissionListener {
            override fun onPermissionGranted() {
                //권한 허가시 실행할 내용
                CamStudyService.isPermissions = true

                surface = viewDataBinding.surfaceView
                initializeSurfaceViews()
                initializePeerConnectionFactory()
                createVideoTrackFromCameraAndShowIt()
                initView()
            }

            override fun onPermissionDenied(deniedPermissions: MutableList<String>?) {
                // 권한 거부시 실행  할 내용
                Toast.makeText(this@EnterCamstudyActivity,"권한을 허용하지 않으면 캠스터디에서 카메라와 마이크를 사용할 수 없습니다.",Toast.LENGTH_SHORT).show()

                CamStudyService.isPermissions = false
                isAudio = false
                isVideo = false
                viewDataBinding.imageButton2.isSelected = true
                viewDataBinding.imageButton3.isSelected = true

                viewDataBinding.enterCamstudyBtnEnter.setOnClickListener(View.OnClickListener {
                    viewModel.getCamstduyInstanceData(dataIntent!!.result.studyInfo.link)
                })
            }
        }

        TedPermission.create()
            .setPermissionListener(permissionListener)
            .setDeniedMessage("앱의 카메라, 마이크 권한을 허용해야 정상적으로 캠스터디를 이용할 수 있습니다. 해당 권한을 [설정] > [권한] 에서 허용해주세요.")
            .setPermissions(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
            .check()

        checkdate()
    }

    override fun initDataBinding() {
        viewModel.EnterCamstudyResponseLiveData.observe(this, Observer {
            when {
                it.isSuccessful -> {
                    firebaseLog.addLog(TAG,FirebaseAnalytics.Event.JOIN_GROUP)

                    var intent = Intent(this, CamStudyActivity::class.java)
                    intent.putExtra("instance",instanceID)
                    intent.putExtra("studyInfo", dataIntent)
                    CamStudyService.isVideo = isVideo
                    CamStudyService.isAudio = isAudio
                    CamStudyService.timer = it.body()!!.result.studyTimeSec
                    enterCamstudy = true
                    startActivity(intent)
                    finish()
                }
                it.code() == 400 -> {
                    //요청값을 제대로 다 전달하지 않은 경우 ex. 날짜 또는 요청타입 값이 잘못되거나 없을때
                    val errorMessage = JSONObject(it.errorBody()?.string())
                    Log.e(TAG, errorMessage.getString("message"))

                    //400번대 에러로 스터디 입장페이지 진입 실패했을 경우, 사용자에게 알려준다.
                    Toast.makeText(this, "스터디에 입장할 수 없습니다. 나중 다시 시도해주세요.", Toast.LENGTH_SHORT)
                        .show()
                }
                it.code() == 403 -> {
                    val errorMessage = JSONObject(it.errorBody()?.string())
                    Log.e(TAG, errorMessage.getString("message"))

                    when (errorMessage.getInt("code")) {
                        -3 -> {
                            //참여 가능 인원을 초과하여 입장할 수 없는 경우
                            Toast.makeText(this, "현재 참여가능한 인원이 가득 찼습니다.", Toast.LENGTH_SHORT).show()
                        }
                        -4 -> {
                            //해당 스터디에 강제 탈퇴 당해 더 이상 입장할 수 없는 경우
                            Toast.makeText(this, "강제 퇴장당한 스터디입니다. 입장할 수 없습니다.", Toast.LENGTH_SHORT)
                                .show()
                        }
                        -5 -> {
                            //참여중인 스터디가 있을 경우
                            Toast.makeText(
                                this,
                                "이미 공부중인 스터디가 있습니다. 바로 참여할 수 없습니다.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
                it.code() == 404 -> {
                    val errorMessage = JSONObject(it.errorBody()?.string())
                    Log.e(TAG, errorMessage.getString("message"))

                    when (errorMessage.getInt("code")) {
                        -2 -> {
                            //존재하지 않는 회원인 경우
                            moveLogin()
                        }
                        -3 -> {
                            //존재하지 않는 스터디일 경우
                            Toast.makeText(this, "더이상 존재하지 않는 스터디입니다.", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        })

        viewModel.CamstduyInstanceResponseLiveData.observe(this, Observer {
            when {
                it.isSuccessful -> {
                    instanceID = it.body()!!.instanceId
                    viewModel.getCamstduyJoinData(studyIndex!!)
                }
                it.code() == 400 -> {
                    //요청값을 제대로 다 전달하지 않은 경우 ex. 날짜 또는 요청타입 값이 잘못되거나 없을때
                    val errorMessage = JSONObject(it.errorBody()?.string())
                    Log.e(TAG, errorMessage.getString("message"))

                    //400번대 에러로 스터디 입장페이지 진입 실패했을 경우, 사용자에게 알려준다.
                    Toast.makeText(this, "스터디에 입장할 수 없습니다. 나중 다시 시도해주세요.", Toast.LENGTH_SHORT)
                        .show()
                }
                it.code() == 403 -> {
                    val errorMessage = JSONObject(it.errorBody()?.string())
                    Log.e(TAG, errorMessage.getString("message"))

                    when (errorMessage.getInt("code")) {
                        -3 -> {
                            //참여 가능 인원을 초과하여 입장할 수 없는 경우
                            Toast.makeText(this, "현재 참여가능한 인원이 가득 찼습니다.", Toast.LENGTH_SHORT).show()
                        }
                        -4 -> {
                            //해당 스터디에 강제 탈퇴 당해 더 이상 입장할 수 없는 경우
                            Toast.makeText(this, "강제 퇴장당한 스터디입니다. 입장할 수 없습니다.", Toast.LENGTH_SHORT)
                                .show()
                        }
                        -5 -> {
                            //참여중인 스터디가 있을 경우
                            Toast.makeText(
                                this,
                                "이미 공부중인 스터디가 있습니다. 바로 참여할 수 없습니다.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
                it.code() == 404 -> {
                    val errorMessage = JSONObject(it.errorBody()?.string())
                    Log.e(TAG, errorMessage.getString("message"))

                    when (errorMessage.getInt("code")) {
                        -2 -> {
                            //존재하지 않는 회원인 경우
                            moveLogin()
                        }
                        -3 -> {
                            //존재하지 않는 스터디일 경우
                            Toast.makeText(this, "더이상 존재하지 않는 스터디입니다.", Toast.LENGTH_SHORT).show()
                        }
                        -8 -> {
                            //인스턴스 아이디가 존재하지 않는 경우
                            viewModel.getCamstduyJoinData(studyIndex!!)
                        }
                    }
                }
            }
        })
    }

    override fun initAfterBinding() {
    }

    private fun initView() {
        viewDataBinding.imageButton2.setOnClickListener(View.OnClickListener {
            switchDevice(it, "audio")
        })
        viewDataBinding.imageButton3.setOnClickListener(View.OnClickListener {
            switchDevice(it, "camera")
        })
        viewDataBinding.enterCamstudyBtnEnter.setOnClickListener(View.OnClickListener {
            viewModel.getCamstduyInstanceData(dataIntent!!.result.studyInfo.link)
        })
    }

    private fun initRV(category: String) {
        val rv = findViewById<RecyclerView>(R.id.enter_camstudy_rv_category)
        val categorys = category.split("|")

        var myStudyAdepter: CamStudyCategotyAdapter = CamStudyCategotyAdapter(this)
        myStudyAdepter.datas = categorys.toMutableList()
        rv.adapter = myStudyAdepter
    }



    override fun onDestroy() {
        super.onDestroy()

        viewDataBinding.unbind()

        if (videoTrackFromCamera != null) {
            videoTrackFromCamera!!.setEnabled(false)
            videoTrackFromCamera!!.removeRenderer(renderer)
            videoTrackFromCamera!!.dispose()
        }

        if(videoCapturer != null){
            videoCapturer!!.dispose()
            videoCapturer = null
        }
        if(videoSource != null){
            videoSource!!.dispose()
            videoSource = null
        }



        surface.release()
    }

    private fun switchDevice(view: View, device: String) {
        when (device) {
            "audio" -> {
                if (isAudio!!) {
                    isAudio = false
                    view.isSelected = true
                } else {
                    isAudio = true
                    view.isSelected = false
                }
            }
            "camera" -> {
                if (isVideo!!) {
                    //카메라가 켜진 상태였으면 끈 상태로 전환
                    isVideo = false
                    videoTrackFromCamera!!.setEnabled(isVideo!!)
                    view.isSelected = true
                    viewDataBinding.enterCamstudyProfile.visibility = View.VISIBLE
                } else {
                    //카메라가 끈 상태였으면 켜진 상태로 전환
                    isVideo = true
                    videoTrackFromCamera!!.setEnabled(isVideo!!)
                    view.isSelected = false
                    viewDataBinding.enterCamstudyProfile.visibility = View.GONE
                }
            }
        }
    }

    private fun initializeSurfaceViews() {
        CamStudyService.rootEglBase = EglBase.create()
        surface.init(CamStudyService.rootEglBase.eglBaseContext, null)
        surface.setEnableHardwareScaler(true)
        surface.setMirror(true)
    }

    private fun initializePeerConnectionFactory() {
        PeerConnectionFactory.initializeAndroidGlobals(this, true, true, true)
        factory = SinglePeerConnectionFactory.getfactory()
        //factory = PeerConnectionFactory(null)
        factory!!.setVideoHwAccelerationOptions(
            CamStudyService.rootEglBase.eglBaseContext,
            CamStudyService.rootEglBase.eglBaseContext
        )
    }

    private fun createVideoTrackFromCameraAndShowIt() {
        Log.d(TAG, "실행테스트 createVideoTrackFromCameraAndShowIt")
        videoCapturer = createVideoCapturer()
        videoSource = factory!!.createVideoSource(videoCapturer)
        videoCapturer!!.startCapture(
            VIDEO_RESOLUTION_WIDTH,
            VIDEO_RESOLUTION_HEIGHT,
            FPS
        )

        videoTrackFromCamera = factory!!.createVideoTrack(
            VIDEO_TRACK_ID,
            videoSource
        )

        viewDataBinding.enterCamstudyProfile.visibility = View.GONE
        videoTrackFromCamera!!.setEnabled(true)
        renderer = VideoRenderer(surface)
        videoTrackFromCamera!!.addRenderer(renderer)
    }




    private fun createVideoCapturer(): VideoCapturer? {
        videoCapturer =
            createCameraCapturer(Camera1Enumerator(true))
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