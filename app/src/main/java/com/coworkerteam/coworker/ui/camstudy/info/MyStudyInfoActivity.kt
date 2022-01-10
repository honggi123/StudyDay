package com.coworkerteam.coworker.ui.camstudy.info

import android.view.View
import android.widget.TextView
import com.coworkerteam.coworker.R
import com.coworkerteam.coworker.data.local.prefs.AppPreferencesHelper
import com.coworkerteam.coworker.data.model.other.MyStudyInfo
import com.coworkerteam.coworker.databinding.ActivityMyStudyInfoBinding
import com.coworkerteam.coworker.ui.base.BaseActivity
import org.koin.androidx.viewmodel.ext.android.viewModel

class MyStudyInfoActivity : BaseActivity<ActivityMyStudyInfoBinding, MyStudyInfoViewModel>() {
    private val TAG = "MyStudyInfoActivity"
    override val layoutResourceID: Int
        get() = R.layout.activity_my_study_info
    override val viewModel: MyStudyInfoViewModel by viewModel()

    override fun initStartView() {
        var main_toolbar: androidx.appcompat.widget.Toolbar =
            findViewById(R.id.camstudy_layout_toolbar)

        setSupportActionBar(main_toolbar) // 툴바를 액티비티의 앱바로 지정
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // 드로어를 꺼낼 홈 버튼 활성화
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_ios_new_24_black) // 홈버튼 이미지 변경
        supportActionBar?.setDisplayShowTitleEnabled(false) // 툴바에 타이틀 안보이게

        init()
    }

    override fun initDataBinding() {
        viewModel.MyStudyInfoResponseLiveData.observe(this, androidx.lifecycle.Observer {
            if (it.isSuccessful) {
                val myStudyInfo = it.body()!!.result
                val aim = if (myStudyInfo.aimTime != null) myStudyInfo.aimTime.subSequence(0, 2)
                    .toString() + "시간" else "목표시간 없음"
                val studyInfo = myStudyInfo.todayTime.hour.toString() + "시간 / " + aim
                val progress = myStudyInfo.achieveTimeRate
                val ddayName =
                    if (myStudyInfo.dream.ddayName != null) myStudyInfo.dream.ddayName else "설정된 디데이가 없습니다."
                val dday = if (myStudyInfo.dream.dday != null) myStudyInfo.dream.dday else ""

                viewDataBinding.myStudyInfo = MyStudyInfo(studyInfo, progress, ddayName, dday)
            }
        })
    }

    override fun initAfterBinding() {

    }

    override fun onStart() {
        super.onStart()

        viewModel.getMyStudyInfoData()
    }

    fun init() {
        val txt_toobarTitle = findViewById<TextView>(R.id.camstudy_info_toolbar_title)
        val txt_peple = findViewById<TextView>(R.id.camstudy_info_toolbar_peple)

        txt_toobarTitle.text = "내 공부"
        txt_peple.visibility = View.GONE
    }
}