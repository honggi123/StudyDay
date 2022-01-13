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

    lateinit var todolistResponse: TodolistResponse

    var myStudyAdepter: TodoListAdapter? = null

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
                todolistResponse = it.body()!!
                Log.d(TAG,todolistResponse.toString())
                
                //네비게이션 바에 넣을 정보가 있을 경우
                if(todolistResponse.result.profile != null) {
                    setNavigaionLoginImage(todolistResponse.result.profile.loginType)
                    setNavigaionProfileImage(todolistResponse.result.profile.img)
                    setNavigaionNickname(todolistResponse.result.profile.nickname)
                }

                //프로그레스바, 투두리스트 항목이 있을 경우
                if(todolistResponse.result.theDayTodo != null){
                    rv_init()
                    progress_init()
                }

                //데코레이터 추가할 날짜가 있을 경우
                if(todolistResponse.result.todoDate != null) {
                    var decorators = ArrayList<CalendarDay>()
                    todolistResponse.result.todoDate.forEach {
                        val date = it.split("-")
                        decorators.add(CalendarDay.from(date[0].toInt(), date[1].toInt(), date[2].toInt()))
                    }
                    viewDataBinding.calendarView.addDecorator(EventDecorator(decorators))
                }

            }
        })

        viewModel.AddTodoListResponseLiveData.observe(this, androidx.lifecycle.Observer {
            if (it.isSuccessful) {
                val day = it.body()!!.result.theDayTodo[0]

                val rv = findViewById<RecyclerView>(R.id.rv_todolist)

                var myStudyAdepter: TodoListAdapter = TodoListAdapter(this, viewModel)

                myStudyAdepter.datas = todolistResponse.result.theDayTodo.toMutableList()
                myStudyAdepter.datas.add(
                    TodolistResponse.Result.TheDayTodo(
                        day.todoDate,
                        day.idx,
                        day.isComplete,
                        day.todo
                    )
                )

                todolistResponse.result.theDayTodo = myStudyAdepter.datas.toList()
                rv.adapter = myStudyAdepter

                val todoProgress = findViewById<ProgressBar>(R.id.todo_list_progress)

                todoProgress.progress = it.body()!!.result.theDayAcheiveRate
            }
        })

        viewModel.CheckTodoListResponseLiveData.observe(this, androidx.lifecycle.Observer {
            if (it.isSuccessful) {
                val todoProgress = findViewById<ProgressBar>(R.id.todo_list_progress)

                todoProgress.progress = it.body()!!.result.theDayAcheiveRate
            }
        })
        viewModel.DeleteTodoListResponseLiveData.observe(this, androidx.lifecycle.Observer {
            if (it.isSuccessful) {
                myStudyAdepter!!.notifyDataSetChanged()
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
    }

    fun init() {

        val calender = findViewById<MaterialCalendarView>(R.id.calendarView)
        calender.state().edit()
//            .setFirstDayOfWeek(Calendar.MONDAY)
            .setCalendarDisplayMode(CalendarMode.WEEKS)
            .commit();

        calender.setTitleFormatter(TitleFormatter { day ->
            var cal = Calendar.getInstance()
            cal.set(day.year, day.month-1, day.day)

            var simpleDateFormat = SimpleDateFormat("yyyy년 MM월")
            var result = simpleDateFormat.format(cal.time)

            return@TitleFormatter result
        })

        calender.setOnMonthChangedListener(OnMonthChangedListener { widget, date ->
            val month = if(date.month<10)"0${date.month}" else date.month.toString()
            val day = if(date.day<10)"0${date.day}" else date.day.toString()

            val showDate = date.year.toString()+"-"+month+"-"+day
            Log.d("먼슬리 데이트 이벤트 테스트",showDate)
            viewModel.getTodoListData("getTodoDate",showDate)
        })

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
        }

        calender.setOnTitleClickListener(View.OnClickListener {
            var dialog = DatePickerDialog(this, listener, 2022, 0, 2)
            dialog.show()
        })

        calender.setOnDateChangedListener(OnDateSelectedListener { widget, date, selected ->
            if (selected) {
                var cal = Calendar.getInstance()
                cal.set(date.year, date.month-1, date.day)
                var simpleDateFormat = SimpleDateFormat("yyyy-MM-dd")
                var dates = simpleDateFormat.format(cal.time)
                viewModel.getTodoListData("old", dates)
            }
        })

        val todays = getToday().split("-")

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

            edt_todolist.requestFocus()
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

            imm.toggleSoftInput(
                InputMethodManager.SHOW_FORCED,
                InputMethodManager.HIDE_IMPLICIT_ONLY
            )


            btn_add.setOnClickListener(View.OnClickListener {
                viewModel.setAddTodoListData(getToday(), edt_todolist.text.toString())
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
        myStudyAdepter!!.datas = todolistResponse.result.theDayTodo.toMutableList()
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

    fun progress_init() {
        val todoProgress = findViewById<ProgressBar>(R.id.todo_list_progress)

        todoProgress.progress = todolistResponse.result.theDayAcheiveRate
    }

    fun getToday(): String {
        val now = System.currentTimeMillis()
        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.KOREAN).format(now)

        return simpleDateFormat
    }
}