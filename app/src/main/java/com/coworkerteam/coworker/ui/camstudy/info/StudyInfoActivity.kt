package com.coworkerteam.coworker.ui.camstudy.info

import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.coworkerteam.coworker.R
import com.coworkerteam.coworker.data.local.prefs.AppPreferencesHelper
import com.coworkerteam.coworker.databinding.ActivityStudyInfoBinding
import com.coworkerteam.coworker.ui.base.BaseActivity
import com.coworkerteam.coworker.ui.camstudy.CamStudyCategotyAdapter
import org.json.JSONObject
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
            when {
                it.isSuccessful -> {
                    viewDataBinding.studyInfo = it.body()
                    initRV(it.body()!!.result.studyInfo.get(0).category)
                }
                it.code() == 400 -> {
                    //요청값을 제대로 다 전달하지 않은 경우 ex. 날짜 또는 요청타입 값이 잘못되거나 없을때
                    val errorMessage = JSONObject(it.errorBody()?.string())
                    Log.e(TAG, errorMessage.getString("message"))

                    //400번대 에러로 스터디 정보 데이터 가져오기 실패했을 경우, 사용자에게 알려준다.
                    Toast.makeText(this,"스터디 정보 데이터를 가져오지 못했습니다.", Toast.LENGTH_SHORT).show()
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
                            Toast.makeText(this,"더이상 존재하지 않는 스터디입니다.", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        })
    }

    override fun initAfterBinding() {
        viewModel.getStudyInfoData(intent.getIntExtra("studyIdx",-1))
    }

    private fun initRV(category: String){
        val rv = findViewById<RecyclerView>(R.id.study_info_rv)
        val categorys = category.split("|")

        var myStudyAdepter = CamStudyCategotyAdapter(this)
        myStudyAdepter.datas = categorys.toMutableList()
        rv.adapter = myStudyAdepter
    }

}