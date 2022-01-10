package com.coworkerteam.coworker.ui.camstudy.info

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.coworkerteam.coworker.data.UserRepository
import com.coworkerteam.coworker.data.model.api.MyStudyInfoResponse
import com.coworkerteam.coworker.ui.base.BaseViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import retrofit2.Response

class MyStudyInfoViewModel(private val model: UserRepository) : BaseViewModel()  {
    private val TAG = "MyStudyInfoViewModel"

    //스터디 입장전 데이터
    private val _MyStudyInfoResponseLiveData = MutableLiveData<Response<MyStudyInfoResponse>>()
    val MyStudyInfoResponseLiveData: LiveData<Response<MyStudyInfoResponse>>
        get() = _MyStudyInfoResponseLiveData

    fun getMyStudyInfoData() {
        val accessToken = model.getAccessToken()
        val nickname = model.getCurrentUserName()

        if (!accessToken.isNullOrEmpty() && !nickname.isNullOrEmpty()) {
            addDisposable(
                model.getMyStudyInfoData(accessToken, nickname)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        it.run {
                            _MyStudyInfoResponseLiveData.postValue(this)
                            Log.d(TAG, "meta : " + it.toString())
                        }
                    }, {
                        Log.d(TAG, "response error, message : ${it.message}")
                    })
            )
        }else{
            Log.d(TAG, "getMyStudyInfoData:: accessToken 또는 nickname 값이 없습니다.")
        }
    }
}