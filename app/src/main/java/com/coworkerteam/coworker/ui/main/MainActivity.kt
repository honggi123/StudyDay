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
import com.coworkerteam.coworker.data.model.api.MainResponse
import com.coworkerteam.coworker.databinding.ActivityMainBinding
import com.coworkerteam.coworker.ui.base.NavigationAcitivity
import com.coworkerteam.coworker.ui.camstudy.enter.EnterCamstudyActivity
import com.coworkerteam.coworker.ui.study.make.MakeStudyActivity
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.navigation.NavigationView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
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

    lateinit var mainResponse: MainResponse
    var NewStudyShowOpen: Boolean = true
    var RecommendStudyShowOpen: Boolean = true
    var setData: Boolean = false

    lateinit var pagingMainMyStudyAdapter: MainMyStudyPagingAdapter

    override fun initStartView() {
        super.initStartView()
        viewDataBinding.activitiy = this

        var main_toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.main_toolber)

        setSupportActionBar(main_toolbar) // 툴바를 액티비티의 앱바로 지정
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // 드로어를 꺼낼 홈 버튼 활성화
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_baseline_menu_24_write) // 홈버튼 이미지 변경
        supportActionBar?.title = getString(R.string.app_name)

        val mainToolbarMakeStudy = findViewById<ImageView>(R.id.main_toolbar_makeStudy)
        mainToolbarMakeStudy.visibility = View.VISIBLE
        mainToolbarMakeStudy.setOnClickListener(
            View.OnClickListener {
                var intent = Intent(this, MakeStudyActivity::class.java)
                startActivity(intent)
            }
        )

        pagingMainMyStudyAdapter = MainMyStudyPagingAdapter(viewModel)
        val rv_MyStudy = findViewById<RecyclerView>(R.id.main_mystudy_recylerView)
        rv_MyStudy.adapter = pagingMainMyStudyAdapter

        init()
    }

    override fun initDataBinding() {
        viewModel.EnterCamstudyResponseLiveData.observe(this, androidx.lifecycle.Observer {
            if (it.isSuccessful) {
                var intent = Intent(this, EnterCamstudyActivity::class.java)
                intent.putExtra("studyInfo", it.body()!!)
                Log.d(TAG, it.toString())
                startActivity(intent)
            } else if (it.code() == 403) {
                Log.d(TAG, "403스테이스 코드 메시지 : " + it.message())
                Log.d(TAG, "403스테이스 코드 바디 메시지 : " + it.body().toString())
                Log.d(TAG, "403스테이스 코드 에러 바디 메시지 : " + it.errorBody()?.string())
            }
        })

        viewModel.MainResponseLiveData.observe(this, androidx.lifecycle.Observer {
            if (it.isSuccessful) {
                mainResponse = it.body()!!

                setNavigaionProfileImage(it.body()!!.result[0].profile.img)
                setNavigaionLoginImage(it.body()!!.result[0].profile.loginType)
                setNavigaionNickname(it.body()!!.result[0].profile.nickname)

                Log.d("디버그태그", it.body()!!.result[0].todo.toString())
                //내스터디
                var recyclerMyStudy: RecyclerView =
                    findViewById(R.id.main_todolist_recylerView)
                var myStudyAdepter: MainTodolistAdapter =
                    MainTodolistAdapter(this, viewModel)
                myStudyAdepter.datas = it.body()!!.result[0].todo.toMutableList()
                recyclerMyStudy.adapter = myStudyAdepter

                var goal = findViewById<TextView>(R.id.textView2)

                goal.setText(mainResponse.result[0].dream.goal)
//                        Log.d("디버그태그",mainResponse.result[0].dream.goal)

                setData = true
                NewStudy_init()
                Recommend_init()
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

                        editTime.editText?.setText(mainResponse.result[0].aimTime)
                        editGoal.editText?.setText(mainResponse.result[0].dream.goal)

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

                            mainResponse.result[0].aimTime = editTime.editText?.text.toString()
                            mainResponse.result[0].dream.goal = goal

                            viewModel.setGoalCamstduyData(aimTime,goal,mainResponse.result[0].dream.ddayDate,mainResponse.result[0].dream.ddayName)
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

                        editDate.editText?.setText(mainResponse.result[0].dream.ddayDate)
                        editDdayName.editText?.setText(mainResponse.result[0].dream.ddayName)

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

                            mainResponse.result[0].dream.ddayDate = dday
                            mainResponse.result[0].dream.ddayName = ddayName

                            viewModel.setGoalCamstduyData(mainResponse.result[0].aimTime,mainResponse.result[0].dream.goal,dday,ddayName)
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
        var newAdapter: MainOtherStudyAdapter = MainOtherStudyAdapter(this, viewModel)

        if (NewStudyShowOpen) {
            newAdapter.datas = mainResponse.result[0].newOpenStudy.toMutableList()
        } else {
            newAdapter.datas = mainResponse.result[0].newGroupStudy.toMutableList()
        }
        recyclerNewStudy.adapter = newAdapter
    }


    fun Recommend_init() {
        //추천
        var recyclerRecommendStudy: RecyclerView =
            findViewById(R.id.main_recommend_study_recylerView)
        var recommendAdapter: MainOtherStudyAdapter = MainOtherStudyAdapter(this, viewModel)

        if (RecommendStudyShowOpen) {
            recommendAdapter.datas = mainResponse.result[0].openRecommend.toMutableList()
        } else {
            recommendAdapter.datas = mainResponse.result[0].groupRecommend.toMutableList()
        }
        recyclerRecommendStudy.adapter = recommendAdapter
    }

    fun changTime(goalTime: String): Int {
        val time = goalTime.split(":")
        val hour = time[0].toInt() * 60 * 60
        val minute = time[1].toInt() * 60

        return hour + minute
    }

}