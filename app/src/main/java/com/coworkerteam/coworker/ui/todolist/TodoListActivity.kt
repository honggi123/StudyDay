package com.coworkerteam.coworker.ui.todolist

import android.app.DatePickerDialog
import android.widget.Toast

import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.RecyclerView
import com.coworkerteam.coworker.R
import com.coworkerteam.coworker.data.model.api.TodolistResponse
import com.coworkerteam.coworker.data.model.custom.EventDecorator
import com.coworkerteam.coworker.data.model.other.DrawerBottomInfo
import com.coworkerteam.coworker.databinding.ActivityTodoListBinding
import com.coworkerteam.coworker.ui.base.NavigationAcitivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.android.material.textfield.TextInputLayout
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.CalendarMode
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener
import com.prolificinteractive.materialcalendarview.OnMonthChangedListener
import com.prolificinteractive.materialcalendarview.format.TitleFormatter
import org.json.JSONObject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.threeten.bp.DayOfWeek
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class TodoListActivity : NavigationAcitivity<ActivityTodoListBinding, TodoListViewModel>() {
    private val TAG = "TodoListActivity"

    override val layoutResourceID: Int
        get() = R.layout.activity_todo_list
    override val viewModel: TodoListViewModel by viewModel()

    override val drawerLayout: DrawerLayout
        get() = findViewById(R.id.drawer_layout)
    override val navigatinView: NavigationView
        get() = findViewById(R.id.navigationView)

    var myStudyAdepter: TodoListAdapter? = null
    var selectData = getToday() //API의 데이터 포멧에 맞춘 선택한 날짜의 값

    private val dateFormat: SimpleDateFormat =
        SimpleDateFormat("MM.dd",  /*Locale.getDefault()*/Locale.KOREA)

    override fun initStartView() {
        super.initStartView()
        var main_toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.main_toolber)

        setSupportActionBar(main_toolbar) // 툴바를 액티비티의 앱바로 지정
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // 드로어를 꺼낼 홈 버튼 활성화
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_baseline_menu_24_write) // 홈버튼 이미지 변경
        supportActionBar?.title = "투두리스트"

        init()
    }

    override fun initDataBinding() {
        viewModel.TodoListResponseLiveData.observe(this, androidx.lifecycle.Observer {
            when {
                it.isSuccessful -> {
                    //데코레이터용 날짜 받아올때는 DataBinding에 넣지 않음
                    if (!it.body()!!.message.equals("선택한 달에서, 할 일이 있는 날짜입니다.")) {
                        viewDataBinding.todolistResponse = it.body()!!
                    }

                    //네비게이션 바에 넣을 정보가 있을 경우
                    if (it.body()!!.result.profile != null) {
                        setNavigaionLoginImage(viewDataBinding.todolistResponse!!.result.profile.loginType)
                        setNavigaionProfileImage(viewDataBinding.todolistResponse!!.result.profile.img)
                        setNavigaionNickname(viewDataBinding.todolistResponse!!.result.profile.nickname)

                        viewDataBinding.draworInfo = DrawerBottomInfo(
                            it.body()!!.result.achieveTimeRate,
                            it.body()!!.result.achieveTodoRate,
                            it.body()!!.result.dream.dday,
                            it.body()!!.result.dream.ddayName
                        )
                    }

                    //프로그레스바, 투두리스트 항목이 있을 경우
                    if (it.body()!!.result.theDayTodo != null) {
                        rv_init()
                        viewDataBinding.todoListProgress.progress = it.body()!!.result.theDayAcheiveRate
                    }

                    //데코레이터 추가할 날짜가 있을 경우
                    if (it.body()!!.result.todoDate != null) {
                        var decorators = ArrayList<CalendarDay>()
                        it.body()!!.result.todoDate.forEach {
                            val date = it.split("-")
                            decorators.add(
                                CalendarDay.from(
                                    date[0].toInt(),
                                    date[1].toInt(),
                                    date[2].toInt()
                                )
                            )
                        }
                        viewDataBinding.calendarView.addDecorator(EventDecorator(decorators))
                    }
                }
                it.code() == 400 -> {
                    //요청값을 제대로 다 전달하지 않은 경우 ex. 날짜 또는 요청타입 값이 잘못되거나 없을때
                    val errorMessage = JSONObject(it.errorBody()?.string())
                    Log.e(TAG, errorMessage.getString("message"))

                    //400번대 에러로 투두리스트 데이터를 가져오는 것이 실패했을 경우, 사용자에게 알려준다.
                    Toast.makeText(this, "투두리스트 데이터를 가져오는 것을 실패했습니다.", Toast.LENGTH_SHORT).show()
                }
                it.code() == 404 -> {
                    //존재하지 않은 회원일 경우
                    val errorMessage = JSONObject(it.errorBody()?.string())
                    Log.e(TAG, errorMessage.getString("message"))

                    moveLogin()
                }
            }
        })

        viewModel.AddTodoListResponseLiveData.observe(this, androidx.lifecycle.Observer {
            when {
                it.isSuccessful -> {
                    //새로 추가한 투두리스트
                    val rv = findViewById<RecyclerView>(R.id.rv_todolist)

                    var todolistAdepter = TodoListAdapter(this, viewModel)

                    todolistAdepter.datas = it.body()!!.result.theDayTodo.toMutableList()

                    //네비게이션 드로어 오늘 할일 달성률 갱신
                    viewDataBinding.draworInfo!!.achieveTodoRate =
                        it.body()!!.result.achieveTodoRate
                    viewDataBinding.draworInfo = viewDataBinding.draworInfo

                    //추가된 새로 받아온 투두리스트로 DataBinding 정보 갱신
                    viewDataBinding.todolistResponse!!.result.theDayTodo =
                        todolistAdepter.datas.toList()
                    viewDataBinding.todolistResponse = viewDataBinding.todolistResponse

                    //리사이클러뷰 갱신
                    rv.adapter = todolistAdepter

                    //진행률 업데이트
                    viewDataBinding.todolistResponse?.result?.theDayAcheiveRate = it.body()!!.result.theDayAcheiveRate
                    viewDataBinding.todolistResponse = viewDataBinding.todolistResponse
                    viewDataBinding.todoListProgress.progress = it.body()!!.result.theDayAcheiveRate
                }
                it.code() == 400 -> {
                    //요청값을 제대로 다 전달하지 않은 경우 ex. 날짜 또는 요청타입 값이 잘못되거나 없을때
                    val errorMessage = JSONObject(it.errorBody()?.string())
                    Log.e(TAG, errorMessage.getString("message"))

                    //400번대 에러로 투두리스트 추가 실패했을 경우, 사용자에게 알려준다.
                    Toast.makeText(this, "투두리스트 추가하는 것을 실패했습니다.", Toast.LENGTH_SHORT).show()
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
                    viewDataBinding.draworInfo!!.achieveTodoRate =
                        it.body()!!.result.achieveTodoRate
                    viewDataBinding.draworInfo = viewDataBinding.draworInfo

                    //프로그래스바
                    viewDataBinding.todolistResponse?.result?.theDayAcheiveRate = it.body()!!.result.theDayAcheiveRate
                    viewDataBinding.todolistResponse = viewDataBinding.todolistResponse
                    viewDataBinding.todoListProgress.progress = it.body()!!.result.theDayAcheiveRate
                }
                it.code() == 400 -> {
                    //요청값을 제대로 다 전달하지 않은 경우 ex. 날짜 또는 요청타입 값이 잘못되거나 없을때
                    val errorMessage = JSONObject(it.errorBody()?.string())
                    Log.e(TAG, errorMessage.getString("message"))

                    //400번대 에러로 투두리스트 체크 실패했을 경우, 사용자에게 알려준다.
                    Toast.makeText(this, "투두리스트 체크에 실패했습니다. 나중 다시 시도해주세요.", Toast.LENGTH_SHORT)
                        .show()
                }
                it.code() == 404 -> {
                    //존재하지 않은 회원일 경우
                    val errorMessage = JSONObject(it.errorBody()?.string())
                    Log.e(TAG, errorMessage.getString("message"))

                    when (errorMessage.getInt("code")) {
                        -2 -> {
                            //존재하지 않는 회원인 경우
                            moveLogin()
                        }
                        -7 -> {
                            //수정하고자 하는 할 일이 실제로 존재하지 않은 경우
                            Toast.makeText(this, "해당 할일이 존재하지 않습니다.", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        })
        viewModel.DeleteTodoListResponseLiveData.observe(this, androidx.lifecycle.Observer {
            when {
                it.isSuccessful -> {
                    myStudyAdepter!!.notifyDataSetChanged()
                    viewDataBinding.todolistResponse!!.result.theDayTodo =
                        myStudyAdepter!!.datas.toList()
                    viewDataBinding.todolistResponse = viewDataBinding.todolistResponse

                    //달력 데코레이터 다시 세팅
                    viewDataBinding.calendarView.removeDecorators()
                    var decorators = ArrayList<CalendarDay>()
                    it.body()!!.result.todoDate.forEach {
                        val date = it.split("-")
                        decorators.add(
                            CalendarDay.from(
                                date[0].toInt(),
                                date[1].toInt(),
                                date[2].toInt()
                            )
                        )
                    }
                    viewDataBinding.calendarView.addDecorator(EventDecorator(decorators))

                    //네비게이션 드로어 오늘 할일 달성률 갱신
                    viewDataBinding.draworInfo!!.achieveTodoRate =
                        it.body()!!.result.achieveTodoRate
                    viewDataBinding.draworInfo = viewDataBinding.draworInfo

                    //프로그래스바
                    viewDataBinding.todolistResponse?.result?.theDayAcheiveRate = it.body()!!.result.theDayAcheiveRate
                    viewDataBinding.todolistResponse = viewDataBinding.todolistResponse
                    viewDataBinding.todoListProgress.progress = it.body()!!.result.theDayAcheiveRate
                }
                it.code() == 400 -> {
                    //요청값을 제대로 다 전달하지 않은 경우 ex. 날짜 또는 요청타입 값이 잘못되거나 없을때
                    val errorMessage = JSONObject(it.errorBody()?.string())
                    Log.e(TAG, errorMessage.getString("message"))

                    //400번대 에러로 투두리스트 삭제 실패했을 경우, 사용자에게 알려준다.
                    Toast.makeText(this, "투두리스트 삭제에 실패했습니다. 나중 다시 시도해주세요.", Toast.LENGTH_SHORT)
                        .show()
                }
                it.code() == 404 -> {
                    //존재하지 않은 회원일 경우
                    val errorMessage = JSONObject(it.errorBody()?.string())
                    Log.e(TAG, errorMessage.getString("message"))

                    when (errorMessage.getInt("code")) {
                        -2 -> {
                            //존재하지 않는 회원인 경우
                            moveLogin()
                        }
                        -7 -> {
                            //수정하고자 하는 할 일이 실제로 존재하지 않은 경우
                            Toast.makeText(this, "해당 할일이 존재하지 않습니다.", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        })
        viewModel.EditTodoListResponseLiveData.observe(this, androidx.lifecycle.Observer {
            when {
                it.isSuccessful -> {
                    myStudyAdepter!!.notifyDataSetChanged()
                }
                it.code() == 400 -> {
                    //요청값을 제대로 다 전달하지 않은 경우 ex. 날짜 또는 요청타입 값이 잘못되거나 없을때
                    val errorMessage = JSONObject(it.errorBody()?.string())
                    Log.e(TAG, errorMessage.getString("message"))

                    //400번대 에러로 수정이 실패했을 경우, 사용자에게 알려준다.
                    Toast.makeText(this, "투두리스트 입력을 다시 확인해주세요.", Toast.LENGTH_SHORT).show()
                }
                it.code() == 404 -> {
                    //존재하지 않은 회원일 경우
                    val errorMessage = JSONObject(it.errorBody()?.string())
                    Log.e(TAG, errorMessage.getString("message"))

                    when (errorMessage.getInt("code")) {
                        -2 -> {
                            //존재하지 않는 회원인 경우
                            moveLogin()
                        }
                        -7 -> {
                            //수정하고자 하는 할 일이 실제로 존재하지 않은 경우
                            Toast.makeText(this, "해당 할일이 존재하지 않습니다.", Toast.LENGTH_SHORT).show()
                        }
                    }

                }
            }
        })
    }

    override fun initAfterBinding() {
        viewModel.getTodoListData("start", getToday())
        viewDataBinding.isAddButton = true
    }

    fun init() {

        val calender = findViewById<MaterialCalendarView>(R.id.calendarView)
        calender.state().edit()
            .setFirstDayOfWeek(DayOfWeek.MONDAY)
            .setCalendarDisplayMode(CalendarMode.WEEKS)
            .commit();

        //캘린터 타이틀의 표기되는 이름을 설정하는 함수 ex) 2020년 02월
        calender.setTitleFormatter(TitleFormatter { day ->
            var cal = Calendar.getInstance()
            cal.set(day.year, day.month - 1, day.day)

            var simpleDateFormat = SimpleDateFormat("yyyy년 MM월")
            var result = simpleDateFormat.format(cal.time)

            return@TitleFormatter result
        })

        //캘린더의 월이 바뀔대마다 발생하는 이벤트
        calender.setOnMonthChangedListener(OnMonthChangedListener { widget, date ->
            val month = if (date.month < 10) "0${date.month}" else date.month.toString()
            val day = if (date.day < 10) "0${date.day}" else date.day.toString()

            val showDate = date.year.toString() + "-" + month + "-" + day
            viewModel.getTodoListData("getTodoDate", showDate)
        })

        //캘린더 다이얼로그(월간)의 이벤트
        val listener = DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
            var local = CalendarDay.from(year, month + 1, dayOfMonth)
            calender.setCurrentDate(local)
            calender.currentDate = local
            calender.clearSelection()
            calender.setDateSelected(
                CalendarDay.from(
                    year,
                    month + 1,
                    dayOfMonth
                ), true
            )

            var cal = Calendar.getInstance()
            cal.set(year, month, dayOfMonth)
            var simpleDateFormat = SimpleDateFormat("yyyy-MM-dd")
            var dates = simpleDateFormat.format(cal.time)

            selectData = dates
            viewDataBinding.selectDate = dateFormat.format(cal.time)
            viewDataBinding.isAddButton = compareDate(selectData, getToday())

            viewModel.getTodoListData("old", dates)
            viewModel.getTodoListData("getTodoDate", dates)
        }

        //캘린더의 2020년 03월 이라고 적힌 타이틀을 누르면 캘린더 다이얼로그(월간)이 나온다.
        calender.setOnTitleClickListener(View.OnClickListener {
            val today = selectData.split("-")
            var dialog = DatePickerDialog(
                this,
                listener,
                today[0].toInt(),
                today[1].toInt() - 1,
                today[2].toInt()
            )
            dialog.show()
        })

        //캘린더의 날짜를 선택했을 경우 발생하는 이벤트
        calender.setOnDateChangedListener(OnDateSelectedListener { widget, date, selected ->
            if (selected) {
                var cal = Calendar.getInstance()
                cal.set(date.year, date.month - 1, date.day)
                var simpleDateFormat = SimpleDateFormat("yyyy-MM-dd")
                var dates = simpleDateFormat.format(cal.time)

                selectData = dates
                viewDataBinding.selectDate = dateFormat.format(cal.time)
                viewDataBinding.isAddButton = compareDate(selectData, getToday())

                viewModel.getTodoListData("old", dates)
            }
        })

        //달력에 오늘의 날짜 기본으로 설정해놓기
        val todays = getToday().split("-")
        viewDataBinding.selectDate = dateFormat.format(System.currentTimeMillis())

        calender.setDateSelected(
            CalendarDay.from(
                todays[0].toInt(),
                todays[1].toInt(),
                todays[2].toInt()
            ), true
        )

        viewDataBinding.todoListFloatingbtnAddTodolist.setOnClickListener(View.OnClickListener {
            val mDialogView = LayoutInflater.from(this).inflate(R.layout.dialog_todo_list_add, null)
            val mBuilder = AlertDialog.Builder(this).setView(mDialogView)

            val builder = mBuilder.show()

            // Custom Dialog 배경 설정 (다음과 같이 진행해야 좌우 여백 없이 그려짐)
            builder.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            val txt_calendar = mDialogView.findViewById<TextInputLayout>(R.id.dialog_todolist_edt__txt_day)
            val txt_todo = mDialogView.findViewById<TextInputLayout>(R.id.dialog_todolist_edt_edit)
            val btn_cancle =
                mDialogView.findViewById<Button>(R.id.dialog_todolist_edt_btn_cancle)
            val btn_add = mDialogView.findViewById<Button>(R.id.dialog_todolist_edt_btn_remove)

            if (selectData.equals(getToday())) {
                txt_calendar.editText?.setText("오늘")
            } else {
                txt_calendar.editText?.setText(selectData)
            }

            btn_cancle.setOnClickListener(View.OnClickListener {
                builder.dismiss()
            })

            btn_add.setOnClickListener(View.OnClickListener {
                viewModel.setAddTodoListData(selectData, txt_todo.editText?.text.toString())
                builder.dismiss()
            })
        })

    }

    fun rv_init() {
        val rv = findViewById<RecyclerView>(R.id.rv_todolist)

        myStudyAdepter = TodoListAdapter(this, viewModel)
        myStudyAdepter!!.datas =
            viewDataBinding.todolistResponse!!.result.theDayTodo.toMutableList()
        if (myStudyAdepter!!.datas.size > 0) {
            val notext = findViewById<TextView>(R.id.todo_list_txt_no_todolist)
            notext.visibility = View.GONE
        }

        myStudyAdepter!!.setItemClickListener(object : TodoListAdapter.OnItemClickListener {
            override fun onClick(v: View, position: Int) {
                Toast.makeText(this@TodoListActivity, "dld", Toast.LENGTH_SHORT).show()
            }
        })

        rv.adapter = myStudyAdepter
    }

    private fun getToday(): String {
        val now = System.currentTimeMillis()
        return SimpleDateFormat("yyyy-MM-dd", Locale.KOREAN).format(now)
    }

    private fun compareDate(selectDay: String, today: String): Boolean {
        var simpleDateFormat = SimpleDateFormat("yyyy-MM-dd")
        val compareSelectDay = simpleDateFormat.parse(selectDay).time
        val compareToday = simpleDateFormat.parse(today).time

        return (compareSelectDay - compareToday) >= 0
    }
}