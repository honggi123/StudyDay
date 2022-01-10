package com.coworkerteam.coworker.ui.setting.profile

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.coworkerteam.coworker.data.UserRepository
import com.coworkerteam.coworker.data.model.api.MyStudyResponse
import com.coworkerteam.coworker.data.model.api.ProfileManageResponse
import com.coworkerteam.coworker.ui.base.BaseViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import retrofit2.Response

class MyProfileViewModel(private val model: UserRepository) : BaseViewModel()  {
    private val TAG = "MyProfileViewModel"

    //내 스터디
    private val _MyProfileResponseLiveData = MutableLiveData<Response<ProfileManageResponse>>()
    val MyProfileResponseLiveData: LiveData<Response<ProfileManageResponse>>
        get() = _MyProfileResponseLiveData

    fun getMyProfileData() {
        val accessToken = model.getAccessToken()
        val nickname = model.getCurrentUserName()

        if (!accessToken.isNullOrEmpty() && !nickname.isNullOrEmpty()) {
            addDisposable(
                model.getProfileManageData(accessToken, nickname)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        it.run {
                            _MyProfileResponseLiveData.postValue(this)
                            Log.d(TAG, "meta : " + it.toString())
                        }
                    }, {
                        Log.d(TAG, "response error, message : ${it.message}")
                    })
            )
        }else{
            Log.d(TAG, "getMyProfileData:: accessToken 또는 nickname 값이 없습니다.")
        }
    }
}