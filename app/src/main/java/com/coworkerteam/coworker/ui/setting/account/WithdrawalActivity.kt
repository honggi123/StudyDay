package com.coworkerteam.coworker.ui.setting.account

import android.content.Context
import android.widget.Toast

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.coworkerteam.coworker.R
import com.coworkerteam.coworker.data.local.prefs.AppPreferencesHelper
import com.coworkerteam.coworker.data.model.api.ApiRequest
import com.coworkerteam.coworker.data.remote.StudydayService
import com.coworkerteam.coworker.databinding.ActivityCategoryBinding
import com.coworkerteam.coworker.databinding.ActivityWithdrawalBinding
import com.coworkerteam.coworker.ui.base.BaseActivity
import com.coworkerteam.coworker.ui.category.CategoryViewModel
import com.coworkerteam.coworker.ui.login.LoginActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.textfield.TextInputLayout
import com.kakao.sdk.user.UserApiClient
import com.nhn.android.naverlogin.OAuthLogin
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
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


class WithdrawalActivity : BaseActivity<ActivityWithdrawalBinding, WithdrawalViewModel>() {

    val TAG = "WithdrawalActivity"
    override val layoutResourceID: Int
        get() = R.layout.activity_withdrawal
    override val viewModel: WithdrawalViewModel by viewModel()

    var reason = ""
    var is_other = false
    val content = this

    override fun initStartView() {
        init()
    }

    override fun initDataBinding() {
        viewModel.WithdrawalResponseLiveData.observe(this, androidx.lifecycle.Observer {
            if (it.isSuccessful) {
                Toast.makeText(getApplicationContext(), "회원탈퇴가 안전하게 완료되었습니다.", Toast.LENGTH_SHORT)
                    .show()
                removeLogin()
            }
        })
    }

    override fun initAfterBinding() {
    }

    fun init() {
        val txt_reason = findViewById<TextView>(R.id.withdrawal_txt_reason)
        val btn_withdrawal = findViewById<Button>(R.id.withdrawal_btn_withdrawal)
        val edt_reasonOther = findViewById<TextInputLayout>(R.id.withdrawal_edt_reason_other)

        txt_reason.setOnClickListener(View.OnClickListener {
            val mDialogView = LayoutInflater.from(this).inflate(R.layout.dialog_withdrawal, null)
            val mBuilder = AlertDialog.Builder(this).setView(mDialogView)
            val builder = mBuilder.show()

            builder.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            val radiogroup = mDialogView.findViewById<RadioGroup>(R.id.dialog_withdrawal_radiogroup)

            radiogroup.setOnCheckedChangeListener(RadioGroup.OnCheckedChangeListener { group, checkedId ->
                val rb = mDialogView.findViewById<RadioButton>(checkedId)
                reason = rb.text.toString()
                txt_reason.text = reason

                if (reason == "기타") {
                    edt_reasonOther.visibility = View.VISIBLE
                    is_other = true
                }else{
                    edt_reasonOther.visibility = View.GONE
                    is_other = false
                }

                builder.dismiss()
            })
        })

        btn_withdrawal.setOnClickListener(View.OnClickListener {
            if (reason != "") {
                if (is_other) {
                    if (edt_reasonOther.editText!!.text.toString() == "") {
                        Toast.makeText(
                            getApplicationContext(),
                            "회원탈퇴 이유를 적어주세요.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    withdrawal()
                }
            } else {
                Toast.makeText(getApplicationContext(), "회원탈퇴 이유를 선택해주세요.", Toast.LENGTH_SHORT)
                    .show()
            }
        })

    }


    fun withdrawal() {
        var loginType = viewModel.getLoginTypeData()

        if (loginType == "google") {
            Log.d(TAG, "google")
            withdrawalGoogle()

        } else if (loginType == "kakao") {
            Log.d(TAG, "kakao")
            withdrawalKakao()

        } else if (loginType == "naver") {
            Log.d(TAG, "naver")
            withdrawalNaver()
        }

        viewModel.setWithdrawalData(reason)
    }

    fun withdrawalGoogle() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()

        val mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

        mGoogleSignInClient.revokeAccess()
            .addOnCompleteListener(this) {
                Log.d(TAG, "연결끊기 성공")
            }
    }

    fun withdrawalKakao() {
        // 연결 끊기
        UserApiClient.instance.unlink { error ->
            if (error != null) {
                Log.e(TAG, "연결 끊기 실패", error)
            } else {
                Log.i(TAG, "연결 끊기 성공. SDK에서 토큰 삭제 됨")
            }
        }
    }

    fun withdrawalNaver() {
        CoroutineScope(Dispatchers.IO).async {
            var mOAuthLoginModule = OAuthLogin.getInstance();
            mOAuthLoginModule.init(
                content,
                getString(R.string.naver_client_id),
                getString(R.string.naver_client_secret),
                getString(R.string.app_name)
                //,OAUTH_CALLBACK_INTENT
                // SDK 4.1.4 버전부터는 OAUTH_CALLBACK_INTENT변수를 사용하지 않습니다.
            );

            val isSuccessDeleteToken: Boolean = mOAuthLoginModule.logoutAndDeleteToken(content)


            if (!isSuccessDeleteToken) {
                // 서버에서 토큰 삭제에 실패했어도 클라이언트에 있는 토큰은 삭제되어 로그아웃된 상태입니다.
                // 클라이언트에 토큰 정보가 없기 때문에 추가로 처리할 수 있는 작업은 없습니다.
                Log.d(TAG, "errorCode:" + mOAuthLoginModule.getLastErrorCode(content))
                Log.d(TAG, "errorDesc:" + mOAuthLoginModule.getLastErrorDesc(content))
            }
        }
    }

    fun removeLogin() {
        val intent = Intent(content, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }


}