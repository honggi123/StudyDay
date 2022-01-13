package com.coworkerteam.coworker.ui.login

import android.app.Activity
import android.content.Intent
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import androidx.activity.result.contract.ActivityResultContracts
import com.coworkerteam.coworker.R
import com.coworkerteam.coworker.databinding.ActivityLoginBinding
import com.coworkerteam.coworker.ui.base.BaseActivity
import com.coworkerteam.coworker.ui.category.CategoryActivity
import com.coworkerteam.coworker.ui.main.MainActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.kakao.sdk.user.UserApiClient
import com.nhn.android.naverlogin.OAuthLogin
import com.nhn.android.naverlogin.OAuthLoginHandler
import org.koin.androidx.viewmodel.ext.android.viewModel


class LoginActivity : BaseActivity<ActivityLoginBinding, LoginViewModel>() {

    override val layoutResourceID: Int
        get() = R.layout.activity_login
    override val viewModel: LoginViewModel by viewModel()

    val GOOGLE_SIGN_IN = 1004

    val TAG = "LoginActivity"

    val googleForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                viewModel.getGoogleUserData(task)
                val acct = task.getResult(ApiException::class.java)
                viewModel.getLoginData(acct.email, "google", acct.photoUrl.toString())
            }
        }

    override fun initStartView() {
        viewDataBinding.activitiy = this
    }

    override fun initDataBinding() {
        viewModel.LoginResponseLiveData.observe(this, androidx.lifecycle.Observer {
            val isCategory = it.body()!!.result[0].isInterest
            moveActivity(isCategory)
        })
    }

    override fun initAfterBinding() {
    }

    fun googleLogin() {
        // 앱에 필요한 사용자 데이터를 요청하도록 로그인 옵션을 설정한다.
        // DEFAULT_SIGN_IN parameter는 유저의 ID와 기본적인 프로필 정보를 요청하는데 사용된다.
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.google_client_id))
            .requestEmail() // email addresses도 요청함
            .build()

        // 위에서 만든 GoogleSignInOptions을 사용해 GoogleSignInClient 객체를 만듬
        val mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

        val signInIntent: Intent = mGoogleSignInClient.getSignInIntent()
        googleForResult.launch(signInIntent)
//        startActivityForResult(signInIntent, GOOGLE_SIGN_IN)
    }

    fun kakaoLogin() {
        // 로그인 공통 callback 구성
        UserApiClient.instance.loginWithKakaoAccount(this) { token, error ->
            if (error != null) {
                Log.e(TAG, "KaKao 로그인 실패", error)
            } else if (token != null) {
                Log.i(TAG, "KaKao 로그인 성공 ${token.accessToken}")
                viewModel.getkakaoUserData()
            }
        }
    }

    fun naverLogin() {
        var mOAuthLoginModule = OAuthLogin.getInstance();
        mOAuthLoginModule.init(
            this,
            getString(R.string.naver_client_id),
            getString(R.string.naver_client_secret),
            getString(R.string.app_name)
            //,OAUTH_CALLBACK_INTENT
            // SDK 4.1.4 버전부터는 OAUTH_CALLBACK_INTENT변수를 사용하지 않습니다.
        )

        val mOAuthLoginHandler: OAuthLoginHandler = object : OAuthLoginHandler() {
            override fun run(success: Boolean) {
                if (success) {
                    val accessToken = mOAuthLoginModule.getAccessToken(this@LoginActivity)
                    viewModel.getNaverUserData(accessToken)
                } else {
                    val errorCode = mOAuthLoginModule.getLastErrorCode(this@LoginActivity).code
                    val errorDesc = mOAuthLoginModule.getLastErrorDesc(this@LoginActivity)
                    Log.d(
                        TAG, "errorCode:" + errorCode
                                + ", errorDesc:" + errorDesc
                    )
                }
            }
        }

        mOAuthLoginModule.startOauthLoginActivity(this, mOAuthLoginHandler);

    }

    fun moveActivity(is_categoty: Boolean) {
        if (is_categoty) {
            var intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            var intent = Intent(this, CategoryActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

}