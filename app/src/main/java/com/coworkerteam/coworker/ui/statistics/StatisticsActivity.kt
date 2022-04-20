package com.coworkerteam.coworker.ui.statistics

import android.graphics.Color
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.coworkerteam.coworker.R
import com.coworkerteam.coworker.data.model.api.StatisticsResponse
import com.coworkerteam.coworker.data.model.custom.CustBarChart
import com.coworkerteam.coworker.data.model.custom.StatisticsMarkerView
import com.coworkerteam.coworker.data.model.other.DrawerBottomInfo
import com.coworkerteam.coworker.databinding.ActivityStatisticsBinding
import com.coworkerteam.coworker.ui.base.NavigationActivity
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import com.github.mikephil.charting.utils.ColorTemplate
import com.github.sundeepk.compactcalendarview.CompactCalendarView.CompactCalendarViewListener
import com.google.android.material.appbar.AppBarLayout
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import com.github.mikephil.charting.formatter.PercentFormatter
import com.google.android.material.navigation.NavigationView
import org.json.JSONObject
import java.text.DecimalFormat


class StatisticsActivity : NavigationActivity<ActivityStatisticsBinding, StatisticsViewModel>() {

    var TAG = "StatisticsActivity"
    override val layoutResourceID: Int
        get() = R.layout.activity_statistics
    override val viewModel: StatisticsViewModel by viewModel()

    override val drawerLayout: DrawerLayout
        get() = findViewById(R.id.drawer_layout)
    override val navigatinView: NavigationView
        get() = findViewById(R.id.navigationView)

    lateinit var appBarLayout: AppBarLayout
    lateinit var statisticsResponse: StatisticsResponse
    var maxstudytime = 0f
    var period = "week"
    var selectDate = getToday()

    val MATERIAL_COLORS = intArrayOf(
        ColorTemplate.rgb("#FF6384"),
        ColorTemplate.rgb("#FFC43A"),
    )
    val MATERIAL_WHITE_COLORS = intArrayOf(
        ColorTemplate.rgb("#FFFFFF"),
        ColorTemplate.rgb("#FFFFFF"),
    )

    private val dateFormat: SimpleDateFormat =
        SimpleDateFormat("yyyy.MM.dd",  /*Locale.getDefault()*/Locale.KOREA)
    private val apiDateFormat: SimpleDateFormat =
        SimpleDateFormat("yyyy-MM-dd",  /*Locale.getDefault()*/Locale.KOREA)

    private var shouldShow = true

    val pieEntries = ArrayList<PieEntry>()
    val barEntries = ArrayList<BarEntry>()
    val markerStrings = ArrayList<String>()

    override fun initStartView() {
        super.initStartView()

        viewDataBinding.activitiy = this

        appBarLayout = findViewById(R.id.app_bar_layout)

        var main_toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)

        setSupportActionBar(main_toolbar) // 툴바를 액티비티의 앱바로 지정
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // 드로어를 꺼낼 홈 버튼 활성화
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_baseline_menu_24_write) // 홈버튼 이미지 변경

        //시간을 한국 기준으로 설정
        viewDataBinding.compactcalendarView.setLocale(
            TimeZone.getDefault(),  /*Locale.getDefault()*/
            Locale.KOREA
        )

        viewDataBinding.compactcalendarView.setShouldDrawDaysHeader(true)
        viewDataBinding.compactcalendarView.shouldSelectFirstDayOfMonthOnScroll(false)

        viewDataBinding.compactcalendarView.setListener(object : CompactCalendarViewListener {
            override fun onDayClick(dateClicked: Date?) {
                setSubtitle(dateFormat.format(dateClicked))

                selectDate = apiDateFormat.format(dateClicked)
                viewModel.getStatisticsData("old", selectDate, period)
                setRangeTime(selectDate)

                firebaseLog.addLog(TAG,"select_date")
            }

            override fun onMonthScroll(firstDayOfNewMonth: Date?) {
                setSubtitle(dateFormat.format(firstDayOfNewMonth))
                setCurrentDate(firstDayOfNewMonth!!)
                selectDate = apiDateFormat.format(firstDayOfNewMonth)
                viewModel.getStatisticsData("old", selectDate, period)
                setRangeTime(selectDate)

                firebaseLog.addLog(TAG,"scroll_month")
            }
        })
        viewDataBinding.compactcalendarView.hideCalendar()

        // Set current date to today
        setCurrentDate(Date())

        val arrow: ImageView = findViewById(R.id.date_picker_arrow)

        val datePickerButton = findViewById<LinearLayout>(R.id.date_picker_button)

        datePickerButton.setOnClickListener { v: View? ->
            val rotation: Float = if (!shouldShow) 0F else 180.toFloat()
            ViewCompat.animate(arrow).rotation(rotation).start()
            appBarLayout.setExpanded(!shouldShow, true)
            calendarEven()
        }

        init()
        setRangeTime(getToday())
    }

    override fun initDataBinding() {
        viewModel.StatisticsResponseLiveData.observe(this, androidx.lifecycle.Observer {
//            카테고리가 성공적으로 선택
            when {
                it.isSuccessful -> {
                    statisticsResponse = it.body()!!
                    viewDataBinding.statisticsResponse = it.body()!!

                    if (statisticsResponse.profile != null) {
                        setNavigaionLoginImage(statisticsResponse.profile.loginType)
                        setNavigaionProfileImage(statisticsResponse.profile.img)
                        setNavigaionNickname(statisticsResponse.profile.nickname)

                        viewDataBinding.draworInfo = DrawerBottomInfo(
                            it.body()!!.achieveTimeRate,
                            it.body()!!.achieveTodoRate,
                            it.body()!!.dream.dday,
                            it.body()!!.dream.ddayName
                        )
                    }

                    var pro_studyTime =
                        if (it.body()!!.theDayAcheiveTimeRate == null) 0 else it.body()!!.theDayAcheiveTimeRate
                    var pro_plan =
                        if (it.body()!!.theDayAcheiveRate == null) 0 else it.body()!!.theDayAcheiveRate

                    viewDataBinding.statisticsProgressTodayStudyTime.progress = pro_studyTime!!
                    viewDataBinding.statisticsProgressPlan.progress = pro_plan!!

                    val studyTime = statisticsResponse.studyRate ?: 0
                    val restTime = statisticsResponse.restRate ?: 0

                    pieEntries.clear()
                    pieEntries.add(PieEntry(studyTime.toFloat(),""))
                    if(restTime.toFloat() > 0.0f){
                        pieEntries.add(PieEntry(restTime.toFloat(),""))
                    }
                    pieChart()

                    barEntries.clear()
                    markerStrings.clear()

                    if (statisticsResponse.weekTimeAcheive != null) {
                        viewDataBinding.timeAVG =
                            it.body()!!.weekTimeAVG.hour + "시간 " + it.body()!!.weekTimeAVG.min + "분"
                        viewDataBinding.todoAVG = if(it.body()!!.weekTodoAVG==null) "없음" else it.body()!!.weekTodoAVG+"%"

                        Log.d(TAG, statisticsResponse.weekTimeAcheive.size.toString())
                        var x = 0
                        statisticsResponse.weekTimeAcheive.forEach { i ->
                            var hour = if (i.hour != null) i.hour else 0

                            barEntries.add(BarEntry(x.toFloat(), hour.toFloat()))
                            if(maxstudytime < hour.toFloat()){
                                maxstudytime = hour.toFloat()
                            }

                            val statistcsDay = statisticsResponse.weekTimeAcheive.get(x).date
                            Log.d(TAG, "weekTimeAcheive 사이즈 : "+ x.toString())
                            val studyTime =
                                if (statisticsResponse.weekTimeAcheive.get(x).time == null) "없음" else statisticsResponse.weekTimeAcheive.get(
                                    x
                                ).time
                            val timeAcheive = i.timeRate ?: "없음"
                            val todoAcheive =
                                statisticsResponse.weekTodoAcheive.get(x).acheiveRate ?: "없음"
                            val text =
                                "$statistcsDay \n공부시간 : $studyTime \n공부시간 달성률 : $timeAcheive \n계획 달성률 : $todoAcheive"
                            markerStrings.add(text)

                            x++

                            Log.d(TAG, x.toString())
                        }
                        barChart()
                    } else if (statisticsResponse.monthTimeAcheive != null) {
                        viewDataBinding.timeAVG =
                            it.body()!!.monthTimeAVG.hour + "시간 " + it.body()!!.monthTimeAVG.min + "분"
                        viewDataBinding.todoAVG = if(it.body()!!.monthTodoAVG==null) "없음" else it.body()!!.monthTodoAVG+"%"

                        var x = 0
                        Log.d(TAG, statisticsResponse.monthTimeAcheive.size.toString())
                        statisticsResponse.monthTimeAcheive.forEach { i ->
                            var hour = if (i.hour != null) i.hour else 0

                            barEntries.add(BarEntry(x.toFloat(), hour.toFloat()))

                            val statistcsDay = statisticsResponse.monthTimeAcheive.get(x).date
                            val studyTime =
                                if (statisticsResponse.monthTimeAcheive.get(x).time == null) "없음" else statisticsResponse.monthTimeAcheive.get(
                                    x
                                ).time
                            val timeAcheive = i.timeRate ?: "없음"
                            val todoAcheive =
                                statisticsResponse.monthTodoAcheive.get(x).acheiveRate ?: "없음"
                            val text =
                                statistcsDay + " \n" + "공부시간 : " + studyTime + " \n" + "공부시간 달성률 : " + timeAcheive + " \n" + "계획 달성률 : " + todoAcheive
                            markerStrings.add(text)

                            x++

                            Log.d(TAG, x.toString())
                        }
                        barChart()
                    }
                }
                it.code() == 400 -> {
                    //요청값을 제대로 다 전달하지 않은 경우 ex. 날짜 또는 요청타입 값이 잘못되거나 없을때
                    val errorMessage = JSONObject(it.errorBody()?.string())
                    Log.e(TAG, errorMessage.getString("message"))

                    //400번대 에러로 통계페이지 데이터 get 실패했을 경우, 사용자에게 알려준다.
                    Toast.makeText(this,"통계 데이터를 가져오는 것을 실패했습니다.", Toast.LENGTH_SHORT).show()
                }
                it.code() == 404 -> {
                    //존재하지 않은 회원일 경우
                    val errorMessage = JSONObject(it.errorBody()?.string())
                    Log.e(TAG, errorMessage.getString("message"))

                    moveLogin()
                }
            }
        })
        checkdate()
    }

    override fun initAfterBinding() {
        viewModel.getStatisticsData("start", getToday(), period)
    }

    override fun onRestart() {
        super.onRestart()
        viewModel.getStatisticsData("start", selectDate, period)
    }

    fun init() {
        val sortWeekly = findViewById<TextView>(R.id.statistics_txt_weekly)
        sortWeekly.setSelected(true)
    }

    fun setRangeTime(today: String) {
        val dateRange = findViewById<TextView>(R.id.statistics_txt_weekly_times)
        dateRange.text = dateRangeComputer(today)
    }

    fun getToday(): String {
        val now = System.currentTimeMillis()
        return SimpleDateFormat("yyyy-MM-dd", Locale.KOREAN).format(now)
    }

    fun calendarEven() {
        if (!viewDataBinding.compactcalendarView.isAnimating) {
            if (shouldShow) {
                viewDataBinding.compactcalendarView.visibility = View.VISIBLE
                viewDataBinding.compactcalendarView.showCalendar()
            } else {
                viewDataBinding.compactcalendarView.hideCalendar()
            }
            shouldShow = !shouldShow
        }
    }

    fun pieChart() {
        val piechart = findViewById<PieChart>(R.id.statistics_piechart)

        piechart.run {
            setUsePercentValues(true)
        }

        var set = PieDataSet(pieEntries, "") // 데이터셋 초기화

        set.colors = MATERIAL_COLORS.toMutableList() // 바 그래프 색 설정

        val formatter = PercentFormatter(piechart)
        formatter.mFormat = DecimalFormat("###")

        val data = PieData(set)
        data.setValueTextSize(15f)
        data.setValueFormatter(formatter)
        data.setValueTextColors(MATERIAL_WHITE_COLORS.toMutableList())
        piechart.run {
            this.data = data //차트의 데이터를 data로 설정해줌.
            description.text = ""
            setUsePercentValues(true)
            invalidate()
        }
        piechart.legend.isEnabled = false


    }

    fun barChart() {
        val barchart = findViewById<CustBarChart>(R.id.statistics_barChart)
        barchart.run {
            description.isEnabled = false // 차트 옆에 별도로 표기되는 description을 안보이게 설정 (false)
            setMaxVisibleValueCount(7) // 최대 보이는 그래프 개수를 7개로 지정
            setPinchZoom(false) // 핀치줌(두손가락으로 줌인 줌 아웃하는것) 설정
            setDrawBarShadow(false) //그래프의 그림자
            setDrawGridBackground(false)//격자구조 넣을건지
            axisLeft.run { //왼쪽 축. 즉 Y방향 축을 뜻한다.
                axisMaximum = maxstudytime + maxstudytime/2  //100 위치에 선을 그리기 위해 101f로 맥시멈값 설정
                axisMinimum = 0f // 최소값 0
                granularity = 1f // 50 단위마다 선을 그리려고 설정.
                setDrawLabels(true) // 값 적는거 허용 (0, 50, 100)
                setDrawGridLines(false) //격자 라인 활용
                setDrawAxisLine(false) // 축 그리기 설정
                axisLineColor = ContextCompat.getColor(
                    context,
                    R.color.design_default_color_secondary_variant
                ) // 축 색깔 설정
                gridColor = ContextCompat.getColor(
                    context,
                    R.color.design_default_color_on_secondary
                ) // 축 아닌 격자 색깔 설정
                textColor = ContextCompat.getColor(
                    context,
                    R.color.black
                ) // 라벨 텍스트 컬러 설정
                textSize = 13f //라벨 텍스트 크기
            }
            xAxis.run {
                position = XAxis.XAxisPosition.BOTTOM //X축을 아래에다가 둔다.
                granularity = 1f // 1 단위만큼 간격 두기
                setDrawAxisLine(false) // 축 그림
                setDrawGridLines(false) // 격자
                textColor = ContextCompat.getColor(
                    context,
                    R.color.black
                ) //라벨 색상
                textSize = 12f // 텍스트 크기
//                valueFormatter = MyXAxisFormatter() // X축 라벨값(밑에 표시되는 글자) 바꿔주기 위해 설정
            }
            axisRight.isEnabled = false // 오른쪽 Y축을 안보이게 해줌.
            setTouchEnabled(false) // 그래프 터치해도 아무 변화없게 막음
            animateY(1000) // 밑에서부터 올라오는 애니매이션 적용
            legend.isEnabled = false //차트 범례 설정
        }

        if (barEntries.size < 8) {
            Log.d("디버그태그", "8보다 작음")
            barchart.xAxis.isEnabled = true
            barchart.xAxis.valueFormatter = MyXAxisFormatter()

        } else {
            Log.d("디버그태그", "8보다 큼")
            barchart.xAxis.isEnabled = false
        }

        var set = BarDataSet(barEntries, "DataSet") // 데이터셋 초기화
        set.colors = MATERIAL_COLORS.toMutableList()// 바 그래프 색 설정

        val dataSet: ArrayList<IBarDataSet> = ArrayList()
        dataSet.add(set)
        val data = BarData(dataSet)
        data.barWidth = 0.7f //막대 너비 설정
        val barMarker = StatisticsMarkerView(this, R.layout.statistics_custom_marker, markerStrings)
        barchart.run {
            this.data = data //차트의 데이터를 data로 설정해줌.
            setFitBars(true)
            invalidate()
            setTouchEnabled(true)
            setScaleEnabled(false)
        }
        barMarker.chartView = barchart
        barchart.marker = barMarker
    }

    inner class MyXAxisFormatter : ValueFormatter() {
        private val days = arrayOf("월", "화", "수", "목", "금", "토", "일")
        override fun getAxisLabel(value: Float, axis: AxisBase?): String {
            return days.getOrNull(value.toInt()) ?: value.toString()
        }
    }

    //TextView 글자색 변경
    fun setTextColor(text: String, start: Int, end: Int): SpannableStringBuilder {
        val builder = SpannableStringBuilder(text)
        val colorBlueSpan = ForegroundColorSpan(Color.BLUE)
        builder.setSpan(colorBlueSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        return builder
    }

    fun changDay(v: View) {
        val view = v as TextView
        val name = view.text
        Log.d(TAG, "먼슬리/위클리" + name)

        if (name.equals("주간")) {
            period = "week"
            view.isSelected = true
            viewDataBinding.statisticsMenthly.isSelected = false
            viewModel.getStatisticsData("old", selectDate, period)
            setRangeTime(selectDate)
            firebaseLog.addLog(TAG,"show_weekly")
        } else if (name.equals("월간")) {
            period = "month"
            view.isSelected = true
            viewDataBinding.statisticsTxtWeekly.isSelected = false
            viewModel.getStatisticsData("old", selectDate, period)
            setRangeTime(selectDate)

            firebaseLog.addLog(TAG,"show_monthly")
        }
    }

    fun dateRangeComputer(today: String): String {
        var todays = today.split("-")
        var cal = Calendar.getInstance()

        cal.set(
            Integer.parseInt(todays[0]),
            (Integer.parseInt(todays[1]) - 1),
            Integer.parseInt(todays[2])
        );

        cal.firstDayOfWeek = Calendar.MONDAY


        var dayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - cal.firstDayOfWeek
        Log.d(TAG,"dayOfWeek : " + dayOfWeek)

        if (period.equals("month")) {
            cal.set(Calendar.DAY_OF_MONTH, 1)
            val firstDay = dateFormat.format(cal.time)
            cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
            val lastDay = dateFormat.format(cal.time)
            return firstDay + " ~ " + lastDay
        } else {
            if(dayOfWeek == -1){
                dayOfWeek = 6
            }
            cal.add(Calendar.DAY_OF_MONTH, -dayOfWeek)
            val firstDay = dateFormat.format(cal.time)
            cal.add(Calendar.DAY_OF_MONTH, 6);

            val lastDay = dateFormat.format(cal.time)
            return firstDay + " ~ " + lastDay
        }
    }


    private fun setCurrentDate(date: Date) {
        setSubtitle(dateFormat.format(date))
        if (viewDataBinding.compactcalendarView != null) {
            viewDataBinding.compactcalendarView!!.setCurrentDate(date)
        }
    }

    private fun setSubtitle(subtitle: String) {
        val datePickerTextView = findViewById<TextView>(R.id.date_picker_text_view)
        if (datePickerTextView != null) {
            datePickerTextView.text = subtitle
        }
    }

}