package com.coworkerteam.coworker.ui.splash

import android.app.Activity
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.widget.Toast

import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.UpdateAvailability
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.net.UrlQuerySanitizer
import android.os.Build
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import androidx.annotation.Nullable
import androidx.core.content.ContextCompat
import com.coworkerteam.coworker.R
import com.coworkerteam.coworker.databinding.ActivitySplashBinding
import com.coworkerteam.coworker.ui.base.BaseActivity
import com.coworkerteam.coworker.ui.category.CategoryActivity
import com.coworkerteam.coworker.ui.login.LoginActivity
import com.coworkerteam.coworker.ui.main.MainActivity
import com.github.ybq.android.spinkit.style.Wave
import com.google.android.play.core.appupdate.AppUpdateManager

import org.json.JSONObject
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.logging.Logger

class SplashActivity : BaseActivity<ActivitySplashBinding, SplashViewModel>() {
    private val TAG = "SplashActivity"

    override val layoutResourceID: Int
        get() = R.layout.activity_splash
    override val viewModel: SplashViewModel by viewModel()

    private val DEFAULT_PATH = "studyday://main";
    var studyinfo : String? = null

    lateinit var appUpdateManager : AppUpdateManager

    override fun initStartView() {
        appUpdateManager = AppUpdateManagerFactory.create(this)


        appUpdateManager?.let {
            it.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
                if(appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE){
                    val mDialogView =
                        LayoutInflater.from(this).inflate(R.layout.dialog_updatecheck, null)
                    val mBuilder = AlertDialog.Builder(this).setView(mDialogView)
                    val builder = mBuilder.show()

                    builder.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

                    val btn_ok = mDialogView.findViewById<Button>(R.id.dialog_btn_ok)
                    builder.setCancelable(false)

                    btn_ok.setOnClickListener(View.OnClickListener {
                        val ownGooglePlayLink = "market://details?id=com.coworkerteam.coworker"
                        val ownWebLink =
                            "https://play.google.com/store/apps/details?id=com.coworkerteam.coworker"
                        finishAffinity()
                        try {
                            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(ownGooglePlayLink)))
                        } catch (anfe: ActivityNotFoundException) {
                            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(ownWebLink)))
                        }
                        //moveTaskToBack(true); // 태스크를 백그라운드로 이동
                       // finishAndRemoveTask(); // 액티비티 종료 + 태스크 리스트에서 지우기
                        System.exit(0)
                    })
                }else{
                    //앱 업데이트가 되어있고 저장되어 있는 리프레쉬 토큰이 있다면
                    if (!viewModel.getRefreshToken().isNullOrEmpty()) {
                        viewModel.getAutoLoginData()
                    } else {
                        //없다면 어플을 다시 깔거나, 신규회원이므로 로그인 화면
                       var intent = Intent(this, LoginActivity::class.java)
                        startActivity(intent)
                       finish()
                    }
                }
                it.appUpdateInfo.addOnFailureListener{it ->
                    //앱 업데이트정보 가져오는데 실패 했고 저장되어 있는 리프레쉬 토큰이 있다면
                    if (!viewModel.getRefreshToken().isNullOrEmpty()) {
                        viewModel.getAutoLoginData()
                    } else {
                        //없다면 어플을 다시 깔거나, 신규회원이므로 로그인 화면
                        var intent = Intent(this, LoginActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                }

            }

        }

        var intent : Intent = getIntent();
        Log.d(TAG,"data : " + intent.getDataString())
        if (intent.getDataString()?.startsWith(DEFAULT_PATH+"?studylink=") == true) {
            var param : String = intent.getDataString()!!.replace(DEFAULT_PATH, "");
            studyinfo = param
        }

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

    }

    //메인페이지로 이동하는 메소드
    private fun moveMain() {
        var intent = Intent(this, MainActivity::class.java)
        if (studyinfo!=null){
            intent.putExtra("studyinfo",studyinfo)
        }
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


