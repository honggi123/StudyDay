package com.coworkerteam.coworker.ui.setting

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.coworkerteam.coworker.BuildConfig
import com.coworkerteam.coworker.R
import com.coworkerteam.coworker.data.local.prefs.AppPreferencesHelper
import com.coworkerteam.coworker.data.model.api.ApiRequest
import com.coworkerteam.coworker.data.remote.StudydayService
import com.coworkerteam.coworker.databinding.ActivityCategoryBinding
import com.coworkerteam.coworker.databinding.ActivitySettingBinding
import com.coworkerteam.coworker.ui.base.BaseActivity
import com.coworkerteam.coworker.ui.category.CategoryViewModel
import com.coworkerteam.coworker.ui.login.LoginActivity
import com.coworkerteam.coworker.ui.setting.account.WithdrawalActivity
import com.coworkerteam.coworker.ui.setting.profile.MyProfileActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.kakao.sdk.user.UserApiClient
import com.nhn.android.naverlogin.OAuthLogin
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


class SettingActivity : BaseActivity<ActivitySettingBinding, SettingViewModel>() {
    val TAG = "SettingActivity"
    override val layoutResourceID: Int
        get() = R.layout.activity_setting
    override val viewModel: SettingViewModel by viewModel()

    override fun initStartView() {
        var main_toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.setting_toolbar)

        setSupportActionBar(main_toolbar) // 툴바를 액티비티의 앱바로 지정
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // 드로어를 꺼낼 홈 버튼 활성화
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_ios_new_24) // 홈버튼 이미지 변경
        supportActionBar?.title = "설정"

        init()
    }

    override fun initDataBinding() {
        viewModel.SettingResponseLiveData.observe(this, androidx.lifecycle.Observer {
            //로그아웃이 성공적으로 이뤄짐
            if (it.isSuccessful) {
                //로그인으로 이동
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish()
            }
        })
    }

    override fun initAfterBinding() {
    }

    fun init() {
        val txt_profile: TextView = findViewById(R.id.setting_profile)
        val txt_to_developer: TextView = findViewById(R.id.setting_to_developer)
        val txt_logout: TextView = findViewById(R.id.setting_logout)
        val txt_wirhdrawal: TextView = findViewById(R.id.setting_withdrawal)

        //테스트
        val app: TextView = findViewById(R.id.textView112)

        txt_to_developer.setOnClickListener(View.OnClickListener {
            val intent = Intent(Intent.ACTION_SENDTO)
            val emailTitle = "["+getString(R.string.app_name)+"] 서비스에 대한 문의"
            val emailContent = String.format("App Version : %s \n Android(SDK) : %d(%s) \n 내용 : ", BuildConfig.VERSION_NAME, Build.VERSION.SDK_INT, Build.VERSION.RELEASE)
            val uri = getString(R.string.team_email)+"?subject="+Uri.encode(emailTitle)+"&body="+Uri.encode(emailContent)
            intent.data = Uri.parse(uri)

            startActivity(Intent.createChooser(intent, null))
        })

        txt_profile.setOnClickListener(View.OnClickListener {
            val intent = Intent(this, MyProfileActivity::class.java)
            startActivity(intent)
        })


        txt_logout.setOnClickListener(View.OnClickListener {
            val mDialogView = LayoutInflater.from(this).inflate(R.layout.dialog_logout, null)
            val mBuilder = AlertDialog.Builder(this).setView(mDialogView)
            val builder = mBuilder.show()

            builder.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            val btn_cancle = mDialogView.findViewById<Button>(R.id.dialog_logout_btn_cancle)
            val btn_logout = mDialogView.findViewById<Button>(R.id.dialog_logout_btn_logout)

            btn_cancle.setOnClickListener(View.OnClickListener {
                builder.dismiss()
            })

            btn_logout.setOnClickListener(View.OnClickListener {
                LogOut()
                builder.dismiss()
            })
        })

        txt_wirhdrawal.setOnClickListener(View.OnClickListener {
            val intent = Intent(this, WithdrawalActivity::class.java)
            startActivity(intent)
        })
    }

    fun LogOut() {

        var loginType = viewModel.getLoginType()

        if (loginType == "google") {
            GoogleLogout()
        } else if (loginType == "kakao") {
            KakaoLogout()
        } else if (loginType == "naver") {
            NaverLogout()
        }

        viewModel.setLogoutData()

    }

    fun GoogleLogout() {
        Log.d(TAG, "google")
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.google_client_id))
            .requestEmail()
            .build()

        val mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

        mGoogleSignInClient.signOut()
            .addOnCompleteListener(this) {
                Log.d(TAG,"GoogleLogout 성공")
            }
    }

    fun KakaoLogout() {
        Log.d(TAG, "kakao")
        UserApiClient.instance.logout { error ->
            if (error != null) {
                Log.d(TAG, "KakaoLogout 실패. SDK에서 토큰 삭제됨")
            } else {
                Log.d(TAG, "KakaoLogout 성공. SDK에서 토큰 삭제됨")
            }
        }
    }

    fun NaverLogout() {
        Log.d(TAG, "naver")
        var mOAuthLoginModule = OAuthLogin.getInstance();
        mOAuthLoginModule.init(
            this,
            getString(R.string.naver_client_id),
            getString(R.string.naver_client_secret),
            getString(R.string.app_name)
            //,OAUTH_CALLBACK_INTENT
            // SDK 4.1.4 버전부터는 OAUTH_CALLBACK_INTENT변수를 사용하지 않습니다.
        );

        mOAuthLoginModule.logout(this);
    }

}