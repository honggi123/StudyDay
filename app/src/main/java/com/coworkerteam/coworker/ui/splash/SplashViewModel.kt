package com.coworkerteam.coworker.ui.splash

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.coworkerteam.coworker.data.UserRepository
import com.coworkerteam.coworker.data.model.api.AutoLoginResponse
import com.coworkerteam.coworker.ui.base.BaseViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import retrofit2.Response

class SplashViewModel(private val model: UserRepository) : BaseViewModel() {

    private val TAG = "SplashViewModel"

    //스터디 입장전 데이터
    private val _AutoLoginResponseLiveData = MutableLiveData<Response<AutoLoginResponse>>()
    val AutoLoginResponseLiveData: LiveData<Response<AutoLoginResponse>>
        get() = _AutoLoginResponseLiveData

    fun getAutoLoginData() {
        val refreshToken = model.getRefreshToken()
        val nickname = model.getCurrentUserName()

        if (!refreshToken.isNullOrEmpty() && !nickname.isNullOrEmpty()) {
            addDisposable(
                model.getAutoLoginData(refreshToken, nickname)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        it.run {
                            if (isSuccessful) {
                                //새롭게 갱신된 accessToken과 refreshToken 저장
                                model.setAccessToken(this.body()!!.result.accessToken)
                                model.setRefreshToken(this.body()!!.result.refreshToken)
                            }

                            _AutoLoginResponseLiveData.postValue(this)
                            Log.d(TAG, "meta : " + it.toString())
                        }
                    }, {
                        Log.d(TAG, "response error, message : ${it.message}")
                    })
            )
        }else{
            Log.d(TAG, "getAutoLoginData:: refreshToken 또는 nickname 값이 없습니다.")
        }
    }

    fun getRefreshToken(): String? {
        return model.getRefreshToken()
    }
}