package com.coworkerteam.coworker.ui.splash
import android.widget.Toast

import android.content.Intent
import com.coworkerteam.coworker.R
import com.coworkerteam.coworker.data.local.prefs.AppPreferencesHelper
import com.coworkerteam.coworker.data.local.prefs.PreferencesHelper
import com.coworkerteam.coworker.databinding.ActivitySplashBinding
import com.coworkerteam.coworker.ui.base.BaseActivity
import com.coworkerteam.coworker.ui.category.CategoryActivity
import com.coworkerteam.coworker.ui.login.LoginActivity
import com.coworkerteam.coworker.ui.main.MainActivity
import org.koin.android.ext.android.get
import org.koin.androidx.viewmodel.ext.android.viewModel

class SplashActivity : BaseActivity<ActivitySplashBinding, SplashViewModel>() {

    override val layoutResourceID: Int
        get() = R.layout.activity_splash
    override val viewModel: SplashViewModel by viewModel()

    override fun initStartView() {
    }

    override fun initDataBinding() {
        viewModel.AutoLoginResponseLiveData.observe(this, androidx.lifecycle.Observer {
            //자동로그인 성공적으로 되었는지 판단
            if(it.isSuccessful){
                //카테고리를 선택했었는지에 대한 여부
                if(it.body()!!.result.isInterest){
                    //메인으로 이동
                    moveMain()
                }else{
                    //카테고리 선택하러 이동
                    moveCategory()
                }
            }else if(it.code() == 401){
                //refreshToken이 만료되었을 경우, 로그인 페이지로 이동
                moveLogin()
            }else if(it.code() == 404){
                //유효하지 않은 refreshToken,없는 유저인 경우 로그인 페이지로 이동
                moveLogin()
            }else{
                Toast.makeText(getApplicationContext(), it.message(),Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun initAfterBinding() {
        //저장되어 있는 리프레쉬 토큰이 있다면
        if(!viewModel.getRefreshToken().isNullOrEmpty()){
            viewModel.getAutoLoginData()
        }else{
            //없다면 어플을 다시 깔거나, 신규회원이므로 로그인 화면
            moveLogin()
        }
    }

    //로그인 페이지로 이동하는 메소드
    fun moveLogin(){
        var intent = Intent(this,LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    //메인페이지로 이동하는 메소드
    fun moveMain(){
        var intent = Intent(this,MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    //카테고리로 이동하는 메소드
    fun moveCategory(){
        var intent = Intent(this,CategoryActivity::class.java)
        startActivity(intent)
        finish()
    }

}


