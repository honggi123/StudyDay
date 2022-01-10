package com.coworkerteam.coworker.ui.setting.profile

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.coworkerteam.coworker.R
import com.coworkerteam.coworker.data.local.prefs.AppPreferencesHelper
import com.coworkerteam.coworker.data.model.api.ProfileManageResponse
import com.coworkerteam.coworker.data.remote.StudydayService
import com.coworkerteam.coworker.databinding.ActivityMyProfileBinding
import com.coworkerteam.coworker.databinding.ActivityWithdrawalBinding
import com.coworkerteam.coworker.ui.base.BaseActivity
import com.coworkerteam.coworker.ui.category.CategoryViewModel
import com.coworkerteam.coworker.ui.setting.account.WithdrawalViewModel
import com.coworkerteam.coworker.ui.setting.profile.edit.ProfileEditActivity
import com.google.android.gms.common.api.ApiException
import de.hdodenhof.circleimageview.CircleImageView
import okhttp3.OkHttpClient
import org.koin.androidx.viewmodel.ext.android.viewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager


class MyProfileActivity : BaseActivity<ActivityMyProfileBinding, MyProfileViewModel>() {

    val TAG = "MyProfileActivity"
    override val layoutResourceID: Int
        get() = R.layout.activity_my_profile
    override val viewModel: MyProfileViewModel by viewModel()

    lateinit var profileManageResponse: ProfileManageResponse.Result.Profile

    override fun initStartView() {
        var main_toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.my_profile_toolbar)

        setSupportActionBar(main_toolbar) // 툴바를 액티비티의 앱바로 지정
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // 드로어를 꺼낼 홈 버튼 활성화
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_ios_new_24) // 홈버튼 이미지 변경
        supportActionBar?.title = "프로필관리"
    }

    override fun initDataBinding() {
        viewModel.MyProfileResponseLiveData.observe(this, androidx.lifecycle.Observer {
            if (it.isSuccessful) {
                profileManageResponse = it.body()!!.result.profile
                setLoginImage(viewDataBinding.myProfileLoginImg,profileManageResponse.loginType)
                init()
                rv_init()
            }
        })
    }

    override fun initAfterBinding() {
    }

    override fun onStart() {
        super.onStart()
        viewModel.getMyProfileData()
    }

    fun init() {
        val profile_img = findViewById<CircleImageView>(R.id.my_profile_img)
        val txt_nickname = findViewById<TextView>(R.id.my_profile_txt_nickname)
        val txt_email = findViewById<TextView>(R.id.my_profile_email)

        /*  의존성(dependencies) 추가
            implementation 'com.github.bumptech.glide:glide:4.11.0'
              annotationProcessor 'com.github.bumptech.glide:compiler:4.11.0'
        */
        Glide.with(this) //해당 환경의 Context나 객체 입력
            .load(profileManageResponse.img) //URL, URI 등등 이미지를 받아올 경로
            .into(profile_img) //받아온 이미지를 받을 공간(ex. ImageView)

        txt_nickname.text = profileManageResponse.nickname
        txt_email.text = profileManageResponse.email

    }

    fun rv_init() {
        val rvCategory = findViewById<RecyclerView>(R.id.my_profile_rv_category)

        var mainStudyCategoryAdapter: MyProfileAdapter = MyProfileAdapter(this)
        mainStudyCategoryAdapter.datas = profileManageResponse.category.split("|").toMutableList()
        rvCategory.adapter = mainStudyCategoryAdapter
    }

    fun setLoginImage(v: View, loginType: String) {
        val imageView = v as CircleImageView

        if (loginType.equals("google")) {
            imageView.setImageResource(com.coworkerteam.coworker.R.drawable.google_icon)
        } else if (loginType.equals("kakao")) {
            imageView.setImageResource(com.coworkerteam.coworker.R.drawable.kakao_icon)
        } else if (loginType.equals("naver")) {
            imageView.setImageResource(com.coworkerteam.coworker.R.drawable.naver_icon)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        getMenuInflater().inflate(R.menu.myprofile_menu, menu);
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_modify -> {
                val intent = Intent(this, ProfileEditActivity::class.java)
                intent.putExtra("profile", profileManageResponse)
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }

}