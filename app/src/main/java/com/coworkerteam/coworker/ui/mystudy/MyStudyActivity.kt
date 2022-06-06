package com.coworkerteam.coworker.ui.mystudy

import android.content.Intent
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.drawerlayout.widget.DrawerLayout
import androidx.paging.LoadState
import com.coworkerteam.coworker.R
import com.coworkerteam.coworker.data.model.api.MyStudyResponse
import com.coworkerteam.coworker.data.model.other.DrawerBottomInfo
import com.coworkerteam.coworker.databinding.ActivityMyStudyBinding
import com.coworkerteam.coworker.ui.base.NavigationActivity
import com.coworkerteam.coworker.ui.dialog.PasswordDialog
import com.coworkerteam.coworker.ui.unity.UnityActivity
import com.coworkerteam.coworker.ui.study.management.ManagementActivity
import com.coworkerteam.coworker.utils.RecyclerViewUtils
import com.google.android.material.navigation.NavigationView
import org.json.JSONObject
import org.koin.androidx.viewmodel.ext.android.viewModel

class MyStudyActivity : NavigationActivity<ActivityMyStudyBinding, MyStudyViewModel>() , NavigationView.OnNavigationItemSelectedListener  {
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

        setSupportActionBar(viewDataBinding.myStudyToolbar as androidx.appcompat.widget.Toolbar) // 툴바를 액티비티의 앱바로 지정
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // 드로어를 꺼낼 홈 버튼 활성화
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_baseline_menu_24_write) // 홈버튼 이미지 변경
        supportActionBar?.title = "내 스터디"

        //페이징3 세팅
        pagingGroupAdapter = MyStudyGroupPagingAdapter(passwordDialog)

        viewDataBinding.myStudyRvGroupStudy.adapter = pagingGroupAdapter
        RecyclerViewUtils().setHorizonSpaceDecration(viewDataBinding.myStudyRvGroupStudy,10)

        pagingGroupAdapter.addLoadStateListener { loadState ->
            if (loadState.source.refresh is LoadState.NotLoading && loadState.append.endOfPaginationReached && pagingGroupAdapter.itemCount < 1) {
                viewDataBinding.textView25.visibility = View.VISIBLE
            } else {
                viewDataBinding.textView25.visibility = View.GONE
            }
        }

        pagingDailyAdapter = MyStudyDailyPagingAdapter(passwordDialog)
        viewDataBinding.myStudyRvOpenStudy.adapter = pagingDailyAdapter
        RecyclerViewUtils().setHorizonSpaceDecration( viewDataBinding.myStudyRvOpenStudy,10)

        pagingDailyAdapter.addLoadStateListener { loadState ->
            if (loadState.source.refresh is LoadState.NotLoading && loadState.append.endOfPaginationReached && pagingDailyAdapter.itemCount < 1) {
                viewDataBinding.textView23.visibility = View.VISIBLE
            } else {
                viewDataBinding.textView23.visibility = View.GONE
            }
        }


        //패스워드 다이얼로그 ok버튼 함수 세팅
        passwordDialog.onClickOKButton = {i: Int, s: String? ->
            viewModel.getEnterCamstduyData(i, s)
            firebaseLog.addLog(TAG,"check_study_password")
        }
        checkdate()

    }

    override fun initDataBinding() {
        viewModel.MyStudyResponseLiveData.observe(this, androidx.lifecycle.Observer {
            when {
                it.isSuccessful -> {
                    val myStudyResponse = it.body()!!

                    //네비게이션 정보 셋팅
                    setNavigaionLoginImage(myStudyResponse.result.profile.loginType)
                    setNavigaionProfileImage(myStudyResponse.result.profile.img)
                    setNavigaionNickname(myStudyResponse.result.profile.nickname)

                    viewDataBinding.draworInfo = DrawerBottomInfo(it.body()!!.result.achieveTimeRate,it.body()!!.result.achieveTodoRate,it.body()!!.result.dream.dday,it.body()!!.result.dream.ddayName)

                    viewDataBinding.mystudyResponse = it.body()!!
                }
                it.code() == 400 -> {
                    //요청값을 제대로 다 전달하지 않은 경우 ex. 날짜 또는 요청타입 값이 잘못되거나 없을때
                    val errorMessage = JSONObject(it.errorBody()?.string())
                    Log.e(TAG, errorMessage.getString("message"))

                    //400번대 에러로 내스터디 데이터 가져오기가 실패했을 경우, 사용자에게 알려준다.
                    Toast.makeText(this,"내스터디 데이터를 가져오지 못했습니다. 나중 다시 시도해주세요.",Toast.LENGTH_SHORT).show()
                }
                it.code() == 404 -> {
                    //존재하지 않은 회원일 경우
                    val errorMessage = JSONObject(it.errorBody()?.string())
                    Log.e(TAG, errorMessage.getString("message"))

                    moveLogin()
                }
            }
        })

        viewModel.MyStudyGroupPagingData.observe(this, androidx.lifecycle.Observer {
            pagingGroupAdapter.submitData(lifecycle,it)
        })

        viewModel.MyStudyDailyPagingData.observe(this,androidx.lifecycle.Observer {
            pagingDailyAdapter.submitData(lifecycle,it)
        })

        viewModel.EnterCamstudyResponseLiveData.observe(this, androidx.lifecycle.Observer {
            when {
                it.isSuccessful -> {
                    startActivity(intent)
                    /*
                    var intent = Intent(this, EnterCamstudyActivity::class.java)
                    intent.putExtra("studyInfo", it.body()!!)

                    passwordDialog.dismissDialog()
                    */
                    Log.d(TAG,"studyinfo : "+ it.body()!!)
                    passwordDialog.dismissDialog()
                    var intent = Intent(this, UnityActivity::class.java)
                    intent.putExtra("studyInfo", it.body()!!)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    Log.d(TAG,"studyInfo : "+it.body().toString())
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
    }

    override fun initAfterBinding() {
        viewModel.getMyStudyData()
    }

    override fun onRestart() {
        super.onRestart()
        pagingGroupAdapter.refresh()
        pagingDailyAdapter.refresh()

    }

    fun init() {
        val txt_menge = findViewById<TextView>(R.id.my_study_txt_study_menage)

        txt_menge.setOnClickListener(View.OnClickListener {
            startActivity(Intent(this, ManagementActivity::class.java))
        })
    }
}