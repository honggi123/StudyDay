package com.coworkerteam.coworker.ui.todolist

import android.app.DatePickerDialog
import android.widget.Toast

import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
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
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.CalendarMode
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener
import com.prolificinteractive.materialcalendarview.OnMonthChangedListener
import com.prolificinteractive.materialcalendarview.format.TitleFormatter
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
            if (it.isSuccessful) {

                //데코레이터용 날짜 받아올때는 DataBinding에 넣지 않음
                if(!it.body()!!.message.equals("선택한 달에서, 할 일이 있는 날짜입니다.")){
                    viewDataBinding.todolistResponse = it.body()!!
                }

                //네비게이션 바에 넣을 정보가 있을 경우
                if(it.body()!!.result.profile != null) {
                    setNavigaionLoginImage(viewDataBinding.todolistResponse!!.result.profile.loginType)
                    setNavigaionProfileImage(viewDataBinding.todolistResponse!!.result.profile.img)
                    setNavigaionNickname(viewDataBinding.todolistResponse!!.result.profile.nickname)

                    viewDataBinding.draworInfo = DrawerBottomInfo(it.body()!!.result.achieveTimeRate,it.body()!!.result.achieveTodoRate,it.body()!!.result.dream.dday,it.body()!!.result.dream.ddayName)
                }

                //프로그레스바, 투두리스트 항목이 있을 경우
                if(it.body()!!.result.theDayTodo != null){
                    rv_init()
                    progress_init()
                }

                //데코레이터 추가할 날짜가 있을 경우
                if(it.body()!!.result.todoDate != null) {
                    var decorators = ArrayList<CalendarDay>()
                    it.body()!!.result.todoDate.forEach {
                        val date = it.split("-")
                        decorators.add(CalendarDay.from(date[0].toInt(), date[1].toInt(), date[2].toInt()))
                    }
                    viewDataBinding.calendarView.addDecorator(EventDecorator(decorators))
                }

            }
        })

        viewModel.AddTodoListResponseLiveData.observe(this, androidx.lifecycle.Observer {
            if (it.isSuccessful) {
                //새로 추가한 투두리스트
                val day = it.body()!!.result.theDayTodo[0]

                val rv = findViewById<RecyclerView>(R.id.rv_todolist)

                var myStudyAdepter: TodoListAdapter = TodoListAdapter(this, viewModel)

                myStudyAdepter.datas = viewDataBinding.todolistResponse!!.result.theDayTodo.toMutableList()
                myStudyAdepter.datas.add(
                    TodolistResponse.Result.TheDayTodo(
                        day.todoDate,
                        day.idx,
                        day.isComplete,
                        day.todo
                    )
                )
                //네비게이션 드로어 오늘 할일 달성률 갱신
                viewDataBinding.draworInfo!!.achieveTodoRate = it.body()!!.result.achieveTodoRate
                viewDataBinding.draworInfo = viewDataBinding.draworInfo

                //추가된 새로 받아온 투두리스트로 DataBinding 정보 갱신
                viewDataBinding.todolistResponse!!.result.theDayTodo = myStudyAdepter.datas.toList()
                viewDataBinding.todolistResponse = viewDataBinding.todolistResponse
                
                //리사이클러뷰 갱신
                rv.adapter = myStudyAdepter

                val todoProgress = findViewById<ProgressBar>(R.id.todo_list_progress)

                todoProgress.progress = it.body()!!.result.theDayAcheiveRate
            }
        })

        viewModel.CheckTodoListResponseLiveData.observe(this, androidx.lifecycle.Observer {
            if (it.isSuccessful) {
                //네비게이션 드로어 오늘 할일 달성률 갱신
                viewDataBinding.draworInfo!!.achieveTodoRate = it.body()!!.result.achieveTodoRate
                viewDataBinding.draworInfo = viewDataBinding.draworInfo
            }
        })
        viewModel.DeleteTodoListResponseLiveData.observe(this, androidx.lifecycle.Observer {
            if (it.isSuccessful) {
                myStudyAdepter!!.notifyDataSetChanged()
                viewDataBinding.todolistResponse!!.result.theDayTodo = myStudyAdepter!!.datas.toList()
                viewDataBinding.todolistResponse = viewDataBinding.todolistResponse

                //달력 데코레이터 다시 세팅
                viewDataBinding.calendarView.removeDecorators()
                var decorators = ArrayList<CalendarDay>()
                it.body()!!.result.todoDate.forEach {
                    val date = it.split("-")
                    decorators.add(CalendarDay.from(date[0].toInt(), date[1].toInt(), date[2].toInt()))
                }
                viewDataBinding.calendarView.addDecorator(EventDecorator(decorators))

                //네비게이션 드로어 오늘 할일 달성률 갱신
                viewDataBinding.draworInfo!!.achieveTodoRate = it.body()!!.result.achieveTodoRate
                viewDataBinding.draworInfo = viewDataBinding.draworInfo
            }
        })
        viewModel.EditTodoListResponseLiveData.observe(this, androidx.lifecycle.Observer {
            if (it.isSuccessful) {
                myStudyAdepter!!.notifyDataSetChanged()
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
            cal.set(day.year, day.month-1, day.day)

            var simpleDateFormat = SimpleDateFormat("yyyy년 MM월")
            var result = simpleDateFormat.format(cal.time)

            return@TitleFormatter result
        })

        //캘린더의 월이 바뀔대마다 발생하는 이벤트
        calender.setOnMonthChangedListener(OnMonthChangedListener { widget, date ->
            val month = if(date.month<10)"0${date.month}" else date.month.toString()
            val day = if(date.day<10)"0${date.day}" else date.day.toString()

            val showDate = date.year.toString()+"-"+month+"-"+day
            viewModel.getTodoListData("getTodoDate",showDate)
        })

        //캘린더 다이얼로그(월간)의 이벤트
        val listener = DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
            var local = CalendarDay.from(year,month+1,dayOfMonth)
            calender.setCurrentDate(local)
            calender.currentDate = local
            calender.clearSelection()
            calender.setDateSelected(
                CalendarDay.from(
                    year,
                    month+1,
                    dayOfMonth
                ), true
            )

            var cal = Calendar.getInstance()
            cal.set(year, month, dayOfMonth)
            var simpleDateFormat = SimpleDateFormat("yyyy-MM-dd")
            var dates = simpleDateFormat.format(cal.time)

            selectData = dates
            viewDataBinding.selectDate = dateFormat.format(cal.time)
            viewDataBinding.isAddButton = compareDate(selectData,getToday())

            viewModel.getTodoListData("old", dates)
            viewModel.getTodoListData("getTodoDate",dates)
        }

        //캘린더의 2020년 03월 이라고 적힌 타이틀을 누르면 캘린더 다이얼로그(월간)이 나온다.
        calender.setOnTitleClickListener(View.OnClickListener {
            val today = selectData.split("-")
            var dialog = DatePickerDialog(this, listener, today[0].toInt(),  today[1].toInt()-1,  today[2].toInt())
            dialog.show()
        })

        //캘린더의 날짜를 선택했을 경우 발생하는 이벤트
        calender.setOnDateChangedListener(OnDateSelectedListener { widget, date, selected ->
            if (selected) {
                var cal = Calendar.getInstance()
                cal.set(date.year, date.month-1, date.day)
                var simpleDateFormat = SimpleDateFormat("yyyy-MM-dd")
                var dates = simpleDateFormat.format(cal.time)

                selectData = dates
                viewDataBinding.selectDate = dateFormat.format(cal.time)
                viewDataBinding.isAddButton = compareDate(selectData,getToday())

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

        val btn_addTodolist =
            findViewById<FloatingActionButton>(R.id.todo_list_floatingbtn_add_todolist)

        btn_addTodolist.setOnClickListener(View.OnClickListener {
            val mDialogView = LayoutInflater.from(this).inflate(R.layout.dialog_todo_list, null)
            val mBuilder = AlertDialog.Builder(this).setView(mDialogView)

            val builder = mBuilder.show()

            // Custom Dialog 크기 설정
            builder.window?.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )

            // Custom Dialog 위치 조절
            builder.window?.setGravity(Gravity.BOTTOM)
            // Custom Dialog 배경 설정 (다음과 같이 진행해야 좌우 여백 없이 그려짐)
            builder.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            val txt_calendar = mDialogView.findViewById<TextView>(R.id.dialog_todo_list_txt_add_day)
            val edt_todolist =
                mDialogView.findViewById<EditText>(R.id.dialog_todo_list_edt_todolist)
            val btn_add = mDialogView.findViewById<ImageView>(R.id.dialog_todo_list_add)

            if(selectData.equals(getToday())){
                txt_calendar.setText("오늘")
            }else{
                txt_calendar.setText(selectData)
            }

            edt_todolist.requestFocus()
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

            imm.toggleSoftInput(
                InputMethodManager.SHOW_FORCED,
                InputMethodManager.HIDE_IMPLICIT_ONLY
            )


            btn_add.setOnClickListener(View.OnClickListener {
                viewModel.setAddTodoListData(selectData, edt_todolist.text.toString())
                builder.dismiss()
            })

            builder.setOnDismissListener(DialogInterface.OnDismissListener {
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

                imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0)
            })
        })

    }

    fun rv_init() {
        val rv = findViewById<RecyclerView>(R.id.rv_todolist)

        myStudyAdepter = TodoListAdapter(this, viewModel)
        myStudyAdepter!!.datas = viewDataBinding.todolistResponse!!.result.theDayTodo.toMutableList()
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

    private fun progress_init() {
        val todoProgress = findViewById<ProgressBar>(R.id.todo_list_progress)

        todoProgress.progress = viewDataBinding.todolistResponse!!.result.theDayAcheiveRate
    }

    private fun getToday(): String {
        val now = System.currentTimeMillis()
        return SimpleDateFormat("yyyy-MM-dd", Locale.KOREAN).format(now)
    }

    private fun compareDate(selectDay:String, today:String): Boolean {
        var simpleDateFormat = SimpleDateFormat("yyyy-MM-dd")
        val compareSelectDay = simpleDateFormat.parse(selectDay).time
        val compareToday = simpleDateFormat.parse(today).time

        return (compareSelectDay - compareToday) >= 0
    }
}