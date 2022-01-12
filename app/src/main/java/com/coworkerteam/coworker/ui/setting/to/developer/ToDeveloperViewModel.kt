package com.coworkerteam.coworker.ui.setting.to.developer

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.coworkerteam.coworker.data.UserRepository
import com.coworkerteam.coworker.data.model.api.ProfileManageResponse
import com.coworkerteam.coworker.ui.base.BaseViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import retrofit2.Response

class ToDeveloperViewModel(private val model: UserRepository) : BaseViewModel()  {
    private val TAG = "ToDeveloperViewModel"

    //내 스터디
    private val _ToDeveloperResponseLiveData = MutableLiveData<Response<ProfileManageResponse>>()
    val ToDeveloperResponseLiveData: LiveData<Response<ProfileManageResponse>>
        get() = _ToDeveloperResponseLiveData

    fun setToDeveloperData() {
        val accessToken = model.getAccessToken()
        val nickname = model.getCurrentUserName()

        if (!accessToken.isNullOrEmpty() && !nickname.isNullOrEmpty()) {
            addDisposable(
                //나중 API 완성되면 메소드 바꾸어줘야함.
                model.getProfileManageData(accessToken, nickname)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        it.run {
                            _ToDeveloperResponseLiveData.postValue(this)
                            Log.d(TAG, "meta : " + it.toString())
                        }
                    }, {
                        Log.d(TAG, "response error, message : ${it.message}")
                    })
            )
        }else{
            Log.d(TAG, "setToDeveloperData:: accessToken 또는 nickname 값이 없습니다.")
        }
    }
}