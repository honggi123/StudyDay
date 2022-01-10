package com.coworkerteam.coworker.ui.camstudy.cam

import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.*
import android.util.Log

import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.coworkerteam.coworker.CamStudyService
import com.coworkerteam.coworker.R
import com.coworkerteam.coworker.data.local.prefs.AppPreferencesHelper
import com.coworkerteam.coworker.data.model.api.EnterCamstudyResponse
import com.coworkerteam.coworker.data.model.other.CamStudyHandler
import com.coworkerteam.coworker.data.model.other.Chat
import com.coworkerteam.coworker.data.model.other.Participant
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
import kotlin.collections.HashMap

class CamStudyActivity : BaseActivity<ActivityCamStudyBinding, CamStudyViewModel>() {


    val TAG = "CamStudyActivity"

    override val layoutResourceID: Int
        get() = R.layout.activity_cam_study
    override val viewModel: CamStudyViewModel by viewModel()

    companion object {
        var handler: Handler? = null

    }

    var chat_rv: RecyclerView? = null

    var isMic = true
    var isVideo = true
    var isPlay = false

    var timer : Int? = null
    var studyInfo : EnterCamstudyResponse? = null


    override fun initStartView() {
        initData()

        var main_toolbar: androidx.appcompat.widget.Toolbar =
            findViewById(R.id.camstudy_layout_toolbar)

        setSupportActionBar(main_toolbar) // 툴바를 액티비티의 앱바로 지정
        supportActionBar?.setDisplayShowTitleEnabled(false) // 툴바에 타이틀 안보이게

        init()
        handler_init()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            var intent = Intent(this, CamStudyService::class.java)
            intent.putExtra("audio", isMic)
            intent.putExtra("video", isVideo)
            intent.putExtra("cameraSwith","front")
            intent.putExtra("studyInfo",studyInfo)
            intent.putExtra("timer",timer)
            startService(intent)
        }
    }

    override fun initDataBinding() {

    }

    override fun initAfterBinding() {

    }

    fun initData() {
        isMic = intent.getBooleanExtra("audio", false)
        isVideo = intent.getBooleanExtra("video", false)
        studyInfo = intent.getSerializableExtra("studyInfo") as EnterCamstudyResponse?
        timer = intent.getIntExtra("timer",0)
    }

    fun handler_init() {
        handler = Handler(Looper.getMainLooper()) {
            when (it.what) {
                0 -> {
                    //캠스터디 아이템
                    var obj = it.obj as CamStudyHandler
                    recyclerview_init(obj.data, obj.hash)
                }
                1 -> {
                    //채팅 아이템
                    var obj = it.obj as ArrayList<Chat>
                    chat_recyclerview_init(obj)
                }
                2 -> {
                    //종료시 타이머 가져오기
                    stopCamstudy(it.arg1,it.arg2)
                    Log.d("핸들러","핸들러 메시지 테스트 Service")
                }
                3 ->{
                    //리더에게 마이크 off
                    val btn_mic = findViewById<ImageButton>(R.id.camstudy_btn_mic)

                    isMic = false
                    btn_mic.isSelected = true
                }
                4 ->{
                    //리더에게 카메라 off
                    val btn_camera = findViewById<ImageButton>(R.id.camstudy_btn_camera)

                    isVideo = false
                    btn_camera.isSelected = true
                }
            }
            true
        }

    }

    fun stopCamstudy(studyTime:Int, restTime:Int){
        var studyIdx = studyInfo!!.result.studyInfo.idx
        var studyTimeValue = if ( studyTime < 0) 0 else studyTime

        viewModel.getCamstduyLeaveData(studyIdx!!,studyTimeValue,restTime)
        stopService(Intent(this, CamStudyService::class.java))
        finish()
    }

    fun recyclerview_init(data: MutableList<String>, hash: HashMap<String, Participant>) {
        var recyclerNewStudy: RecyclerView =
            findViewById(R.id.cam_study_rv)
        var newAdapter = CamStudyAdapter(this)
        newAdapter.datas = data
        newAdapter.hashmap = hash

        recyclerNewStudy.adapter = newAdapter
    }

    fun chat_recyclerview_init(data: ArrayList<Chat>) {
        if (chat_rv != null) {
            var newAdapter = ChatAdapter(this)
            newAdapter.datas = data.toMutableList()

            chat_rv!!.adapter = newAdapter
            chat_rv!!.scrollToPosition(newAdapter.itemCount-1)
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
            CamStudyService.handler!!.sendEmptyMessage(7)
        })

        btn_mic.setOnClickListener(View.OnClickListener {
            Log.d(TAG, "btn_mic 클릭")

            var msg = Message()
            msg.what = 2

            if (isMic) {
                isMic = false
                msg.obj = "off"
                it.isSelected = true
            } else {
                isMic = true
                msg.obj = "on"
                it.isSelected = false
            }

            CamStudyService.handler!!.sendMessage(msg)
        })

        btn_camera.setOnClickListener(View.OnClickListener {
            Log.d(TAG, "btn_camera 클릭")
            var msg = Message()
            msg.what = 3
            if (isVideo) {
                isVideo = false
                msg.obj = "off"
                it.isSelected = true
            } else {
                isVideo = true
                msg.obj = "on"
                it.isSelected = false
            }
            CamStudyService.handler!!.sendMessage(msg)
        })

        btn_play.setOnClickListener(View.OnClickListener {
            Log.d(TAG, "btn_play 클릭")
            if (isPlay) {
                //일시정지
                isPlay = false
                it.isSelected = false

                CamStudyService.handler!!.sendEmptyMessage(6)
            } else {
                //타이머 재생
                isPlay = true
                it.isSelected = true
                CamStudyService.handler!!.sendEmptyMessage(5)
            }
        })
        btn_chat.setOnClickListener(View.OnClickListener {
            Log.d(TAG, "btn_chat 클릭")
            val dialogView: View = layoutInflater.inflate(R.layout.camstudy_chat, null)
            val dialog = BottomSheetDialog(this, R.style.NewDialog)
            dialog.setContentView(dialogView)
            dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            chat_rv = dialogView.findViewById<RecyclerView>(R.id.camstudy_chat_recycler)
            val spinner_sender = dialogView.findViewById<Spinner>(R.id.camstudy_chat_spinner_sender)
            val edt_chat = dialogView.findViewById<EditText>(R.id.camstudy_chat_edt_message)
            val btn_chat = dialogView.findViewById<ImageView>(R.id.camstudy_chat_btn_send)

            chat_recyclerview_init(CamStudyService.chatDate)

            btn_chat.setOnClickListener(View.OnClickListener {
                var message = Message()
                var chat = edt_chat.text.toString()
                edt_chat.setText("")

                message.what = 0
                message.obj = chat

                var dataformat = SimpleDateFormat("a HH:mm", Locale.KOREA)
                var time = dataformat.format(System.currentTimeMillis())

                CamStudyService.handler?.sendMessage(message)
                CamStudyService.chatDate.add(Chat("total", "나", chat, time))

                chat_recyclerview_init(CamStudyService.chatDate)
            })

            spinnerInit(dialogView)

            dialog.setOnDismissListener(DialogInterface.OnDismissListener {
                chat_rv = null
            })

            dialog.show()
        })

        btn_more.setOnClickListener(View.OnClickListener {

            Log.d(TAG, "btn_more 클릭")
            val dialogView: View = layoutInflater.inflate(R.layout.camstudy_bottom_menu, null)
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

            txt_studyUrl.text = studyInfo!!.result.studyInfo.link

            btn_participants.setOnClickListener(View.OnClickListener {
                val intent = Intent(this, ParticipantsActivity::class.java)
                startActivity(intent)
                dialog.dismiss()
            })
            btn_study_info.setOnClickListener(View.OnClickListener {
                val intent = Intent(this, StudyInfoActivity::class.java)
                intent.putExtra("studyIdx",studyInfo!!.result.studyInfo.idx)
                startActivity(intent)
                dialog.dismiss()
            })
            btn_mystudy_info.setOnClickListener(View.OnClickListener {
                val intent = Intent(this, MyStudyInfoActivity::class.java)
                startActivity(intent)
                dialog.dismiss()
            })

            dialog.show()
//            dialog.findViewById<R.id.>()
        })
    }

    fun spinnerInit(view: View) {
        //스피너
        val data = arrayOf("모두에게", "이현주", "암ㄴ옮ㄴ아롬니올먼올머ㅏ농러ㅏ")

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
//                if (parent.getItemAtPosition(position).toString() == "오픈스터디") {
//                    NewStudyShowOpen = true
//                    if(setData) {
//                        NewStudy_init()
//                    }
//                } else {
//                    NewStudyShowOpen = false
//                    if(setData) {
//                        NewStudy_init()
//                    }
//                }

            }

            override fun onNothingSelected(parent: AdapterView<*>) {

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
                CamStudyService.handler!!.sendEmptyMessage(4)
            }
        }
        return super.onOptionsItemSelected(item)
    }

}