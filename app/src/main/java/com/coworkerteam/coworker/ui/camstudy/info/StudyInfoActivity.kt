package com.coworkerteam.coworker.ui.camstudy.info

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.coworkerteam.coworker.R
import com.coworkerteam.coworker.data.local.prefs.AppPreferencesHelper
import com.coworkerteam.coworker.databinding.ActivityStudyInfoBinding
import com.coworkerteam.coworker.ui.base.BaseActivity
import com.coworkerteam.coworker.ui.camstudy.CamStudyCategotyAdapter
import org.koin.androidx.viewmodel.ext.android.viewModel

class StudyInfoActivity : BaseActivity<ActivityStudyInfoBinding, StudyInfoViewModel>() {
    private val TAG = "StudyInfoActivity"
    override val layoutResourceID: Int
        get() = R.layout.activity_study_info
    override val viewModel: StudyInfoViewModel by viewModel()

    override fun initStartView() {
        var main_toolbar: androidx.appcompat.widget.Toolbar =
            findViewById(R.id.camstudy_layout_toolbar)

        setSupportActionBar(main_toolbar) // 툴바를 액티비티의 앱바로 지정
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // 드로어를 꺼낼 홈 버튼 활성화
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_ios_new_24_black) // 홈버튼 이미지 변경
        supportActionBar?.setDisplayShowTitleEnabled(false) // 툴바에 타이틀 안보이게

        val txt_toobarTitle = findViewById<TextView>(R.id.camstudy_info_toolbar_title)
        val txt_peple = findViewById<TextView>(R.id.camstudy_info_toolbar_peple)

        txt_toobarTitle.text = "스터디 정보"
        txt_peple.visibility = View.GONE
    }

    override fun initDataBinding() {
        viewModel.StudyInfoResponseLiveData.observe(this, androidx.lifecycle.Observer {
            if(it.isSuccessful){
                viewDataBinding.studyInfo = it.body()
                initRV(it.body()!!.result.studyInfo.get(0).category)
            }
        })
    }

    override fun initAfterBinding() {

    }

    override fun onStart() {
        super.onStart()
        viewModel.getStudyInfoData(intent.getIntExtra("studyIdx",-1))
    }

    private fun initRV(category: String){
        val rv = findViewById<RecyclerView>(R.id.study_info_rv)
        val categorys = category.split("|")

        var myStudyAdepter: CamStudyCategotyAdapter = CamStudyCategotyAdapter(this)
        myStudyAdepter.datas = categorys.toMutableList()
        rv.adapter = myStudyAdepter
    }

}