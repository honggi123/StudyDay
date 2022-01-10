package com.coworkerteam.coworker.ui.camstudy.enter

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.coworkerteam.coworker.data.UserRepository
import com.coworkerteam.coworker.data.model.api.CamStudyJoinResponse
import com.coworkerteam.coworker.ui.base.BaseViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import retrofit2.Response

class EnterCamstudyViewModel(private val model: UserRepository) : BaseViewModel() {
    private val TAG = "EnterCamstudyViewModel"

    private val _EnterCamstudyResponseLiveData = MutableLiveData<Response<CamStudyJoinResponse>>()
    val EnterCamstudyResponseLiveData: LiveData<Response<CamStudyJoinResponse>>
        get() = _EnterCamstudyResponseLiveData

    fun getCamstduyJoinData(studyIdx: Int) {
        val accessToken = model.getAccessToken()

        if (!accessToken.isNullOrEmpty()) {
            addDisposable(
                model.getCamStudyJoinData(accessToken, studyIdx)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        it.run {
                            _EnterCamstudyResponseLiveData.postValue(this)
                            Log.d(TAG, "meta : " + it.toString())
                        }
                    }, {
                        Log.d(TAG, "response error, message : ${it.message}")
                    })
            )
        }else{
            Log.d(TAG, "getCamstduyJoinData:: accessToken 값이 없습니다.")
        }
    }
}