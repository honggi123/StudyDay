package com.coworkerteam.coworker.ui.mystudy

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.RecyclerView
import com.coworkerteam.coworker.R
import com.coworkerteam.coworker.data.model.api.MyStudyResponse
import com.coworkerteam.coworker.data.model.other.DrawerBottomInfo
import com.coworkerteam.coworker.databinding.ActivityMyStudyBinding
import com.coworkerteam.coworker.ui.base.BaseActivity
import com.coworkerteam.coworker.ui.base.NavigationAcitivity
import com.coworkerteam.coworker.ui.camstudy.enter.EnterCamstudyActivity
import com.coworkerteam.coworker.ui.dialog.PasswordDialog
import com.coworkerteam.coworker.ui.search.StudySearchActivity
import com.coworkerteam.coworker.ui.statistics.StatisticsActivity
import com.coworkerteam.coworker.ui.study.management.ManagementActivity
import com.coworkerteam.coworker.ui.todolist.TodoListActivity
import com.coworkerteam.coworker.utils.RecyclerViewUtils
import com.google.android.material.navigation.NavigationView
import org.json.JSONObject
import org.koin.androidx.viewmodel.ext.android.viewModel

class MyStudyActivity : NavigationAcitivity<ActivityMyStudyBinding, MyStudyViewModel>() , NavigationView.OnNavigationItemSelectedListener  {
    val TAG = "MyStudyActivity"
    override val layoutResourceID: Int
        get() = R.layout.activity_my_study
    override val viewModel: MyStudyViewModel by viewModel()

    override val drawerLayout: DrawerLayout
        get() = findViewById(R.id.drawer_layout)
    override val navigatinView: NavigationView
        get() = findViewById(R.id.navigationView)

    lateinit var myStudy: MyStudyResponse
    lateinit var pagingGroupAdapter: MyStudyGroupPagingAdapter
    lateinit var pagingDailyAdapter: MyStudyDailyPagingAdapter

    val passwordDialog = PasswordDialog()

    override fun initStartView() {
        super.initStartView()

        init()

        pagingGroupAdapter = MyStudyGroupPagingAdapter(passwordDialog)
        val rv_group = findViewById<RecyclerView>(R.id.my_study_rv_group_study)
        rv_group.adapter = pagingGroupAdapter
        RecyclerViewUtils().setHorizonSpaceDecration(rv_group,10)

        pagingDailyAdapter = MyStudyDailyPagingAdapter(passwordDialog)
        val rv_daily = findViewById<RecyclerView>(R.id.my_study_rv_open_study)
        rv_daily.adapter = pagingDailyAdapter
        RecyclerViewUtils().setHorizonSpaceDecration(rv_daily,10)

        var main_toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.my_study_toolbar)

        setSupportActionBar(main_toolbar) // 툴바를 액티비티의 앱바로 지정
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // 드로어를 꺼낼 홈 버튼 활성화
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_baseline_menu_24_write) // 홈버튼 이미지 변경
        supportActionBar?.title = "내 스터디"

        //패스워드 다이얼로그 ok버튼 함수 세팅
        passwordDialog.onClickOKButton = {i: Int, s: String? ->
            viewModel.getEnterCamstduyData(i, s)
        }
    }

    override fun initDataBinding() {
        viewModel.MyStudyResponseLiveData.observe(this, androidx.lifecycle.Observer {
            if (it.isSuccessful) {
                val myStudyResponse = it.body()!!

                //네비게이션 정보 셋팅
                setNavigaionLoginImage(myStudyResponse.result.profile.loginType)
                setNavigaionProfileImage(myStudyResponse.result.profile.img)
                setNavigaionNickname(myStudyResponse.result.profile.nickname)

                viewDataBinding.draworInfo = DrawerBottomInfo(it.body()!!.result.achieveTimeRate,it.body()!!.result.achieveTodoRate,it.body()!!.result.dream.dday,it.body()!!.result.dream.ddayName)

                viewDataBinding.mystudyResponse = it.body()!!
            }
        })

        viewModel.MyStudyGroupPagingData.observe(this, androidx.lifecycle.Observer {
            pagingGroupAdapter.submitData(lifecycle,it)
        })

        viewModel.MyStudyDailyPagingData.observe(this,androidx.lifecycle.Observer {
            pagingDailyAdapter.submitData(lifecycle,it)
        })

        viewModel.EnterCamstudyResponseLiveData.observe(this, androidx.lifecycle.Observer {
            if (it.isSuccessful) {
                var intent = Intent(this, EnterCamstudyActivity::class.java)
                intent.putExtra("studyInfo", it.body()!!)

                passwordDialog.dismissDialog()
                startActivity(intent)
            } else if (it.code() == 403) {
                val errorMessage = JSONObject(it.errorBody()?.string())

                when(errorMessage.getInt("code")){
                    -12 -> {
                        //비밀번호를 틀린 경우
                        passwordDialog.setErrorMessage(errorMessage.getString("message"))
                    }
                }
            }
        })
    }

    override fun initAfterBinding() {
    }

    override fun onPostResume() {
        super.onPostResume()
        viewModel.getMyStudyData()
    }

    fun init() {
        val txt_menge = findViewById<TextView>(R.id.my_study_txt_study_menage)

        txt_menge.setOnClickListener(View.OnClickListener {
            startActivity(Intent(this, ManagementActivity::class.java))
        })
    }
}