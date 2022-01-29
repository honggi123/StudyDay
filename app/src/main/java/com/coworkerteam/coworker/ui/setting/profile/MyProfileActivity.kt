package com.coworkerteam.coworker.ui.setting.profile

import android.content.Intent
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.bumptech.glide.Glide
import com.coworkerteam.coworker.R
import com.coworkerteam.coworker.data.model.api.ProfileManageResponse
import com.coworkerteam.coworker.databinding.ActivityMyProfileBinding
import com.coworkerteam.coworker.ui.base.BaseActivity
import com.coworkerteam.coworker.ui.setting.profile.edit.ProfileEditActivity
import de.hdodenhof.circleimageview.CircleImageView
import org.json.JSONObject
import org.koin.androidx.viewmodel.ext.android.viewModel


class MyProfileActivity : BaseActivity<ActivityMyProfileBinding, MyProfileViewModel>() {
    private val TAG = "MyProfileActivity"

    override val layoutResourceID: Int
        get() = R.layout.activity_my_profile
    override val viewModel: MyProfileViewModel by viewModel()

    lateinit var profileManageResponse: ProfileManageResponse.Result.Profile

    override fun initStartView() {
        setSupportActionBar(viewDataBinding.myProfileToolbar) // 툴바를 액티비티의 앱바로 지정
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // 드로어를 꺼낼 홈 버튼 활성화
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_ios_new_24) // 홈버튼 이미지 변경
        supportActionBar?.title = "프로필 관리"
    }

    override fun initDataBinding() {
        viewModel.MyProfileResponseLiveData.observe(this, androidx.lifecycle.Observer {
            when {
                it.isSuccessful -> {
                    profileManageResponse = it.body()!!.result.profile
                    setLoginImage(viewDataBinding.myProfileLoginImg,profileManageResponse.loginType)
                    init()
                    rv_init()
                }
                it.code() == 400 -> {
                    //API 요청값을 제대로 다 전달하지 않은 경우
                    val errorMessage = JSONObject(it.errorBody()?.string())
                    Log.e(TAG, errorMessage.getString("message"))

                    //400번대 에러로 프로필 데이터를 받아오는 것이 실패했을 경우, 사용자에게 알려준다.
                    Toast.makeText(this,"프로필 데이터를 가져오는 것을 실패했습니다.", Toast.LENGTH_SHORT).show()
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
    }

    override fun onStart() {
        super.onStart()
        viewModel.getMyProfileData()
    }

    fun init() {
        Glide.with(this) //해당 환경의 Context나 객체 입력
            .load(profileManageResponse.img) //URL, URI 등등 이미지를 받아올 경로
            .into(viewDataBinding.myProfileImg) //받아온 이미지를 받을 공간(ex. ImageView)

        viewDataBinding.myProfileTxtNickname.text = profileManageResponse.nickname
        viewDataBinding.myProfileEmail.text = profileManageResponse.email
    }

    fun rv_init() {
        var myProfileAdapter = MyProfileAdapter(this)
        myProfileAdapter.datas = profileManageResponse.category.split("|").toMutableList()
        viewDataBinding.myProfileRvCategory.adapter = myProfileAdapter
    }

    fun setLoginImage(v: View, loginType: String) {
        val imageView = v as CircleImageView

        when (loginType) {
            "google" -> {
                imageView.setImageResource(com.coworkerteam.coworker.R.drawable.google_icon)
            }
            "kakao" -> {
                imageView.setImageResource(com.coworkerteam.coworker.R.drawable.kakao_icon)
            }
            "naver" -> {
                imageView.setImageResource(com.coworkerteam.coworker.R.drawable.naver_icon)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.myprofile_menu, menu)
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