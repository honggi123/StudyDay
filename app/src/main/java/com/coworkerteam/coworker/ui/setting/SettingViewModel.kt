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
                            if(isSuccessful) {
                                model.deletePreferencesData()
                                _SettingResponseLiveData.postValue(this)
                                Log.d(TAG, "meta : " + it.toString())
                            }
                        }
                    }, {
                        Log.d(TAG, "response error, message : ${it.message}")
                    })
            )
        } else {
            Log.d(TAG, "getMyProfileData:: accessToken 또는 nickname 값이 없습니다.")
        }
    }

    fun getLoginType(): String? {
        return model.getCurrentUserProfilePicUrl()
    }
}