package com.coworkerteam.coworker.ui.yourday

import android.content.Intent
import android.view.View
import android.widget.ImageView
import androidx.drawerlayout.widget.DrawerLayout
import androidx.viewpager.widget.ViewPager
import com.coworkerteam.coworker.R
import com.coworkerteam.coworker.databinding.ActivityStudySearchBinding
import com.coworkerteam.coworker.ui.base.NavigationActivity
import com.coworkerteam.coworker.ui.search.FragmentAdapter
import com.coworkerteam.coworker.ui.search.StudySearchViewModel
import com.coworkerteam.coworker.ui.study.make.MakeStudyActivity
import com.google.android.material.navigation.NavigationView
import com.google.android.material.tabs.TabLayout
import org.koin.androidx.viewmodel.ext.android.viewModel

class YourDayActivity(

) : NavigationActivity<ActivityStudySearchBinding, StudySearchViewModel>() {

    override val layoutResourceID: Int
        get() = R.layout.activity_yourday
    override val viewModel: StudySearchViewModel by viewModel()

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
                var intent = Intent(this, WriteMoodPostActivity::class.java)
                startActivity(intent)
            }
        )

    }

    override fun initDataBinding() {
    }

    override fun initAfterBinding() {
    }

    fun fragment_init() {
        val pagerAdapter = FragmentAdapter_YourDay(supportFragmentManager)
        val pager = findViewById<ViewPager>(R.id.yourday_viewPager)
        pager.visibility = View.VISIBLE
        pager.adapter = pagerAdapter
        val tab = findViewById<TabLayout>(R.id.yourday_tab)
        tab.setupWithViewPager(pager)
    }
}

