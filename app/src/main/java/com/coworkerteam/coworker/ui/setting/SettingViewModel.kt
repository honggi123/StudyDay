package com.coworkerteam.coworker.ui.setting

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.coworkerteam.coworker.data.UserRepository
import com.coworkerteam.coworker.data.model.api.ApiRequest
import com.coworkerteam.coworker.data.model.api.ProfileManageResponse
import com.coworkerteam.coworker.ui.base.BaseViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import retrofit2.Response

class SettingViewModel(private val model: UserRepository) : BaseViewModel() {
    private val TAG = "SettingViewModel"

    //내 스터디
    private val _SettingResponseLiveData = MutableLiveData<Response<ApiRequest>>()
    val SettingResponseLiveData: LiveData<Response<ApiRequest>>
        get() = _SettingResponseLiveData

    fun setLogoutData() {
        val refreshToken = model.getRefreshToken()
        val nickname = model.getCurrentUserName()

        if (!refreshToken.isNullOrEmpty() && !nickname.isNullOrEmpty()) {
            addDisposable(
                model.setLogoutData(refreshToken, nickname)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        it.run {
                            Log.d(TAG, "meta : $it")

                            if (isSuccessful || it.code() == 401 || it.code() == 404) {
                                //로그아웃에 성공하거나, 리프레시 토큰 만료 및 존재안할경우, 회원이 아닐경우에도 로그아웃 시킴 => 다 로그인 유지되면 문제가 되는것들이라
                                // 로컬에 저장되어있던 정보들 삭제
                                model.deletePreferencesData()
                            }

                            when {
                                it.code() > 500 -> {
                                    //서비스 서버에 문제가 있을 경우
                                    setServiceError(TAG,it.errorBody())
                                }
                                else -> {
                                    //그 외에는 값 Activity에 전달 ( 200, 400번대의 경우 )
                                    _SettingResponseLiveData.postValue(this)
                                }
                            }
                        }
                    }, {
                        Log.d(TAG, "response error, message : ${it.message}")
                    })
            )
        } else {
            Log.d(TAG, "setLogoutData:: accessToken 또는 nickname 값이 없습니다.")
        }
    }

    fun getLoginType(): String? {
        return model.getCurrentUserProfilePicUrl()
    }
}