package com.coworkerteam.coworker.ui.login

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.coworkerteam.coworker.data.UserRepository
import com.coworkerteam.coworker.data.model.api.LoginResponse
import com.coworkerteam.coworker.ui.base.BaseViewModel
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.kakao.sdk.user.UserApiClient
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import retrofit2.Response

class LoginViewModel(private val model: UserRepository) : BaseViewModel() {

    val TAG = "LoginViewModel"

    //StudyDay Login Data
    private val _LoginResponseLiveData = MutableLiveData<Response<LoginResponse>>()
    val LoginResponseLiveData: LiveData<Response<LoginResponse>>
        get() = _LoginResponseLiveData

    fun getLoginData(email: String, loginType: String, imgUri: String) {
        addDisposable(
            model.getLoginData(email, loginType, imgUri)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    it.run {
                        if (this.isSuccessful) {
                            Log.d(TAG, "meta : " + it.toString())
                            val user = this.body()!!.result[0]

                            _LoginResponseLiveData.postValue(this)
                            model.setPreferencesData(
                                user.accessToken,
                                user.refreshToken,
                                user.nickname,
                                email,
                                loginType,
                                imgUri
                            )
                        }
                    }
                }, {
                    Log.d(TAG, "response error, message : ${it.message}")
                })
        )
    }

    fun getkakaoUserData(){
        // 사용자 정보 요청 (기본)
        UserApiClient.instance.me { user, error ->
            if (error != null) {
                Log.e(TAG, "사용자 정보 요청 실패", error)
            }
            else if (user != null) {
                Log.i(
                    TAG, "사용자 정보 요청 성공" +
                            "\n회원번호: ${user.id}" +
                            "\n이메일: ${user.kakaoAccount?.email}" +
                            "\n닉네임: ${user.kakaoAccount?.profile?.nickname}" +
                            "\n프로필사진: ${user.kakaoAccount?.profile?.thumbnailImageUrl}"
                )

                getLoginData(
                    user.kakaoAccount?.email.toString(),"kakao",
                    user.kakaoAccount?.profile?.thumbnailImageUrl.toString()
                )
            }
        }
    }

    fun getGoogleUserData(completedTask: Task<GoogleSignInAccount>) {
        try {
            val acct = completedTask.getResult(ApiException::class.java)
            if (acct != null) {
                val personName = acct.displayName
                val personGivenName = acct.givenName
                val personFamilyName = acct.familyName
                val personEmail = acct.email
                val personId = acct.id
                val personPhoto: Uri = acct.photoUrl
                Log.d(TAG, "handleSignInResult:personName $personName")
                Log.d(TAG, "handleSignInResult:personGivenName $personGivenName")
                Log.d(TAG, "handleSignInResult:personEmail $personEmail")
                Log.d(TAG, "handleSignInResult:personId $personId")
                Log.d(TAG, "handleSignInResult:personFamilyName $personFamilyName")
                Log.d(TAG, "handleSignInResult:personPhoto $personPhoto")
            }
        } catch (e: ApiException) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.e(TAG, "signInResult:failed code=" + e.statusCode)
        }
    }

    fun getNaverUserData(token: String) {
        val accessToken = "Bearer $token" // Bearer 다음에 공백 추가

        addDisposable(
            model.getNaverUserData(accessToken)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    it.run {
                        if (this.isSuccessful) {
                            Log.d(TAG, "meta : " + it.toString())
                            val user = this.body()!!.response

                            getLoginData(user.email,"naver",user.profile_image)
                        }
                    }
                }, {
                    Log.d(TAG, "response error, message : ${it.message}")
                })
        )
    }

}