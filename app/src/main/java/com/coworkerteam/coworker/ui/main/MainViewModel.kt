package com.coworkerteam.coworker.ui.main

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.coworkerteam.coworker.data.UserRepository
import com.coworkerteam.coworker.data.model.api.EditGoalResponse
import com.coworkerteam.coworker.data.model.api.EnterCamstudyResponse
import com.coworkerteam.coworker.data.model.api.MainResponse
import com.coworkerteam.coworker.ui.base.BaseViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import retrofit2.Response

class MainViewModel(private val model: UserRepository) : BaseViewModel() {
    private val TAG = "MainViewModel"

    //메인페이지 데이터
    private val _MainResponseLiveData = MutableLiveData<Response<MainResponse>>()
    val MainResponseLiveData: LiveData<Response<MainResponse>>
        get() = _MainResponseLiveData

    //스터디 입장전 데이터
    private val _EnterCamstudyResponseLiveData = MutableLiveData<Response<EnterCamstudyResponse>>()
    val EnterCamstudyResponseLiveData: LiveData<Response<EnterCamstudyResponse>>
        get() = _EnterCamstudyResponseLiveData

    //목표 수정 데이터
    private val _EditGoalResponseLiveData = MutableLiveData<Response<EditGoalResponse>>()
    val EditGoalResponseLiveData: LiveData<Response<EditGoalResponse>>
        get() = _EditGoalResponseLiveData

    fun getMainData() {
        val accessToken = model.getAccessToken()
        val nickname = model.getCurrentUserName()

        if (!accessToken.isNullOrEmpty() && !nickname.isNullOrEmpty()) {
            addDisposable(
                model.getMainData(accessToken, nickname)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        it.run {
                            _MainResponseLiveData.postValue(this)
                            Log.d(TAG, "meta : " + it.toString())
                        }
                    }, {
                        Log.d(TAG, "response error, message : ${it.message}")
                    })
            )
        } else {
            Log.d(TAG, "getMainData:: accessToken 또는 nickname 값이 없습니다.")
        }
    }

    fun getEnterCamstduyData(studyIdx: Int, password: String?) {
        val accessToken = model.getAccessToken()

        if (!accessToken.isNullOrEmpty()) {
            addDisposable(
                model.getEnterCamStudyData(accessToken, studyIdx, password)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        it.run {
                            if (isSuccessful) {
                                it.body()!!.result.studyInfo.idx = studyIdx
                                _EnterCamstudyResponseLiveData.postValue(it)
                            } else if (it.code() == 403) {
                                _EnterCamstudyResponseLiveData.postValue(it)
                            }
                            Log.d(TAG, "meta : " + it.toString())
                        }
                    }, {
                        Log.d(TAG, "response error, message : ${it.message}")
                    })
            )
        } else {
            Log.d(TAG, "getEnterCamstduyData:: accessToken 값이 없습니다.")
        }
    }

    fun setGoalCamstduyData(aimTime: String?, goal: String?, ddayDate: String?, ddayName: String?) {
        val accessToken = model.getAccessToken()
        val nickname = model.getCurrentUserName()

        if (!accessToken.isNullOrEmpty() && !nickname.isNullOrEmpty()) {
            addDisposable(
                model.setEditGoalData(accessToken, nickname,aimTime, goal, ddayDate, ddayName)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        it.run {
                            if (isSuccessful) {
                                _EditGoalResponseLiveData.postValue(it)
                            }
                            Log.d(TAG, "meta : " + it.toString())
                        }
                    }, {
                        Log.d(TAG, "response error, message : ${it.message}")
                    })
            )
        } else {
            Log.d(TAG, "setGoalCamstduyData:: accessToken 또는 nickname 값이 없습니다.")
        }
    }

}