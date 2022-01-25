package com.coworkerteam.coworker.ui.camstudy.cam

import android.content.*
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.*
import android.util.Log
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.coworkerteam.coworker.data.local.service.CamStudyService
import com.coworkerteam.coworker.R
import com.coworkerteam.coworker.data.model.api.EnterCamstudyResponse
import com.coworkerteam.coworker.data.model.other.ChatData
import com.coworkerteam.coworker.databinding.ActivityCamStudyBinding
import com.coworkerteam.coworker.ui.base.BaseActivity
import com.coworkerteam.coworker.ui.camstudy.info.MyStudyInfoActivity
import com.coworkerteam.coworker.ui.camstudy.info.ParticipantsActivity
import com.coworkerteam.coworker.ui.camstudy.info.StudyInfoActivity
import com.google.android.material.bottomsheet.BottomSheetDialog
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import android.content.ClipData
import android.view.*
import androidx.annotation.RequiresApi
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxItemDecoration
import com.google.android.flexbox.FlexboxLayout
import com.google.android.material.dialog.MaterialAlertDialogBuilder


class CamStudyActivity : BaseActivity<ActivityCamStudyBinding, CamStudyViewModel>() {
    val TAG = "CamStudyActivity"

    override val layoutResourceID: Int
        get() = R.layout.activity_cam_study
    override val viewModel: CamStudyViewModel by viewModel()

    //서비스와 통신하는 Messenger 객체
    private var mServiceCallback: Messenger? = null
    private var mClientCallback = Messenger(CallbackHandler(Looper.getMainLooper()))

    var chat_rv: RecyclerView? = null

    var isMic = true
    var isVideo = true
    var isPlay = false

    var timer: Int? = null

    companion object {
        var studyInfo: EnterCamstudyResponse? = null
    }

    var receiver: String? = null

    var chatDialogView: View? = null

    @RequiresApi(Build.VERSION_CODES.O)
    override fun initStartView() {
        initData()

        var main_toolbar: androidx.appcompat.widget.Toolbar =
            findViewById(R.id.camstudy_layout_toolbar)

        setSupportActionBar(main_toolbar) // 툴바를 액티비티의 앱바로 지정
        supportActionBar?.setDisplayShowTitleEnabled(false) // 툴바에 타이틀 안보이게

        init()

        var intent = Intent(this, CamStudyService::class.java)
        intent.putExtra("audio", isMic)
        intent.putExtra("video", isVideo)
        intent.putExtra("studyInfo", studyInfo)
        intent.putExtra("timer", timer)
        startForegroundService(intent)
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE)

    }

    override fun initDataBinding() {

    }

    override fun initAfterBinding() {

    }

    fun initData() {
        isMic = intent.getBooleanExtra("audio", false)
        isVideo = intent.getBooleanExtra("video", false)
        studyInfo = intent.getSerializableExtra("studyInfo") as EnterCamstudyResponse?
        timer = intent.getIntExtra("timer", 0)
        isPlay = intent.getBooleanExtra("paly", false)
    }

    fun stopCamstudy(studyTime: Int, restTime: Int) {
        var studyIdx = studyInfo!!.result.studyInfo.idx
        var studyTimeValue = if (studyTime < 0) 0 else studyTime

        viewModel.getCamstduyLeaveData(studyIdx!!, studyTimeValue, restTime)
        unbindService(mConnection)
        stopService(Intent(this, CamStudyService::class.java))
        finish()
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

        btn_end.setOnClickListener(View.OnClickListener {
            //종료하기 캠스터디
            val msg: Message = Message.obtain(null, CamStudyService.MSG_COMSTUDY_LEFT)
            sendHandlerMessage(msg)
        })

        btn_mic.isSelected = !isMic

        btn_mic.setOnClickListener(View.OnClickListener {
            val msg: Message = Message.obtain(null, CamStudyService.MSG_HOST_AUDIO_ON_OFF)

            if (isMic) {
                isMic = false
                msg.obj = "off"
                it.isSelected = true
            } else {
                isMic = true
                msg.obj = "on"
                it.isSelected = false
            }

            sendHandlerMessage(msg)
        })

        btn_camera.isSelected = !isVideo

        btn_camera.setOnClickListener(View.OnClickListener {
            val msg: Message = Message.obtain(null, CamStudyService.MSG_HOST_VIDEO_ON_OFF)

            if (isVideo) {
                isVideo = false
                msg.obj = "off"
                it.isSelected = true
            } else {
                isVideo = true
                msg.obj = "on"
                it.isSelected = false
            }

            sendHandlerMessage(msg)
        })

        btn_play.isSelected = isPlay

        btn_play.setOnClickListener(View.OnClickListener {
            if (isPlay) {
                //일시정지
                isPlay = false
                it.isSelected = false

                val msg: Message = Message.obtain(null, CamStudyService.MSG_HOST_TIMER_PAUSE)
                sendHandlerMessage(msg)
            } else {
                //타이머 재생
                isPlay = true
                it.isSelected = true

                val msg: Message = Message.obtain(null, CamStudyService.MSG_HOST_TIMER_RUN)
                sendHandlerMessage(msg)
            }
        })

        btn_chat.setOnClickListener(View.OnClickListener {
            chatDialogView = layoutInflater.inflate(R.layout.dialog_camstudy_chat, null)
            val dialog = BottomSheetDialog(this, R.style.NewDialog)
            dialog.setContentView(chatDialogView!!)
            dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            chat_rv = chatDialogView!!.findViewById<RecyclerView>(R.id.camstudy_chat_recycler)
            val spinner_sender =
                chatDialogView!!.findViewById<Spinner>(R.id.camstudy_chat_spinner_sender)
            val edt_chat = chatDialogView!!.findViewById<EditText>(R.id.camstudy_chat_edt_message)
            val btn_chat = chatDialogView!!.findViewById<ImageView>(R.id.camstudy_chat_btn_send)

            chat_recyclerview_init(CamStudyService.chatDate)

            btn_chat.setOnClickListener(View.OnClickListener {
                var chat = edt_chat.text.toString()

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
                } else {
                    val bundle = Bundle()
                    bundle.putString("msg", chat)
                    bundle.putString("receiver", receiver)

                    val msg: Message = Message.obtain(null, CamStudyService.MSG_WHISPER_MESSAGE)
                    msg.obj = bundle
                    sendHandlerMessage(msg)
                    CamStudyService.chatDate.add(ChatData("total", "나", receiver, chat, time))

                    chat_recyclerview_init(CamStudyService.chatDate)
                }
            })

            val members = CamStudyService.peerConnection.keys
            spinnerInit(chatDialogView, members.toTypedArray())

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

            txt_studyUrl.text = studyInfo!!.result.studyInfo.link

            btn_studyUrl_copy.setOnClickListener(View.OnClickListener {
                val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("label", studyInfo!!.result.studyInfo.link)
                clipboard.setPrimaryClip(clip)
            })

            btn_participants.setOnClickListener(View.OnClickListener {
                val intent = Intent(this, ParticipantsActivity::class.java)
                startActivity(intent)
                dialog.dismiss()
            })
            btn_study_info.setOnClickListener(View.OnClickListener {
                val intent = Intent(this, StudyInfoActivity::class.java)
                intent.putExtra("studyIdx", studyInfo!!.result.studyInfo.idx)
                startActivity(intent)
                dialog.dismiss()
            })
            btn_mystudy_info.setOnClickListener(View.OnClickListener {
                val intent = Intent(this, MyStudyInfoActivity::class.java)
                startActivity(intent)
                dialog.dismiss()
            })

            dialog.show()
        })
    }

    fun spinnerInit(view: View?, member: Array<String>) {
        if (view != null) {
            //스피너
            var members = member
            if (member.isEmpty()) {
                members = arrayOf("모두에게")
            } else {
                members[0] = "모두에게"
            }
            val data = members

            val adapter = ArrayAdapter(this, R.layout.spinner_item_selected_gray, data)
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
        getMenuInflater().inflate(R.menu.camstudy_menu, menu);
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.camera_chang -> {
                Log.d(TAG, "카메라 체인지")
                val msg: Message = Message.obtain(null, CamStudyService.MSG_SWITCH_CAMERA)
                sendHandlerMessage(msg)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private var mConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
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

    private fun sendHandlerMessage(msg: Message) {
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
        val lp = FlexboxLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        lp.order = 1

        if (size == 0) {
            lp.flexGrow = 1.0F
        }

        lp.flexShrink = 1.0F
        lp.flexBasisPercent = 0.5F

        return lp
    }

    private fun setFlexOption(count: Int) {
        if (count == 2) {
            viewDataBinding.camStudyFelxboxLayout.flexDirection = FlexDirection.COLUMN
        } else {
            viewDataBinding.camStudyFelxboxLayout.flexDirection = FlexDirection.ROW
        }

        if(count > 2){
            val lp = viewDataBinding.camStudyFelxboxLayout.layoutParams as FlexboxLayout.LayoutParams
            lp.width = WindowManager.LayoutParams.MATCH_PARENT
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT
            viewDataBinding.camStudyFelxboxLayout.layoutParams = lp
        } else{
            val lp = viewDataBinding.camStudyFelxboxLayout.layoutParams as FlexboxLayout.LayoutParams
            lp.width = WindowManager.LayoutParams.MATCH_PARENT
            lp.height = WindowManager.LayoutParams.MATCH_PARENT
            viewDataBinding.camStudyFelxboxLayout.layoutParams = lp
        }
    }

    inner class CallbackHandler(looper: Looper) : Handler(looper) {
        override fun handleMessage(msg: Message) {
            Log.d(TAG, "Activity에 메세지 도착" + msg.toString())
            when (msg.what) {
                CamStudyService.MSG_EXISTINGPARTICIPANNTS -> {
                    //내가 방에 맨 먼저 참여했을 경우

                    CamStudyService.peerConnection.keys.forEach {
                        val item = CamStudyService.peerConnection.get(it)?.itemView
                        item?.layoutParams =
                            getLayoutParams(viewDataBinding.camStudyFelxboxLayout.flexItemCount)

                        viewDataBinding.camStudyFelxboxLayout.addView(
                            CamStudyService.peerConnection[it]?.itemView
                        )
                    }

                    setFlexOption(viewDataBinding.camStudyFelxboxLayout.flexItemCount)

                    Log.d(TAG, "추가된 뷰의 수 : " + viewDataBinding.camStudyFelxboxLayout.flexItemCount)

                }
                CamStudyService.MSG_NEWPARTICIPANTARRIVED -> {
                    //새로운 참여자가 들어왔을때

                    //레이아웃 초기화
                    viewDataBinding.camStudyFelxboxLayout.removeAllViews()

                    //레이아웃 다시 설정
                    CamStudyService.peerConnection.keys.forEach {
                        val item = CamStudyService.peerConnection.get(it)?.itemView
                        item?.layoutParams =
                            getLayoutParams(viewDataBinding.camStudyFelxboxLayout.flexItemCount)

                        viewDataBinding.camStudyFelxboxLayout.addView(
                            CamStudyService.peerConnection[it]?.itemView
                        )
                    }

                    setFlexOption(viewDataBinding.camStudyFelxboxLayout.flexItemCount)

                }
                CamStudyService.MSG_PARTICIPANTLEFT -> {
                    //누군가 스터디를 떠났을 경우

                    viewDataBinding.camStudyFelxboxLayout.removeAllViews()
                    CamStudyService.peerConnection.keys.forEach {
                        val item = CamStudyService.peerConnection.get(it)?.itemView
                        item?.layoutParams =
                            getLayoutParams(viewDataBinding.camStudyFelxboxLayout.flexItemCount)

                        viewDataBinding.camStudyFelxboxLayout.addView(
                            CamStudyService.peerConnection.get(
                                it
                            )?.itemView
                        )
                    }

                    setFlexOption(viewDataBinding.camStudyFelxboxLayout.flexItemCount)

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
                    MaterialAlertDialogBuilder(this@CamStudyActivity)
                        .setMessage("스터디에서 추방되었습니다.")
                        .setPositiveButton("확인", DialogInterface.OnClickListener { dialog, which ->
                            val msg: Message =
                                Message.obtain(null, CamStudyService.MSG_COMSTUDY_LEFT)
                            sendHandlerMessage(msg)
                        }).show()
                }
                CamStudyService.MSG_LEADER_FORCED_AUDIO_OFF -> {
                    //리더에게 마이크 off
                    val btn_mic = findViewById<ImageButton>(R.id.camstudy_btn_mic)

                    isMic = false
                    btn_mic.isSelected = true
                }
                CamStudyService.MSG_LEADER_FORCED_VIDEO_OFF -> {
                    //리더에게 카메라 off
                    val btn_camera = findViewById<ImageButton>(R.id.camstudy_btn_camera)

                    isVideo = false
                    btn_camera.isSelected = true
                }
            }
        }
    }

}