package com.coworkerteam.coworker.ui.main

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.RecyclerView
import com.coworkerteam.coworker.R
import com.coworkerteam.coworker.data.model.other.DrawerBottomInfo
import com.coworkerteam.coworker.databinding.ActivityMainBinding
import com.coworkerteam.coworker.ui.base.NavigationAcitivity
import com.coworkerteam.coworker.ui.camstudy.enter.EnterCamstudyActivity
import com.coworkerteam.coworker.ui.dialog.PasswordDialog
import com.coworkerteam.coworker.ui.study.make.MakeStudyActivity
import com.coworkerteam.coworker.ui.todolist.TodoListActivity
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.navigation.NavigationView
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import org.json.JSONObject
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.text.SimpleDateFormat

class MainActivity : NavigationAcitivity<ActivityMainBinding, MainViewModel>() {

    val TAG = "MainActivity"
    override val layoutResourceID: Int
        get() = R.layout.activity_main
    override val viewModel: MainViewModel by viewModel()

    override val drawerLayout: DrawerLayout
        get() = findViewById(R.id.drawer_layout)
    override val navigatinView: NavigationView
        get() = findViewById(R.id.navigationView)

    var NewStudyShowOpen: Boolean = true
    var RecommendStudyShowOpen: Boolean = true
    var setData: Boolean = false

    lateinit var pagingMainMyStudyAdapter: MainMyStudyPagingAdapter

    val passwordDialog = PasswordDialog()

    override fun initStartView() {
        super.initStartView()
        viewDataBinding.activitiy = this

        //툴바 세팅
        var main_toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.main_toolber)

        setSupportActionBar(main_toolbar) // 툴바를 액티비티의 앱바로 지정
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // 드로어를 꺼낼 홈 버튼 활성화
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_baseline_menu_24_write) // 홈버튼 이미지 변경
        supportActionBar?.title = getString(R.string.app_name)

        //툴바의 + 아이콘에 대한 세팅
        val mainToolbarMakeStudy = findViewById<ImageView>(R.id.main_toolbar_makeStudy)
        mainToolbarMakeStudy.visibility = View.VISIBLE
        mainToolbarMakeStudy.setOnClickListener(
            View.OnClickListener {
                var intent = Intent(this, MakeStudyActivity::class.java)
                startActivity(intent)
            }
        )

        //내스터디 페이징 Adapter 적용
        pagingMainMyStudyAdapter = MainMyStudyPagingAdapter(viewModel)
        val rv_MyStudy = findViewById<RecyclerView>(R.id.main_mystudy_recylerView)
        rv_MyStudy.adapter = pagingMainMyStudyAdapter

        //패스워드 다이얼로그 ok버튼 함수 세팅
        passwordDialog.onClickOKButton = {i: Int, s: String? ->
            viewModel.getEnterCamstduyData(i, s)
        }

        init()
    }

    override fun initDataBinding() {
        viewModel.EnterCamstudyResponseLiveData.observe(this, androidx.lifecycle.Observer {
            when {
                it.isSuccessful -> {
                    var intent = Intent(this, EnterCamstudyActivity::class.java)
                    intent.putExtra("studyInfo", it.body()!!)

                    passwordDialog.dismissDialog()
                    startActivity(intent)
                }
                it.code() == 400 -> {
                    //요청값을 제대로 다 전달하지 않은 경우 ex. 날짜 또는 요청타입 값이 잘못되거나 없을때
                    val errorMessage = JSONObject(it.errorBody()?.string())
                    Log.e(TAG, errorMessage.getString("message"))

                    //400번대 에러로 스터디 입장페이지 진입 실패했을 경우, 사용자에게 알려준다.
                    Toast.makeText(this,"스터디에 입장할 수 없습니다. 나중 다시 시도해주세요.",Toast.LENGTH_SHORT).show()
                }
                it.code() == 403 -> {
                    val errorMessage = JSONObject(it.errorBody()?.string())
                    Log.e(TAG, errorMessage.getString("message"))

                    when(errorMessage.getInt("code")){
                        -4 ->{
                            //해당 스터디에 강제 탈퇴 당해 더 이상 입장할 수 없는 경우
                            Toast.makeText(this,"강제 퇴장당한 스터디입니다. 입장할 수 없습니다.",Toast.LENGTH_SHORT).show()
                        }
                        -5 ->{
                            //참여중인 스터디가 있을 경우
                            Toast.makeText(this,"이미 공부중인 스터디가 있습니다. 바로 참여할 수 없습니다.",Toast.LENGTH_SHORT).show()
                        }
                        -12 ->{
                            //비밀번호를 틀린 경우
                            passwordDialog.setErrorMessage(errorMessage.getString("message"))
                        }
                    }
                }
                it.code() == 404 -> {
                    val errorMessage = JSONObject(it.errorBody()?.string())
                    Log.e(TAG, errorMessage.getString("message"))

                    when(errorMessage.getInt("code")){
                        -2 ->{
                            //존재하지 않는 회원인 경우
                            moveLogin()
                        }
                        -3 ->{
                            //존재하지 않는 스터디일 경우
                            Toast.makeText(this,"더이상 존재하지 않는 스터디입니다.",Toast.LENGTH_SHORT).show()
                            finish()
                        }
                    }
                }

            }
        })

        viewModel.MainResponseLiveData.observe(this, androidx.lifecycle.Observer {
            when {
                it.isSuccessful -> {
                    viewDataBinding.mainResponse = it.body()!!

                    //네비게이션 정보 세팅
                    setNavigaionProfileImage(it.body()!!.result[0].profile.img)
                    setNavigaionLoginImage(it.body()!!.result[0].profile.loginType)
                    setNavigaionNickname(it.body()!!.result[0].profile.nickname)

                    viewDataBinding.draworInfo = DrawerBottomInfo(it.body()!!.result[0].achieveTimeRate,it.body()!!.result[0].achieveTodoRate,it.body()!!.result[0].dream.dday,it.body()!!.result[0].dream.ddayName)

                    Log.d("디버그태그", it.body()!!.result[0].achieveTodoRate.toString())
                    //내스터디
                    var recyclerMyStudy: RecyclerView =
                        findViewById(R.id.main_todolist_recylerView)
                    var myStudyAdepter: MainTodolistAdapter =
                        MainTodolistAdapter(this, viewModel)
                    myStudyAdepter.datas = it.body()!!.result[0].todo.toMutableList()
                    recyclerMyStudy.adapter = myStudyAdepter

                    setData = true
                    NewStudy_init()
                    Recommend_init()
                }
                it.code() == 400 -> {
                    //요청값을 제대로 다 전달하지 않은 경우 ex. 날짜 또는 요청타입 값이 잘못되거나 없을때
                    val errorMessage = JSONObject(it.errorBody()?.string())
                    Log.e(TAG, errorMessage.getString("message"))

                    //400번대 에러로 메인 데이터 가져오기가 실패했을 경우, 사용자에게 알려준다.
                    Toast.makeText(this,"메인 데이터를 가져오지 못했습니다. 나중 다시 시도해주세요.",Toast.LENGTH_SHORT).show()
                }
                it.code() == 404 -> {
                    //존재하지 않은 회원일 경우
                    val errorMessage = JSONObject(it.errorBody()?.string())
                    Log.e(TAG, errorMessage.getString("message"))

                    moveLogin()

                }
            }
        })

        viewModel.EditGoalResponseLiveData.observe(this, androidx.lifecycle.Observer {
            when {
                it.isSuccessful -> {
                    Log.d("확인", it.body().toString())
                    viewDataBinding.mainResponse!!.result[0].dream = it.body()!!.result.dream
                    viewDataBinding.mainResponse!!.result[0].achieveTimeRate = it.body()!!.result.achieveTimeRate
                    viewDataBinding.mainResponse = viewDataBinding.mainResponse
                }
                it.code() == 400 -> {
                    //요청값을 제대로 다 전달하지 않은 경우 ex. 날짜 또는 요청타입 값이 잘못되거나 없을때
                    val errorMessage = JSONObject(it.errorBody()?.string())
                    Log.e(TAG, errorMessage.getString("message"))

                    //400번대 에러로 목표 수정이 실패했을 경우, 사용자에게 알려준다.
                    Toast.makeText(this,"목표 수정에 실패했습니다. 나중에 다시 시도해주세요.",Toast.LENGTH_SHORT).show()
                }
                it.code() == 404 -> {
                    //존재하지 않은 회원일 경우
                    val errorMessage = JSONObject(it.errorBody()?.string())
                    Log.e(TAG, errorMessage.getString("message"))

                    moveLogin()

                }
            }
        })

        viewModel.CheckTodoListResponseLiveData.observe(this, androidx.lifecycle.Observer {
            when {
                it.isSuccessful -> {
                    //네비게이션 드로어 오늘 할일 달성률 갱신
                    viewDataBinding.draworInfo!!.achieveTodoRate = it.body()!!.result.achieveTodoRate
                    viewDataBinding.draworInfo = viewDataBinding.draworInfo
                }
                it.code() == 400 -> {
                    //요청값을 제대로 다 전달하지 않은 경우 ex. 날짜 또는 요청타입 값이 잘못되거나 없을때
                    val errorMessage = JSONObject(it.errorBody()?.string())
                    Log.e(TAG, errorMessage.getString("message"))

                    //400번대 에러로 투두리스트 체크 실패했을 경우, 사용자에게 알려준다.
                    Toast.makeText(this,"투두리스트 체크에 실패했습니다. 나중 다시 시도해주세요.",Toast.LENGTH_SHORT).show()
                }
                it.code() == 404 -> {
                    //존재하지 않은 회원일 경우
                    val errorMessage = JSONObject(it.errorBody()?.string())
                    Log.e(TAG, errorMessage.getString("message"))

                    when(errorMessage.getInt("code")){
                        -2 ->{
                            //존재하지 않는 회원인 경우
                            moveLogin()
                        }
                        -7 ->{
                            //수정하고자 하는 할 일이 실제로 존재하지 않은 경우
                            Toast.makeText(this,"해당 할일이 존재하지 않습니다.",Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        })

        viewModel.MyStudyPagingData.observe(this, androidx.lifecycle.Observer {
            pagingMainMyStudyAdapter.submitData(lifecycle, it)
        })
    }

    override fun initAfterBinding() {
        viewModel.getMainData()
    }

    override fun onPostResume() {
        super.onPostResume()
        viewModel.getMainData()
    }

    fun init() {
        //스피너
        val data = arrayOf("일일스터디", "그룹스터디")

        val adapter = ArrayAdapter(this, R.layout.spinner_item_selected, data)
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)

        val spinner_new = findViewById<Spinner>(R.id.main_spinner_new_study)
        val spinner_dcommend = findViewById<Spinner>(R.id.main_spinner_dcommend)

        spinner_new.adapter = adapter
        spinner_new.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View,
                position: Int,
                id: Long
            ) {
                if (parent.getItemAtPosition(position).toString() == "일일스터디") {
                    NewStudyShowOpen = true
                    if (setData) {
                        NewStudy_init()
                    }
                } else {
                    NewStudyShowOpen = false
                    if (setData) {
                        NewStudy_init()
                    }
                }

            }

            override fun onNothingSelected(parent: AdapterView<*>) {

            }
        }

        spinner_dcommend.adapter = adapter
        spinner_dcommend.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View,
                position: Int,
                id: Long
            ) {
                if (parent.getItemAtPosition(position).toString() == "일일스터디") {
                    RecommendStudyShowOpen = true
                    if (setData) {
                        Recommend_init()
                    }
                } else {
                    RecommendStudyShowOpen = false
                    if (setData) {
                        Recommend_init()
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {

            }
        }
    }

    fun showPopupMenu(v: View) {
        var popup = PopupMenu(this, v)
        var con = this
        con.menuInflater?.inflate(R.menu.main_popup_menu, popup.menu)
        popup.setOnMenuItemClickListener(object : PopupMenu.OnMenuItemClickListener {
            override fun onMenuItemClick(item: MenuItem?): Boolean {
                when (item?.itemId) {
                    R.id.goal_setting -> {
                        Log.d(TAG, "goal_setting")
                        val mDialogView =
                            LayoutInflater.from(con).inflate(R.layout.dialog_goal, null)
                        val mBuilder = AlertDialog.Builder(con).setView(mDialogView)
                        val builder = mBuilder.show()

                        builder.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

                        val editTime =
                            mDialogView.findViewById<TextInputLayout>(R.id.dialog_goal_edit_goal_time)
                        val editGoal =
                            mDialogView.findViewById<TextInputLayout>(R.id.dialog_goal_edit_goal)

                        editTime.editText?.setText(viewDataBinding.mainResponse!!.result[0].aimTime)
                        editGoal.editText?.setText(viewDataBinding.mainResponse!!.result[0].dream.goal)

                        editTime.editText?.setOnClickListener(View.OnClickListener {
                            MaterialTimePicker.Builder()
                                .setTimeFormat(TimeFormat.CLOCK_24H)
                                .setHour(0)
                                .setMinute(0)
                                .build()
                                .apply {
                                    addOnPositiveButtonClickListener {
                                        editTime.editText?.setText(
                                            onTimeSelected(
                                                this.hour,
                                                this.minute
                                            )
                                        )
                                    }
                                }.show(
                                    supportFragmentManager,
                                    MaterialTimePicker::class.java.canonicalName
                                )
                        })

                        val btn_cancle =
                            mDialogView.findViewById<Button>(R.id.dialog_goal_btn_cancle)
                        val btn_ok = mDialogView.findViewById<Button>(R.id.dialog_goal_btn_ok)

                        btn_cancle.setOnClickListener(View.OnClickListener {
                            builder.dismiss()
                        })

                        btn_ok.setOnClickListener(View.OnClickListener {
                            val aimTime = changTime(editTime.editText?.text.toString()).toString()
                            val goal = editGoal.editText?.text.toString()

                            viewDataBinding.mainResponse!!.result[0].aimTime = editTime.editText?.text.toString()
                            viewDataBinding.mainResponse!!.result[0].dream.goal = goal

                            viewModel.setGoalCamstduyData(aimTime,goal,viewDataBinding.mainResponse!!.result[0].dream.ddayDate,viewDataBinding.mainResponse!!.result[0].dream.ddayName)
                            builder.dismiss()
                        })
                    }

                    R.id.ddat_setting -> {
                        Log.d(TAG, "ddat_setting")
                        val mDialogView =
                            LayoutInflater.from(con).inflate(R.layout.dialog_dday, null)
                        val mBuilder = AlertDialog.Builder(con).setView(mDialogView)
                        val builder = mBuilder.show()

                        builder.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

                        val editDate =
                            mDialogView.findViewById<TextInputLayout>(R.id.dialog_dday_edit_day)
                        val editDdayName = mDialogView.findViewById<TextInputLayout>(R.id.dialog_dday_edit_name)

                        editDate.editText?.setText(viewDataBinding.mainResponse!!.result[0].dream.ddayDate)
                        editDdayName.editText?.setText(viewDataBinding.mainResponse!!.result[0].dream.ddayName)

                        editDate.editText?.setOnClickListener(View.OnClickListener {
                            MaterialDatePicker.Builder.datePicker()
                                .setTitleText("Select date")
                                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                                .build()
                                .apply {
                                    addOnPositiveButtonClickListener {
                                        var simpleDateFormat = SimpleDateFormat("yyyy-MM-dd")
                                        editDate.editText?.setText(simpleDateFormat.format(it))
                                    }
                                }.show(
                                    supportFragmentManager,
                                    MaterialDatePicker::class.java.canonicalName
                                )
                        })

                        val btn_cancle =
                            mDialogView.findViewById<Button>(R.id.dialog_goal_btn_cancle)
                        val btn_ok = mDialogView.findViewById<Button>(R.id.dialog_goal_btn_ok)

                        btn_cancle.setOnClickListener(View.OnClickListener {
                            builder.dismiss()
                        })

                        btn_ok.setOnClickListener(View.OnClickListener {
                            val dday = editDate.editText?.text.toString()
                            val ddayName = editDdayName.editText?.text.toString()

                            viewDataBinding.mainResponse!!.result[0].dream.ddayDate = dday
                            viewDataBinding.mainResponse!!.result[0].dream.ddayName = ddayName

                            viewModel.setGoalCamstduyData(changTime(viewDataBinding.mainResponse!!.result[0].aimTime).toString(),viewDataBinding.mainResponse!!.result[0].dream.goal,dday,ddayName)
                            builder.dismiss()
                        })

                    }
                }

                return false
            }
        })
        popup.show()
    }

    private fun onTimeSelected(hour: Int, minute: Int): String {
        var selectedHour = hour
        var selectedMinute = minute
        val hourAsText = if (hour < 10) "0$hour" else hour
        val minuteAsText = if (minute < 10) "0$minute" else minute

        return "$hourAsText:$minuteAsText"
    }

    fun NewStudy_init() {
        //새로운
        var recyclerNewStudy: RecyclerView =
            findViewById(R.id.main_newstudy_recylerView)
        var newAdapter = MainOtherStudyAdapter(this,passwordDialog)

        if (NewStudyShowOpen) {
            newAdapter.datas = viewDataBinding.mainResponse!!.result[0].newOpenStudy.toMutableList()
            viewDataBinding.isNewStudy = if(viewDataBinding.mainResponse!!.result[0].newOpenStudy.size < 1) true else false
        } else {
            newAdapter.datas = viewDataBinding.mainResponse!!.result[0].newGroupStudy.toMutableList()
            viewDataBinding.isNewStudy = if(viewDataBinding.mainResponse!!.result[0].newGroupStudy.size < 1) true else false
        }
        recyclerNewStudy.adapter = newAdapter
    }


    fun Recommend_init() {
        //추천
        var recyclerRecommendStudy: RecyclerView =
            findViewById(R.id.main_recommend_study_recylerView)
        var recommendAdapter: MainOtherStudyAdapter = MainOtherStudyAdapter(this,passwordDialog)

        if (RecommendStudyShowOpen) {
            recommendAdapter.datas = viewDataBinding.mainResponse!!.result[0].openRecommend.toMutableList()
            viewDataBinding.isRecommendStudy = if(viewDataBinding.mainResponse!!.result[0].openRecommend.size < 1) true else false
        } else {
            recommendAdapter.datas = viewDataBinding.mainResponse!!.result[0].groupRecommend.toMutableList()
            viewDataBinding.isRecommendStudy = if(viewDataBinding.mainResponse!!.result[0].groupRecommend.size < 1) true else false
        }
        recyclerRecommendStudy.adapter = recommendAdapter
    }

    fun changTime(goalTime: String): Int {
        val time = goalTime.split(":")
        val hour = time[0].toInt() * 60 * 60
        val minute = time[1].toInt() * 60

        return hour + minute
    }

    fun moveTodolist(){
        var intent = Intent(this, TodoListActivity::class.java)
        startActivity(intent)
        finish()
    }

}