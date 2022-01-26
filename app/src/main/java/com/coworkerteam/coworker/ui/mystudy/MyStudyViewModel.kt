package com.coworkerteam.coworker.ui.mystudy

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.coworkerteam.coworker.data.UserRepository
import com.coworkerteam.coworker.data.model.api.*
import com.coworkerteam.coworker.ui.base.BaseViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import retrofit2.Response

class MyStudyViewModel(private val model: UserRepository) : BaseViewModel() {
    private val TAG = "MyStudyViewModel"

    //내 스터디
    private val _MyStudyResponseLiveData = MutableLiveData<Response<MyStudyResponse>>()
    val MyStudyResponseLiveData: LiveData<Response<MyStudyResponse>>
        get() = _MyStudyResponseLiveData

    //스터디 입장전 데이터
    private val _EnterCamstudyResponseLiveData = MutableLiveData<Response<EnterCamstudyResponse>>()
    val EnterCamstudyResponseLiveData: LiveData<Response<EnterCamstudyResponse>>
        get() = _EnterCamstudyResponseLiveData

    //그룹스터디
    private val _MyStudyGroupPagingData = model.getMyStudyGroupPagingData()
        .cachedIn(viewModelScope) as MutableLiveData<PagingData<MyStudyGroupPagingResponse.Result.Group>>
    val MyStudyGroupPagingData: LiveData<PagingData<MyStudyGroupPagingResponse.Result.Group>>
        get() = _MyStudyGroupPagingData
    
    //일일스터디
    private val _MyStudyDailyPagingData = model.getMyStudyDailyPagingData()
        .cachedIn(viewModelScope) as MutableLiveData<PagingData<MyStudyDailyPagingResponse.Result.Open>>
    val MyStudyDailyPagingData: LiveData<PagingData<MyStudyDailyPagingResponse.Result.Open>>
        get() = _MyStudyDailyPagingData

    fun getMyStudyData() {
        val accessToken = model.getAccessToken()
        val nickname = model.getCurrentUserName()

        if (!accessToken.isNullOrEmpty() && !nickname.isNullOrEmpty()) {
            addDisposable(
                model.getMyStudyData(accessToken, nickname)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        it.run {
                            _MyStudyResponseLiveData.postValue(this)
                            Log.d(TAG, "meta : " + it.toString())
                        }
                    }, {
                        Log.d(TAG, "response error, message : ${it.message}")
                    })
            )
        } else {
            Log.d(TAG, "getMyStudyData:: accessToken 또는 nickname 값이 없습니다.")
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

    fun getLoginType():String?{
        return model.getCurrentUserLoggedInMode()
    }

    fun getUserProfile():String?{
        return model.getCurrentUserProfilePicUrl()
    }

    fun getUserNickname():String?{
        return model.getCurrentUserName()
    }
}