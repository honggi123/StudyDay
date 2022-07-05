package com.coworkerteam.coworker.ui.base

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.GravityCompat
import androidx.databinding.ViewDataBinding
import androidx.drawerlayout.widget.DrawerLayout
import com.bumptech.glide.Glide
import com.coworkerteam.coworker.data.UserRepository
import com.coworkerteam.coworker.ui.main.MainActivity
import com.coworkerteam.coworker.ui.mystudy.MyStudyActivity
import com.coworkerteam.coworker.ui.search.StudySearchActivity
import com.coworkerteam.coworker.ui.setting.SettingActivity
import com.coworkerteam.coworker.ui.statistics.StatisticsActivity
import com.coworkerteam.coworker.ui.todolist.TodoListActivity
import com.coworkerteam.coworker.ui.yourday.YourDayActivity
import com.google.android.material.navigation.NavigationView
import de.hdodenhof.circleimageview.CircleImageView
import org.koin.android.ext.koin.androidApplication

open abstract class NavigationActivity<T : ViewDataBinding, R : BaseViewModel> :
    BaseActivity<T, R>(),
    NavigationView.OnNavigationItemSelectedListener {
    val TAG_Navigation = "NavigationActivity"
    abstract val drawerLayout: DrawerLayout
    abstract val navigatinView: NavigationView
    private val PREF_KEY_CURRENT_USER_NAME = "PREF_KEY_CURRENT_USER_NAME"
    private var navigationclick = false
    fun setNavigaionProfileImage(img: String) {
        var navigationHeaderProfile =
            navigatinView.getHeaderView(0)
                .findViewById<CircleImageView>(com.coworkerteam.coworker.R.id.navi_profile_image)
        Glide.with(this) //해당 환경의 Context나 객체 입력
            .load(img) //URL, URI 등등 이미지를 받아올 경로
            .into(navigationHeaderProfile) //받아온 이미지를 받을 공간(ex. ImageView)
    }

    fun setNavigaionLoginImage(loginType: String) {
        var navigationHeaderLogin =
            navigatinView.getHeaderView(0)
                .findViewById<CircleImageView>(com.coworkerteam.coworker.R.id.navi_login_image)
        if (loginType == "google") {
            navigationHeaderLogin.setImageResource(com.coworkerteam.coworker.R.drawable.btn_google_light_focus)
        } else if (loginType == "kakao") {
            navigationHeaderLogin.setImageResource(com.coworkerteam.coworker.R.drawable.kakao_icon)
        } else if (loginType == "naver") {
            navigationHeaderLogin.setImageResource(com.coworkerteam.coworker.R.drawable.naver_icon)
        }
    }

    fun setNavigaionNickname(nickname: String) {
        var mPrefs = applicationContext.getSharedPreferences("studyday", Context.MODE_PRIVATE)
        mPrefs!!.edit().putString(PREF_KEY_CURRENT_USER_NAME, nickname).apply()

        navigatinView.getHeaderView(0)
            .findViewById<TextView>(com.coworkerteam.coworker.R.id.navi_nickname).text = nickname
    }

    override fun initStartView() {
        navigatinView.setNavigationItemSelectedListener(this)
        navigatinView.getHeaderView(0)
            .findViewById<ImageView>(com.coworkerteam.coworker.R.id.navi_setting)
            .setOnClickListener(
                View.OnClickListener {
                    val intent = Intent(this, SettingActivity::class.java)
                    startActivity(intent)
                })
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        var moveIntent: Intent? = null


        when (item.itemId) {
            com.coworkerteam.coworker.R.id.menuitem1 -> {
                firebaseLog.addLog(TAG_Navigation,"show_main")
                moveIntent = Intent(this, MainActivity::class.java)
                setFlag(moveIntent)

            }
            com.coworkerteam.coworker.R.id.menuitem2 -> {
                firebaseLog.addLog(TAG_Navigation,"show_mystudy")
                moveIntent = Intent(this, MyStudyActivity::class.java)
                setFlag(moveIntent)
            }

            com.coworkerteam.coworker.R.id.menuitem3 -> {
                firebaseLog.addLog(TAG_Navigation,"show_search")
                moveIntent = Intent(this, StudySearchActivity::class.java)
                setFlag(moveIntent)
            }

            com.coworkerteam.coworker.R.id.menuitem4 -> {
                firebaseLog.addLog(TAG_Navigation,"show_statics")
                moveIntent = Intent(this, StatisticsActivity::class.java)
                setFlag(moveIntent)
            }
            com.coworkerteam.coworker.R.id.menuitem5 -> {
                firebaseLog.addLog(TAG_Navigation,"show_todo")
                moveIntent = Intent(this, TodoListActivity::class.java)
                setFlag(moveIntent)
            }

            com.coworkerteam.coworker.R.id.menuitem6 -> {
                firebaseLog.addLog(TAG_Navigation,"show_yourday")
                moveIntent = Intent(this, YourDayActivity::class.java)
                moveIntent?.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                moveIntent?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
        }

        drawerLayout.closeDrawers()
        startActivity(moveIntent)

        return true
    }

    fun setFlag(moveIntent : Intent){
        moveIntent!!.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
        moveIntent?.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        moveIntent?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
    }

    // 툴바 메뉴 버튼이 클릭 됐을 때 실행하는 함수
    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        // 클릭한 툴바 메뉴 아이템 id 마다 다르게 실행하도록 설정
        when (item!!.itemId) {
            android.R.id.home -> {
                // 햄버거 버튼 클릭시 네비게이션 드로어 열기
                drawerLayout.openDrawer(GravityCompat.START)
            }
        }
        return true
    }


    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawers()
        } else {
            super.onBackPressed()
        }
    }
}