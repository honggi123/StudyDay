package com.coworkerteam.coworker.ui.yourday

import android.content.Intent
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.drawerlayout.widget.DrawerLayout
import androidx.viewpager.widget.ViewPager
import com.coworkerteam.coworker.R
import com.coworkerteam.coworker.data.model.other.DrawerBottomInfo
import com.coworkerteam.coworker.databinding.ActivityYourdayBinding
import com.coworkerteam.coworker.ui.base.NavigationActivity
import com.coworkerteam.coworker.ui.yourday.moodPost.make.EmotionChoiceActivity
import com.google.android.material.navigation.NavigationView
import com.google.android.material.tabs.TabLayout
import org.json.JSONObject
import org.koin.androidx.viewmodel.ext.android.viewModel

class YourDayActivity(
) : NavigationActivity<ActivityYourdayBinding, YourdayViewModel>() {

    val TAG = "YourDayActivity"
    override val layoutResourceID: Int
        get() = R.layout.activity_yourday
    override val viewModel: YourdayViewModel by viewModel()

    override val drawerLayout: DrawerLayout
        get() = findViewById(R.id.drawer_layout)
    override val navigatinView: NavigationView
        get() = findViewById(R.id.navigationView)

    override fun initStartView() {

              super.initStartView()
              var main_toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.yourday_toolber)

              setSupportActionBar(main_toolbar) // 툴바를 액티비티의 앱바로 지정
              supportActionBar?.setDisplayHomeAsUpEnabled(true) // 드로어를 꺼낼 홈 버튼 활성화
              supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_baseline_menu_24_write) // 홈버튼 이미지 변경
              supportActionBar?.title = "너의 하루는"

                     fragment_init()


                     //툴바의 + 아이콘에 대한 세팅
                     val main_toolbar_writepost = findViewById<ImageView>(R.id.main_toolbar_writepost)

                     main_toolbar_writepost.visibility = View.VISIBLE
                     main_toolbar_writepost.setOnClickListener(
                         View.OnClickListener {
                             firebaseLog.addLog(TAG,"add_post")
                             var intent = Intent(this, EmotionChoiceActivity::class.java)
                             startActivity(intent)
                         }
                     )

    }

    override fun initDataBinding() {

        viewModel.MoodPostPagingData.observe(this, androidx.lifecycle.Observer {
            when {
                it.isSuccessful -> {
                    //네비게이션 정보 세팅

                    setNavigaionProfileImage(it.body()!!.result.get(0).profile.img)
                   setNavigaionLoginImage(it.body()!!.result.get(0).profile.login_type)
                    setNavigaionNickname(it.body()!!.result.get(0).profile.nickname)

                    viewDataBinding.draworInfo = DrawerBottomInfo(it.body()!!.result.get(0).achieveTimeRate,it.body()!!.result.get(0).achieveTodoRate,it.body()!!.result.get(0).dream.dday,it.body()!!.result.get(0).dream.dday_name)
                }
                it.code() == 400 -> {
                    //요청값을 제대로 다 전달하지 않은 경우 ex. 날짜 또는 요청타입 값이 잘못되거나 없을때
                    val errorMessage = JSONObject(it.errorBody()?.string())
                    Log.e(TAG, errorMessage.getString("message"))

                    //400번대 에러로 검색 데이터 가져오기가 실패했을 경우, 사용자에게 알려준다.
                    Toast.makeText(this,"검색 데이터를 가져오지 못했습니다. 나중 다시 시도해주세요.", Toast.LENGTH_SHORT).show()
                }
                it.code() == 404 -> {
                    //존재하지 않은 회원일 경우
                    val errorMessage = JSONObject(it.errorBody()?.string())
                    Log.e(TAG, errorMessage.getString("message"))

                    moveLogin()
                }
            }
        })

    }

    override fun initAfterBinding() {
        viewModel.getMoodPost("latest",1)
    }

    fun fragment_init() {
        val pagerAdapter = FragmentAdapter_YourDay(supportFragmentManager)
        val pager = findViewById<ViewPager>(R.id.yourday_viewPager)
        pager.visibility = View.VISIBLE
        pager.adapter = pagerAdapter
        val tab = findViewById<TabLayout>(R.id.yourday_tab)
        tab.setupWithViewPager(pager)

    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG,"ondestroy")
    }


}

