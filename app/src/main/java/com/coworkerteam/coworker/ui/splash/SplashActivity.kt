package com.coworkerteam.coworker.ui.splash

import android.app.Activity
import android.widget.Toast

import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import com.coworkerteam.coworker.R
import com.coworkerteam.coworker.databinding.ActivitySplashBinding
import com.coworkerteam.coworker.ui.base.BaseActivity
import com.coworkerteam.coworker.ui.category.CategoryActivity
import com.coworkerteam.coworker.ui.login.LoginActivity
import com.coworkerteam.coworker.ui.main.MainActivity

import com.github.ybq.android.spinkit.style.Wave

import org.json.JSONObject
import org.koin.androidx.viewmodel.ext.android.viewModel

class SplashActivity : BaseActivity<ActivitySplashBinding, SplashViewModel>() {
    private val TAG = "SplashActivity"

    override val layoutResourceID: Int
        get() = R.layout.activity_splash
    override val viewModel: SplashViewModel by viewModel()



    override fun initStartView() {
        //프로그래스바 로딩 이미지 세팅
        viewDataBinding.spinKit.setIndeterminateDrawable(Wave())
    }



    override fun initDataBinding() {
        viewModel.AutoLoginResponseLiveData.observe(this, androidx.lifecycle.Observer {
            //자동로그인 성공적으로 되었는지 판단
            when {
                it.isSuccessful -> {
                    firebaseLog.addLog(TAG,"auto_login")

                    //카테고리를 선택했었는지에 대한 여부
                    if (it.body()!!.result.isInterest) {
                        //메인으로 이동
                        moveMain()
                    } else {
                        //카테고리 선택하러 이동
                        moveCategory()
                    }
                }
                else -> {
                    //400번대 에러 -> 클라이언트의 문제일 경우가 높음(안드)
                    val errorMessage = JSONObject(it.errorBody()?.string())
                    Log.e(TAG, errorMessage.getString("message"))

                    //400번대 에러 시 로그인 페이지로 이동
                    var intent = Intent(this, LoginActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }
        })
    }

    override fun initAfterBinding() {
       // inAppUpdate = InAppUpdate(this)
        Log.d(TAG,"initAfterBinding")
        //저장되어 있는 리프레쉬 토큰이 있다면
        if (!viewModel.getRefreshToken().isNullOrEmpty()) {
            viewModel.getAutoLoginData()
        } else {
            //없다면 어플을 다시 깔거나, 신규회원이므로 로그인 화면
            var intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG,"onCreate")

    }

    //메인페이지로 이동하는 메소드
    private fun moveMain() {
        var intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    //카테고리로 이동하는 메소드
    private fun moveCategory() {
        var intent = Intent(this, CategoryActivity::class.java)
        startActivity(intent)
        finish()
    }


    override fun onDestroy() {
        super.onDestroy()
    }



}


