package com.coworkerteam.coworker.ui.camstudy.cam

import android.app.Activity
import android.app.ActivityManager
import android.app.AlertDialog
import android.content.*
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.AudioManager
import android.media.MediaRecorder.AudioSource.VOICE_CALL
import android.media.MicrophoneInfo
import android.media.projection.MediaProjectionManager
import android.os.*
import android.util.Base64
import android.util.Log
import android.view.*
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.*
import androidx.recyclerview.widget.RecyclerView
import com.coworkerteam.coworker.R
import com.coworkerteam.coworker.data.local.prefs.PreferencesHelper
import com.coworkerteam.coworker.data.local.service.CamStudyService
import com.coworkerteam.coworker.data.model.api.EnterCamstudyResponse
import com.coworkerteam.coworker.data.model.other.CamStudyItemView
import com.coworkerteam.coworker.data.model.other.ChatData
import com.coworkerteam.coworker.data.remote.StudydayService
import com.coworkerteam.coworker.databinding.ActivityCamStudyBinding
import com.coworkerteam.coworker.di.module.preferencesModule
import com.coworkerteam.coworker.ui.base.BaseActivity
import com.coworkerteam.coworker.ui.camstudy.info.MyStudyInfoActivity
import com.coworkerteam.coworker.ui.camstudy.info.ParticipantsActivity
import com.coworkerteam.coworker.ui.camstudy.info.StudyInfoActivity
import com.coworkerteam.coworker.ui.main.MainActivity
import com.coworkerteam.coworker.ui.main.VoiceRecorder
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayout
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import org.json.JSONObject
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.ceil


class CamStudyActivity : BaseActivity<ActivityCamStudyBinding, CamStudyViewModel>()
{
    val TAG = "CamStudyActivity"

    override val layoutResourceID: Int
        get() = R.layout.activity_cam_study
    override val viewModel: CamStudyViewModel by viewModel()

    //서비스와 통신하는 Messenger 객체
    private var mServiceCallback: Messenger? = null
    private var mClientCallback = Messenger(CallbackHandler(Looper.getMainLooper()))

    private var chat_rv: RecyclerView? = null

    private var ClickEndBackBtn = false    // 프로세스 종료로 캠스터디를 종료했을때와 종료, 뒤로가기 버튼을 클릭해서 종료했을때를 구분 false : 프로세스 종료 / true : 종료, 뒤로가기 버튼 클릭
    lateinit var mainMoveIntent : Intent

    private var goalIsSuccess = false
    private var goalSuccesstime: String? = null
    private var goalPostIsWrite = false

    companion object {
        var studyInfo: EnterCamstudyResponse? = null
        var instance: String? = null
    }

    private var speakMode : Boolean = true

    var receiver: String? = null

    var kick : Boolean = false

    var chatDialogView: View? = null

    var newpart = false

    var page = 1

    var context : Context = this

    lateinit var whoshare : String

    var menu : Menu? = null

    lateinit var mediaProjectionManager : MediaProjectionManager
    lateinit var activityResultLauncher: ActivityResultLauncher<Intent>

    lateinit var headSetReceiver : HeadSetReceiver
    var height : Int = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        //받아온값 세팅
        mediaProjectionManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult())
        {
            if (it.resultCode == RESULT_OK) {
                //SubActivity에서 갖고온 Intent(It)
                CamStudyService.screencaptureintent = it.data!!

                val msg: Message = Message.obtain(null, CamStudyService.REQUEST_MEDIA_PROJECTION)
                sendHandlerMessage(msg)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun initStartView() {
        Log.d(TAG,"initStartView")
        initData()

        var main_toolbar: androidx.appcompat.widget.Toolbar =
            findViewById(R.id.camstudy_layout_toolbar)

        setSupportActionBar(main_toolbar) // 툴바를 액티비티의 앱바로 지정
        supportActionBar?.setDisplayShowTitleEnabled(false) // 툴바에 타이틀 안보이게

        mainMoveIntent = Intent(this,MainActivity::class.java)

        var intent = Intent(this, CamStudyService::class.java)
        intent.putExtra("studyInfo", studyInfo)
        intent.putExtra("instance", instance)
        startForegroundService(intent)
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE)
        init()
    }


    override fun onConfigurationChanged(newConfig: Configuration){
        super.onConfigurationChanged(newConfig)
    }

    override fun initDataBinding() {
        viewModel.CamstduyLeaveResponseLiveData.observe(this, androidx.lifecycle.Observer {
            Log.d(TAG,"CamstduyLeaveResponseLiveData : "+ it.body())
            when {
                it.isSuccessful -> {
                    Log.d(TAG,"issucess"+it.body()?.result?.get(0)?.isSuccess)
                    if (it.body()?.result?.get(0)?.isSuccess == true){
                        mainMoveIntent.putExtra("goalIsSuccess",it.body()?.result?.get(0)?.isSuccess)
                        mainMoveIntent.putExtra("goalSuccesstime",it.body()?.result?.get(0)?.successTime)
                        mainMoveIntent.putExtra("goalPostIsWrite",it.body()!!.result.get(0)?.isWrite )
                    }
                    //퇴장처리 성공
                    Log.d(TAG,"퇴장 처리 성공")
                    unbindService(mConnection)
                    stopService(Intent(this, CamStudyService::class.java))
                }
                it.code() == 400 -> {
                    //요청값을 제대로 다 전달하지 않은 경우 ex. 날짜 또는 요청타입 값이 잘못되거나 없을때
                    val errorMessage = JSONObject(it.errorBody()?.string())
                    Log.e(TAG, errorMessage.getString("message"))

                    //400번대 에러로 퇴장처리가 실패했을 경우, 사용자에게 알려준다.
                    Toast.makeText(this, "스터디 공부시간을 저장하는데 실패하였습니다.", Toast.LENGTH_SHORT).show()
                    unbindService(mConnection)
                    stopService(Intent(this, CamStudyService::class.java))
                    finish()
                }
                it.code() == 404 -> {
                    //존재하지 않은 회원일 경우
                    val errorMessage = JSONObject(it.errorBody()?.string())
                    Log.e(TAG, errorMessage.getString("message"))

                    unbindService(mConnection)
                    stopService(Intent(this, CamStudyService::class.java))

                    moveLogin()
                }
            }
        })
    }

    override fun initAfterBinding() {
    }

    override fun onResume() {
        super.onResume()
       // headSetReceiver = HeadSetReceiver()
       // var filter = IntentFilter(Intent.ACTION_HEADSET_PLUG)
       // registerReceiver(headSetReceiver,filter)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG,"ondestroy")

        viewDataBinding.camStudyFelxboxLayout.removeAllViews()
        viewDataBinding.unbind()

        //FelxboxLayout 초기화
        /*
        //서비스랑 메신저 연결 해제
        val connectMsg = Message.obtain(null, CamStudyService.MSG_CLIENT_DISCNNECT)
        connectMsg.replyTo = mClientCallback

        try {
            mServiceCallback!!.send(connectMsg)
        } catch (e: Exception) {
            e.printStackTrace()
        }
         */
        // 프로세스 종료로 캠스터디를 종료 할 경우 서비스를 종료시켜준다.
        if(isServiceRunningCheck(CamStudyService::class.java.name)){
            Log.d(TAG,"프로세스 종료")
            unbindService(mConnection)
            stopService(Intent(this, CamStudyService::class.java))
        }
    }

    fun initData() {
        studyInfo = intent.getSerializableExtra("studyInfo") as EnterCamstudyResponse?
        instance = intent.getStringExtra("instance")
    }

    fun stopCamstudy(studyTime: Int, restTime: Int) {
        Log.d(TAG,"stopcamstudy")
        var studyIdx = studyInfo!!.result.studyInfo.idx
        var studyTimeValue = if (studyTime < 0) 0 else studyTime

        viewModel.getCamstduyLeaveData(studyIdx!!, studyTimeValue, restTime)
    }

    fun chat_recyclerview_init(data: ArrayList<ChatData>) {
        if (chat_rv != null) {
            var newAdapter = ChatAdapter(this)
            newAdapter.datas = data.toMutableList()

            chat_rv!!.adapter = newAdapter
            chat_rv!!.scrollToPosition(newAdapter.itemCount - 1)
        }
    }

    fun init() {
        //툴바
        val btn_end = findViewById<Button>(R.id.camstudy_btn_end)
        val txt_toolbarName = findViewById<TextView>(R.id.camstudy_toolbar_name)

        //하단메뉴
        val btn_mic = findViewById<ImageButton>(R.id.camstudy_btn_mic)
        val btn_camera = findViewById<ImageButton>(R.id.camstudy_btn_camera)
        val btn_play = findViewById<ImageButton>(R.id.camstudy_btn_play)
        val btn_chat = findViewById<ImageButton>(R.id.camstudy_btn_chat)
        val btn_more = findViewById<ImageButton>(R.id.camstudy_btn_more)

        txt_toolbarName.text = studyInfo?.result?.studyInfo?.name ?: ""

        btn_end.setOnClickListener(View.OnClickListener {
            //종료하기 캠스터디
            camStudyut()
        })

        btn_mic.isSelected = !CamStudyService.isAudio!!
        btn_camera.isSelected = !CamStudyService.isVideo!!
        btn_play.isSelected = CamStudyService.isPlay

        if(CamStudyService.isPermissions) {
            btn_mic.setOnClickListener(View.OnClickListener {
                val msg: Message = Message.obtain(null, CamStudyService.MSG_HOST_AUDIO_ON_OFF)

                if (CamStudyService.isAudio!!) {
                    CamStudyService.isAudio = false
                    msg.obj = "off"
                    it.isSelected = true

                    firebaseLog.addLog(TAG,"audio_off")
                } else {
                    CamStudyService.isAudio = true
                    msg.obj = "on"
                    it.isSelected = false

                    firebaseLog.addLog(TAG,"audio_on")
                }
                sendHandlerMessage(msg)
            })

            btn_camera.setOnClickListener(View.OnClickListener {
                val msg: Message = Message.obtain(null, CamStudyService.MSG_HOST_VIDEO_ON_OFF)

                if (CamStudyService.isVideo!!) {
                    CamStudyService.isVideo = false
                    msg.obj = "off"
                    it.isSelected = true

                    firebaseLog.addLog(TAG,"video_off")
                } else {
                    CamStudyService.isVideo = true
                    msg.obj = "on"
                    it.isSelected = false

                    firebaseLog.addLog(TAG,"video_on")
                }
                sendHandlerMessage(msg)
            })

        }else{
            btn_mic.setOnClickListener(View.OnClickListener {
                Toast.makeText(this,"카메라,마이크 권한이 없습니다.",Toast.LENGTH_SHORT).show()
            })

            btn_camera.setOnClickListener(View.OnClickListener {
                Toast.makeText(this,"카메라,마이크 권한이 없습니다.",Toast.LENGTH_SHORT).show()
            })
        }

        btn_play.setOnClickListener(View.OnClickListener {
            if (CamStudyService.isPlay) {
                //일시정지
                CamStudyService.isPlay = false
                it.isSelected = false

                val msg: Message = Message.obtain(null, CamStudyService.MSG_HOST_TIMER_PAUSE)
                sendHandlerMessage(msg)

                firebaseLog.addLog(TAG,"stop_timer")
            } else {
                //타이머 재생
                CamStudyService.isPlay = true
                it.isSelected = true

                val msg: Message = Message.obtain(null, CamStudyService.MSG_HOST_TIMER_RUN)
                sendHandlerMessage(msg)

                firebaseLog.addLog(TAG,"run_timer")
            }
        })

        btn_chat.setOnClickListener(View.OnClickListener {
            chatDialogView = layoutInflater.inflate(R.layout.dialog_camstudy_chat, null)
            val dialog = BottomSheetDialog(this, R.style.NewDialog)
            dialog.setContentView(chatDialogView!!)
            dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            chat_rv = chatDialogView!!.findViewById(R.id.camstudy_chat_recycler)
            chatDialogView!!.findViewById<Spinner>(R.id.camstudy_chat_spinner_sender)
            val edt_chat = chatDialogView!!.findViewById<EditText>(R.id.camstudy_chat_edt_message)
            val btn_chat = chatDialogView!!.findViewById<ImageView>(R.id.camstudy_chat_btn_send)

            chat_recyclerview_init(CamStudyService.chatDate)

            btn_chat.setOnClickListener(View.OnClickListener {
                var chat = edt_chat.text.toString()
                edt_chat.text.clear()

                var dataformat = SimpleDateFormat("a HH:mm", Locale.KOREA)
                var time = dataformat.format(System.currentTimeMillis())

                if (receiver.equals("모두에게")) {
                    val bundle = Bundle()
                    bundle.putString("msg", chat)

                    val msg: Message = Message.obtain(null, CamStudyService.MSG_TOTAL_MESSAGE)
                    msg.obj = bundle
                    sendHandlerMessage(msg)
                    CamStudyService.chatDate.add(ChatData("total", "나", null, chat, time))

                    chat_recyclerview_init(CamStudyService.chatDate)

                    firebaseLog.addLog(TAG,"send_total_chat")
                } else {
                    val bundle = Bundle()
                    bundle.putString("msg", chat)
                    bundle.putString("receiver", receiver)

                    val msg: Message = Message.obtain(null, CamStudyService.MSG_WHISPER_MESSAGE)
                    msg.obj = bundle
                    sendHandlerMessage(msg)
                    CamStudyService.chatDate.add(ChatData("total", "나", receiver, chat, time))

                    chat_recyclerview_init(CamStudyService.chatDate)

                    firebaseLog.addLog(TAG,"send_whisper_chat")
                }
            })

            val members = CamStudyService.peerConnection.keys
            spinnerInit(chatDialogView, members.toMutableList())

            dialog.setOnDismissListener(DialogInterface.OnDismissListener {
                chat_rv = null
                chatDialogView = null
            })

            dialog.show()
        })

        btn_more.setOnClickListener(View.OnClickListener {

            val dialogView: View = layoutInflater.inflate(R.layout.menu_camstudy_more, null)
            val dialog = BottomSheetDialog(this, R.style.NewDialog)
            dialog.setContentView(dialogView)

            dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            // 화면공유시 필요한 레이아웃

            val btn_screenshare =
                dialogView.findViewById<TextView>(R.id.camstudy_bottom_menu_screenshare)

            val btn_participants =
                dialogView.findViewById<TextView>(R.id.camstudy_bottom_menu_participants)
            val btn_study_info =
                dialogView.findViewById<TextView>(R.id.camstudy_bottom_menu_study_info)
            val btn_mystudy_info =
                dialogView.findViewById<TextView>(R.id.camstudy_bottom_menu_mystudy_info)
            val txt_studyUrl =
                dialogView.findViewById<TextView>(R.id.camstudy_bottom_menu_link)
            val btn_studyUrl_copy =
                dialogView.findViewById<ImageView>(R.id.camstudy_bottom_menu_link_copy)

            val newlink : String

            val array: List<String> = studyInfo!!.result.studyInfo.link.split("=")
            if(array[2].equals("null")){
                newlink = studyInfo!!.result.studyInfo.link
            }else{
                Log.d(TAG,"array[2] : " + array[2])
                var pwd = Base64.encodeToString(array[2].encodeToByteArray(),0)
                pwd = URLEncoder.encode(pwd, "UTF-8")
                newlink = array[0] + array[1] + "=" + pwd
            }

            Log.d(TAG,"newlink : " + newlink)
            // txt_studyUrl.text = studyInfo!!.result.studyInfo.link
            txt_studyUrl.text = newlink

            btn_studyUrl_copy.setOnClickListener(View.OnClickListener {
                val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("label", newlink)
                clipboard.setPrimaryClip(clip)

                Toast.makeText(this,"클립보드에 복사되었습니다.",Toast.LENGTH_SHORT).show()

                firebaseLog.addLog(TAG, "copy_link")
            })

            btn_participants.setOnClickListener(View.OnClickListener {
                val intent = Intent(this, ParticipantsActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                startActivity(intent)
                dialog.dismiss()
            })
            btn_study_info.setOnClickListener(View.OnClickListener {
                val intent = Intent(this, StudyInfoActivity::class.java)
                intent.putExtra("studyIdx", studyInfo!!.result.studyInfo.idx)
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                startActivity(intent)
                dialog.dismiss()
            })
            btn_mystudy_info.setOnClickListener(View.OnClickListener {
                val intent = Intent(this, MyStudyInfoActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                startActivity(intent)
                dialog.dismiss()
            })


            btn_screenshare.setOnClickListener(View.OnClickListener {
                Log.d(TAG,"CamStudyService.onScreen: " + CamStudyService.onScreen)
                if(CamStudyService.onScreen == true){
                    val dialogView: View = layoutInflater.inflate(R.layout.dialog_camstudy_cannotshare, null)
                    val mBuilder = androidx.appcompat.app.AlertDialog.Builder(context).setView(dialogView)
                    val builder = mBuilder.show()
                    builder.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                    val txt = dialogView.findViewById<TextView>(R.id.dialog_cannotshare_text)
                    txt.setText(whoshare+"님이 화면 공유 중입니다.")

                    val btn_ok =
                        dialogView.findViewById<Button>(R.id.dialog_camstudyshareing_btn_ok)
                    btn_ok.setOnClickListener(View.OnClickListener {
                        builder.dismiss()
                    })
                }else{
                    activityResultLauncher.launch(mediaProjectionManager.createScreenCaptureIntent())
                }
                dialog.dismiss()
            })
            dialog.show()
        })


        viewDataBinding.camstudyPre.setOnClickListener(View.OnClickListener {
            page -= 1
            setPage()
            viewDataBinding.camStudyFelxboxLayout.removeAllViewsInLayout()
            addCamStudyItemViewPaging()
        })

        viewDataBinding.camstudyNext.setOnClickListener(View.OnClickListener {
            page += 1
            viewDataBinding.camStudyFelxboxLayout.removeAllViewsInLayout()

            setPage()
            addCamStudyItemViewPaging()
        })

    }


    fun spinnerInit(view: View?, members: MutableList<String>) {
        if (view != null) {
            //스피너
            //선택목록에 나는 제외
            members.remove(viewModel.getNickName())

            if (members.isEmpty()){
                members.add("모두에게")
            } else {
                members.add(0, "모두에게")
            }

            val adapter = ArrayAdapter(this, R.layout.spinner_item_selected_gray, members)
            adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)

            val spinner_new = view.findViewById<Spinner>(R.id.camstudy_chat_spinner_sender)
            spinner_new.adapter = adapter
            spinner_new.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View,
                    position: Int,
                    id: Long
                ) {
                    if (parent.getItemAtPosition(position).toString() == "모두에게") {
                        receiver = "모두에게"
                    } else {
                        receiver = parent.getItemAtPosition(position).toString()
                    }
                }
                override fun onNothingSelected(parent: AdapterView<*>) {
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.camstudy_menu, menu)
        this.menu = menu!!
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.camera_chang -> {
                val msg: Message = Message.obtain(null, CamStudyService.MSG_SWITCH_CAMERA)
                sendHandlerMessage(msg)
            }

            /*
            R.id.sound_change -> {
                if (speakMode){
                    commuicationModeOn()
                }else{
                    speakModeOn()
                }
            }

             */

        }
        return super.onOptionsItemSelected(item)
    }

    private fun sendHandlerMessage(msg: Message){
        if (mServiceCallback != null) {
            try {
                mServiceCallback!!.send(msg)
            } catch (e: RemoteException) {
                e.printStackTrace()
            }
            Log.d(TAG, "Send message to Service")
        }
    }

    private fun getLayoutParams(size: Int): FlexboxLayout.LayoutParams {
        var height = viewDataBinding.camStudyFelxboxLayout.height

        val item_height = when{
            CamStudyService.peerConnection.keys.size < 2 -> {
                Log.d(TAG, "getLayoutParams: peerConnection.keys.size < 2")
                ViewGroup.LayoutParams.WRAP_CONTENT
            }
            CamStudyService.peerConnection.keys.size == 2 -> {
                Log.d(TAG, "getLayoutParams: peerConnection.keys.size  == 2 ")
                height/2
            }
            else -> {
                Log.d(TAG, "getLayoutParams: else ")
                height/3
            }
        }

        val lp = FlexboxLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            item_height
        )

        lp.order = 1

        if (size == 0) {
            Log.d(TAG, "getLayoutParams: size 0")
            lp.flexGrow = 1.0F
        } else if (size == 1 && CamStudyService.peerConnection.keys.size == 2) {
            Log.d(TAG, "getLayoutParams: size == 1 && CamStudyService.peerConnection.keys.size == 2")
            lp.flexGrow = 1.0F
            lp.isWrapBefore = true
        }

        lp.flexShrink = 1.0F
        lp.flexBasisPercent = 0.5F

        return lp
    }

    private fun getLayoutParamsScreen(size: Int,num : Int): FlexboxLayout.LayoutParams {

        var height = height

        val item_height = when{
            size < 2 -> {
                Log.d(TAG, "getLayoutParams: peerConnection.keys.size < 2")
                ViewGroup.LayoutParams.WRAP_CONTENT
            }
            size == 2 -> {
                Log.d(TAG, "getLayoutParams: peerConnection.keys.size  == 2 ")
                height/2
            }
            else -> {
                Log.d(TAG, "getLayoutParams: else ")
                height/3
            }
        }
        val lp = FlexboxLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            item_height
        )

        lp.order = 1

        if (num == 0) {
            Log.d(TAG, "getLayoutParams: size 0")
            lp.flexGrow = 1.0F
        } else if (num == 1 && CamStudyService.peerConnection.keys.size == 2) {
            Log.d(TAG, "getLayoutParamsScreen: size == 1")
            lp.flexGrow = 1.0F
            lp.isWrapBefore = true
        }

        lp.flexShrink = 1.0F
        lp.flexBasisPercent = 0.5F

        return lp
    }

    fun setPage(){
        val maxPage = ceil(CamStudyService.peerConnection.keys.size / 2.0).toInt()

        if(page > maxPage && maxPage > 0){
            page = maxPage
        }
        //페이지 정보 설정
        viewDataBinding.textView55.text = "$page / $maxPage"

        //이전 페이지 버튼 활성화
        if(page-1 <= 0){
            viewDataBinding.camstudyPre.visibility = View.INVISIBLE
        }else{
            viewDataBinding.camstudyPre.visibility = View.VISIBLE
        }

        //다음페이지 버튼 활성화
        if(page >= maxPage){
            viewDataBinding.camstudyNext.visibility = View.INVISIBLE
        }else{
            viewDataBinding.camstudyNext.visibility = View.VISIBLE
        }

    }

    fun addCamStudyItemView() {
        val startIndex:Int
        var endIndex:Int
        if(CamStudyService.onScreen == true){
            Log.d(TAG,"onScreen!!")
            startIndex = 2 * ( page-1 )
            endIndex = startIndex + 1
        }else{
            startIndex = 6 * ( page-1 )
            endIndex = startIndex + 5
        }

        val maxIndex = CamStudyService.peerConnection.keys.size -1

        if(endIndex > maxIndex){
            endIndex = maxIndex
        }
        Log.d(TAG, "addCamStudyItemView: $startIndex, $endIndex, $maxIndex")

        if(maxIndex >= 0) {
            for (i in startIndex..endIndex) {
                Log.d(TAG, "addCamStudyItemView: $i")
                val key = CamStudyService.peerConnection.keys.toList()[i]
                val item = CamStudyService.peerConnection[key]?.itemView

                item?.layoutParams =
                    getLayoutParams(viewDataBinding.camStudyFelxboxLayout.flexItemCount)

                viewDataBinding.camStudyFelxboxLayout.addView(
                    CamStudyService.peerConnection[key]?.itemView
                )
            }
        }
    }

    fun addCamStudyItemViewPaging() {
        val startIndex = 2 * ( page-1 )
        var endIndex = startIndex + 1
        val maxIndex = CamStudyService.peerConnection.keys.size -1

        if(endIndex > maxIndex){
            endIndex = maxIndex
        }

        Log.d(TAG, "addCamStudyItemView: $startIndex, $endIndex, $maxIndex")

        if(maxIndex >= 0) {
            for (i in startIndex..endIndex) {
                Log.d(TAG, "addCamStudyItemView: $i")
                val key = CamStudyService.peerConnection.keys.toList()[i]
                val item = CamStudyService.peerConnection[key]?.itemView

                item?.layoutParams =
                    getLayoutParams(viewDataBinding.camStudyFelxboxLayout.flexItemCount)

                viewDataBinding.camStudyFelxboxLayout.addView(
                    CamStudyService.peerConnection[key]?.itemView
                )
            }
        }
    }


    private var mConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            Log.e(TAG,"ServiceConnection")
            mServiceCallback = Messenger(service)
            //서비스랑 연결
            val connectMsg = Message.obtain(null, CamStudyService.MSG_CLIENT_CONNECT)
            connectMsg.replyTo = mClientCallback

            try {
                mServiceCallback!!.send(connectMsg)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            mServiceCallback = null
        }
    }


    override fun onBackPressed() {
        camStudyut()
    }

    fun camStudyut(){
        // 캠스터디 종료 확인 다이얼로그
        val mDialogView =
            LayoutInflater.from(context).inflate(R.layout.dialog_camstudyout, null)
        val mBuilder = androidx.appcompat.app.AlertDialog.Builder(context).setView(mDialogView)
        val builder = mBuilder.show()

        builder.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val btn_cancle =
            mDialogView.findViewById<Button>(R.id.dialog_camstudyout_btn_cancle)
        val btn_out =
            mDialogView.findViewById<Button>(R.id.dialog_camstudyout_btn_ok)

        btn_cancle.setOnClickListener(View.OnClickListener {
            builder.dismiss()
        })
        // 캠스터디 퇴장
        btn_out.setOnClickListener(View.OnClickListener {
            ClickEndBackBtn = true
            val msg: Message = Message.obtain(null, CamStudyService.MSG_COMSTUDY_LEFT)
            sendHandlerMessage(msg)
            builder.dismiss()
        })
    }


    inner class CallbackHandler(looper: Looper) : Handler(looper) {
        override fun handleMessage(msg: Message) {
            Log.d(TAG, "Activity에 메세지 도착 : $msg")
            when (msg.what) {
                CamStudyService.MSG_SERVICE_CONNECT -> {
                    //연결 성공했을 경우
                    viewDataBinding.camStudyFelxboxLayout.removeAllViews()
                    //레이아웃 설정
                    setPage()
                    addCamStudyItemView()
                }
                CamStudyService.MSG_EXISTINGPARTICIPANNTS -> {
                    //내가 방에 맨 먼저 참여했을 경우
                    viewDataBinding.camStudyFelxboxLayout.removeAllViews()

                    //레이아웃 설정
                    setPage()
                    addCamStudyItemView()
                }
                CamStudyService.MSG_NEWPARTICIPANTARRIVED -> {
                    //새로운 참여자가 들어왔을때

                    //레이아웃 초기화
                    viewDataBinding.camStudyFelxboxLayout.removeAllViews()
                    //레이아웃 다시 설정
                    setPage()
                    addCamStudyItemView()

                }
                CamStudyService.MSG_PARTICIPANTLEFT -> {
                    //누군가 스터디를 떠났을 경우
                    Log.d(TAG,"onScreen : "+ CamStudyService.onScreen)

                    viewDataBinding.camStudyFelxboxLayout.removeAllViews()
                    Log.d(TAG,"MSG_PARTICIPANTLEFT")
                    //레이아웃 다시 설정
                    setPage()
                    addCamStudyItemView()

                }
                CamStudyService.MSG_RECEIVED_MESSAGE -> {
                    //채팅 아이템
                    chat_recyclerview_init(CamStudyService.chatDate)
                }
                CamStudyService.MSG_COMSTUDY_LEFT -> {
                    //종료시 타이머 가져오기
                    stopCamstudy(msg.arg1, msg.arg2)
                }
                CamStudyService.MSG_LEADER_FORCED_EXIT -> {
                    //리더에게 추방
                    val msg: Message =
                        Message.obtain(null, CamStudyService.MSG_COMSTUDY_LEFT)
                    sendHandlerMessage(msg)
                    kick = true
                }
                CamStudyService.MSG_LEADER_FORCED_AUDIO_OFF -> {
                    //리더에게 마이크 off
                    val btn_mic = findViewById<ImageButton>(R.id.camstudy_btn_mic)

                    CamStudyService.isAudio = false
                    btn_mic.isSelected = true
                }
                CamStudyService.MSG_LEADER_FORCED_VIDEO_OFF -> {
                    //리더에게 카메라 off
                    val btn_camera = findViewById<ImageButton>(R.id.camstudy_btn_camera)
                    CamStudyService.isVideo = false
                    btn_camera.isSelected = true
                }
                CamStudyService.MSG_SERVICE_FINISH -> {
                    if(CamStudyService.forcedexit){
                        mainMoveIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                        mainMoveIntent.putExtra("KickFromLeader",CamStudyService.forcedexit)
                        Log.d(TAG,"강퇴 여부 : "+CamStudyService.forcedexit)
                        CamStudyService.forcedexit = false
                        startActivity(mainMoveIntent)
                    }
                    if(!isFinishing) finish()
                }
                CamStudyService.REQUEST_SCREEN_SHARE -> {
                    height = viewDataBinding.camStudyFelxboxLayout.height
                    var bundle = Bundle()
                    bundle = msg.data
                    var sharehostname = bundle.getString("name")
                    var shareHostorNot = bundle.getBoolean("shareHost")

                    if (sharehostname != null){
                        whoshare = sharehostname
                    }

                    viewDataBinding.camStudyFelxboxLayout.removeAllViews()
                    viewDataBinding.camStudyShareLayout.visibility = View.VISIBLE
                    viewDataBinding.screensharePaging.visibility = View.VISIBLE

                    if (shareHostorNot){
                        viewDataBinding.camStudyShareMyview.visibility = View.VISIBLE
                    }else{
                        viewDataBinding.camStudyShareMyview.visibility = View.GONE
                        viewDataBinding.camStudyShareFelxboxOther.visibility = View.VISIBLE
                        val item = CamStudyService.peerConnection[sharehostname]!!.itemViewScreen

                        item?.layoutParams =
                            getLayoutParams(0)

                        viewDataBinding.camStudyShareFelxboxOther.addView(
                            item
                        )

                        if (sharehostname != null) {
                            item.screenShareMode(sharehostname)
                        }
                        item.showProfileImage("on")
                    }
                    setPage()

                    viewDataBinding.camStudyFelxboxLayout.removeAllViewsInLayout()
                    viewDataBinding.camStudyFelxboxLayout.flexWrap = FlexWrap.NOWRAP
                    addCamStudyItemViewPaging()

                    viewDataBinding.btnCamstudyStopscreen.setOnClickListener(View.OnClickListener {
                        //레이아웃 초기화
                        val msg: Message = Message.obtain(null, CamStudyService.REQUEST_STOP_SHARE)
                        sendHandlerMessage(msg)
                        firebaseLog.addLog(TAG,"stop_share")
                    })
                    firebaseLog.addLog(TAG,"start_share")
                }
                CamStudyService.RECEIVE_STOP_SHARE -> {
                    // 화면 공유 중지를 받음
                    //레이아웃 초기화
                    viewDataBinding.camStudyShareFelxboxOther.visibility = View.GONE
                    viewDataBinding.camStudyShareLayout.visibility = View.GONE
                    viewDataBinding.screensharePaging.visibility = View.INVISIBLE

                    viewDataBinding.camStudyFelxboxLayout.flexWrap = FlexWrap.WRAP

                    viewDataBinding.camStudyShareFelxboxOther.removeAllViews()
                    viewDataBinding.camStudyFelxboxLayout.removeAllViewsInLayout()

                    Log.d(TAG,"RECEIVE_STOP_SHARE")
                    for (i in 1..CamStudyService.peerConnection.size) {
                        Log.d(TAG, "addCamStudyItemView: $i")
                        val key = CamStudyService.peerConnection.keys.toList()[i-1]
                        val item = CamStudyService.peerConnection[key]?.itemView

                        item?.layoutParams = getLayoutParamsScreen(CamStudyService.peerConnection.size,
                            viewDataBinding.camStudyFelxboxLayout.flexItemCount)

                        viewDataBinding.camStudyFelxboxLayout.addView(
                            item
                        )
                    }
                    CamStudyService.onScreen = false
                    firebaseLog.addLog(TAG,"receive_stop_share")
                }
            }
        }
    }

    class HeadSetReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            var camstudyActivity  = context as CamStudyActivity
            if(intent?.action.equals(Intent.ACTION_HEADSET_PLUG)){
                var state = intent?.getIntExtra("state",-1)
                if(state == 0){ // 헤드셋 해제
                    Log.d("CAMACT","헤드셋 해제")
                    camstudyActivity.speakModeOn()
                }else{
                    Log.d("CAMACT","헤드셋 장착")
                    camstudyActivity.commuicationModeOn()
                }
            }
        }
    }

    fun speakModeOn(){
        var audioManager : AudioManager
        audioManager = context!!.getSystemService(AUDIO_SERVICE) as AudioManager

        audioManager.setSpeakerphoneOn(true)
        speakMode = true

        menu?.getItem(1)?.setIcon(R.drawable.ic_baseline_volume_up_24)
    }

    fun commuicationModeOn(){
        var audioManager : AudioManager
        audioManager = context?.getSystemService(AUDIO_SERVICE) as AudioManager



        audioManager.setSpeakerphoneOn(false)
        speakMode = false

        menu?.getItem(1)?.setIcon(R.drawable.ic_baseline_phone_in_talk_24)
    }

    @SuppressWarnings("deprecation")
    fun isServiceRunningCheck(servicename:String) : Boolean{
    var manager : ActivityManager =  this.getSystemService(Activity.ACTIVITY_SERVICE) as ActivityManager
    for (service : ActivityManager.RunningServiceInfo in manager.getRunningServices(Integer.MAX_VALUE)) {
        Log.d(TAG,"service.service.getClassName() ; " + service.service.getClassName())
        if (servicename.equals(service.service.getClassName())) {
            return true
        }
    }
    return false
    }




}