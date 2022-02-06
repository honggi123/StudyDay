package com.coworkerteam.coworker.ui.setting.account

import android.widget.Toast

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import com.coworkerteam.coworker.R
import com.coworkerteam.coworker.databinding.ActivityWithdrawalBinding
import com.coworkerteam.coworker.ui.base.BaseActivity
import com.coworkerteam.coworker.ui.login.LoginActivity
import com.coworkerteam.coworker.utils.PatternUtils
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.textfield.TextInputLayout
import com.kakao.sdk.user.UserApiClient
import com.nhn.android.naverlogin.OAuthLogin
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import org.json.JSONObject
import org.koin.androidx.viewmodel.ext.android.viewModel


class WithdrawalActivity : BaseActivity<ActivityWithdrawalBinding, WithdrawalViewModel>() {
    private val TAG = "WithdrawalActivity"

    override val layoutResourceID: Int
        get() = R.layout.activity_withdrawal
    override val viewModel: WithdrawalViewModel by viewModel()

    var reason = ""
    var is_other = false
    val content = this

    override fun initStartView() {
        viewDataBinding.activity = this

        setSupportActionBar(viewDataBinding.mainToolber as androidx.appcompat.widget.Toolbar) // 툴바를 액티비티의 앱바로 지정
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // 드로어를 꺼낼 홈 버튼 활성화
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_ios_new_24) // 홈버튼 이미지 변경
        supportActionBar?.title = "회원탈퇴"
    }

    override fun initDataBinding() {
        viewModel.WithdrawalResponseLiveData.observe(this, androidx.lifecycle.Observer {
            when {
                it.isSuccessful -> {
                    Toast.makeText(this, "회원탈퇴가 안전하게 완료되었습니다.", Toast.LENGTH_SHORT).show()
                    moveLogin()
                }
                it.code() == 400 -> {
                    //회원탈퇴에 실패한 원인이 클라이언트 측의 값 입력, 전송에 문제가 있을 경우
                    val errorMessage = JSONObject(it.errorBody()?.string())
                    Log.e(TAG, errorMessage.getString("message"))

                    //400번대 에러로 로그인이 실패했을 경우, 사용자에게 알려준다.
                    Toast.makeText(this,"탈퇴사유를 올바르게 선택 또는 입력해주세요.",Toast.LENGTH_SHORT).show()
                }
                it.code() == 403 ->{
                    //회원탈퇴에 실패한 원인이 운영중인 스터디가 존재하는 경우
                    val errorMessage = JSONObject(it.errorBody()?.string())
                    Log.e(TAG, errorMessage.getString("message"))

                    //403번대 에러로 로그인이 실패했을 경우, 사용자에게 알려준다.
                    Toast.makeText(this,"현재 운영중인 스터디가 존재합니다. 정리 후 다시 시도해주세요.",Toast.LENGTH_SHORT).show()
                }
                it.code() == 404 -> {
                    //존재하지 않는 회원, 리프레시 토큰 실종 시 로그인 화면으로 이동
                    val errorMessage = JSONObject(it.errorBody()?.string())
                    Log.e(TAG, errorMessage.getString("message"))

                    moveLogin()
                }
            }
        })
    }

    override fun initAfterBinding() {
    }

    fun onClickWithdrawal() {
            val mDialogView = LayoutInflater.from(this).inflate(R.layout.dialog_withdrawal, null)
            val mBuilder = AlertDialog.Builder(this).setView(mDialogView)
            val builder = mBuilder.show()

            builder.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            val radiogroup = mDialogView.findViewById<RadioGroup>(R.id.dialog_withdrawal_radiogroup)

            radiogroup.setOnCheckedChangeListener(RadioGroup.OnCheckedChangeListener { group, checkedId ->
                val rb = mDialogView.findViewById<RadioButton>(checkedId)
                reason = rb.text.toString()
                viewDataBinding.withdrawalTxtReason.text = reason

                if (reason == "기타") {
                    viewDataBinding.withdrawalEdtReasonOther.visibility = View.VISIBLE
                    is_other = true
                    viewDataBinding.withdrawalBtnWithdrawal.isEnabled = viewDataBinding.withdrawalEdtReasonOther.isErrorEnabled
                }else{
                    viewDataBinding.withdrawalEdtReasonOther.visibility = View.GONE
                    is_other = false
                    viewDataBinding.withdrawalBtnWithdrawal.isEnabled = true
                }

                builder.dismiss()
            })
    }

    fun changTextOther(s: CharSequence, start: Int, before: Int, count: Int) {
        val result = PatternUtils.matcheWithdrawal(s.toString())
        Log.d(TAG,s.toString())

        if (result.isNotError) {
            viewDataBinding.withdrawalEdtReasonOther.isErrorEnabled = false
            viewDataBinding.withdrawalEdtReasonOther.error = null
            viewDataBinding.withdrawalBtnWithdrawal.isEnabled = true
        } else {
            viewDataBinding.withdrawalEdtReasonOther.error = result.ErrorMessge
            viewDataBinding.withdrawalBtnWithdrawal.isEnabled = false
        }
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

        firebaseLog.addLog(TAG,"withdrawal")
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

}