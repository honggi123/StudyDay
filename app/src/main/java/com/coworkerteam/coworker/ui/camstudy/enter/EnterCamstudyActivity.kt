package com.coworkerteam.coworker.ui.camstudy.enter

import android.Manifest
import android.content.Intent
import android.util.Log
import android.view.View
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.coworkerteam.coworker.data.local.Service.CamStudyService

import com.coworkerteam.coworker.R
import com.coworkerteam.coworker.data.model.api.EnterCamstudyResponse
import com.coworkerteam.coworker.databinding.ActivityEnterCamstudyBinding
import com.coworkerteam.coworker.ui.base.BaseActivity
import com.coworkerteam.coworker.ui.camstudy.CamStudyCategotyAdapter
import com.coworkerteam.coworker.ui.camstudy.cam.CamStudyActivity
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.webrtc.*
import pub.devrel.easypermissions.EasyPermissions

class EnterCamstudyActivity : BaseActivity<ActivityEnterCamstudyBinding, EnterCamstudyViewModel>() {
    val TAG = "EnterCamstudyActivity"

    override val layoutResourceID: Int
        get() = R.layout.activity_enter_camstudy
    override val viewModel: EnterCamstudyViewModel by viewModel()

    lateinit var surface: SurfaceViewRenderer

    val VIDEO_TRACK_ID = "ARDAMSv0"
    val VIDEO_RESOLUTION_WIDTH = 1280
    val VIDEO_RESOLUTION_HEIGHT = 720
    val FPS = 30

    var videoCapturer: VideoCapturer? = null

    var factory: PeerConnectionFactory? = null
    var videoTrackFromCamera: VideoTrack? = null

    var studyIndex: Int? = null
    var cameraSwich = "front"

    var isCamera = true
    var isAudio = true

    var dataIntent: EnterCamstudyResponse? = null

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun initStartView() {

        dataIntent = intent.getSerializableExtra("studyInfo") as EnterCamstudyResponse?
        var data = dataIntent

        viewDataBinding.studyInfo = data
        studyIndex = data?.result?.studyInfo?.idx
        initRV(data?.result?.studyInfo!!.category)

        val perms = arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
        if (EasyPermissions.hasPermissions(this, *perms)) {
            Log.d(TAG, "권한 성공")
            surface = viewDataBinding.surfaceView
            initializeSurfaceViews()
            initializePeerConnectionFactory()
            createVideoTrackFromCameraAndShowIt()
            initView()
        } else {
            EasyPermissions.requestPermissions(this, "Need some permissions", 114, *perms)
        }

    }

    override fun initDataBinding() {
        viewModel.EnterCamstudyResponseLiveData.observe(this, Observer {
            if (it.isSuccessful) {
                var intent = Intent(this, CamStudyActivity::class.java)
                intent.putExtra("video", isCamera)
                intent.putExtra("audio", isAudio)
                intent.putExtra("cameraSwith", "front")
                intent.putExtra("studyInfo", dataIntent)
                intent.putExtra("timer", it.body()!!.result.studyTimeSec)
                startActivity(intent)
                finish()
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
            viewModel.getCamstduyJoinData(studyIndex!!)
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
        if (videoTrackFromCamera != null) {
            videoTrackFromCamera!!.removeRenderer(VideoRenderer(surface))
        }
    }

    private fun switchDevice(view: View, device: String) {
        when (device) {
            "audio" -> {
                if (isAudio) {
                    isAudio = false
                    view.isSelected = true
                } else {
                    isAudio = true
                    view.isSelected = false
                }
            }
            "camera" -> {
                if (isCamera) {
                    isCamera = false
                    videoTrackFromCamera!!.setEnabled(isCamera)
                    view.isSelected = true
                } else {
                    isCamera = true
                    videoTrackFromCamera!!.setEnabled(isCamera)
                    view.isSelected = false
                }
            }
        }
    }

    private fun initializeSurfaceViews() {
        CamStudyService.rootEglBase = EglBase.create()
        surface.init(CamStudyService.rootEglBase?.getEglBaseContext(), null)
        surface.setEnableHardwareScaler(true)
        surface.setMirror(true)
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

    private fun initializePeerConnectionFactory() {
        PeerConnectionFactory.initializeAndroidGlobals(this, true, true, true)
        factory = PeerConnectionFactory(null)
        factory!!.setVideoHwAccelerationOptions(
            CamStudyService.rootEglBase?.getEglBaseContext(),
            CamStudyService.rootEglBase?.getEglBaseContext()
        )
    }

    private fun createVideoTrackFromCameraAndShowIt() {
        Log.d("디버그태그", "실행테스트 createVideoTrackFromCameraAndShowIt")
        videoCapturer = createVideoCapturer()
        val videoSource: VideoSource = factory!!.createVideoSource(videoCapturer)
        videoCapturer!!.startCapture(
            VIDEO_RESOLUTION_WIDTH,
            VIDEO_RESOLUTION_HEIGHT,
            FPS
        )
        videoTrackFromCamera = factory!!.createVideoTrack(
            VIDEO_TRACK_ID,
            videoSource
        )

        videoTrackFromCamera!!.setEnabled(true)
        videoTrackFromCamera!!.addRenderer(VideoRenderer(surface))

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