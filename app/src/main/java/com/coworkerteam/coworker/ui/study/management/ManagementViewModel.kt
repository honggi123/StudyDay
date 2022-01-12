package com.coworkerteam.coworker.ui.study.management

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

class ManagementViewModel(private val model: UserRepository) : BaseViewModel()  {
    private val TAG = "ManagementViewModel"

//    //내 스터디
//    private val _ManagementResponseLiveData = MutableLiveData<Response<MyStudyManageResponse>>()
//    val ManagementResponseLiveData: LiveData<Response<MyStudyManageResponse>>
//        get() = _ManagementResponseLiveData

    //내 스터디
    private val _ApiResponseLiveData = MutableLiveData<Response<ApiRequest>>()
    val ApiResponseLiveData: LiveData<Response<ApiRequest>>
        get() = _ApiResponseLiveData

    //내스터디 페이징
    private val _MyStudyManagementPagingData = model.getMyStudyManagePagingData()
        .cachedIn(viewModelScope) as MutableLiveData<PagingData<MyStudyManagePagingResponse.Result.Group>>
    val MyStudyManagementPagingData: LiveData<PagingData<MyStudyManagePagingResponse.Result.Group>>
        get() = _MyStudyManagementPagingData

//    fun getManagementData() {
//        val accessToken = model.getAccessToken()
//        val nickname = model.getCurrentUserName()
//
//        if (!accessToken.isNullOrEmpty() && !nickname.isNullOrEmpty()) {
//            addDisposable(
//                model.getMyStudyManageData(accessToken, nickname)
//                    .subscribeOn(Schedulers.io())
//                    .observeOn(AndroidSchedulers.mainThread())
//                    .subscribe({
//                        it.run {
//                            _ManagementResponseLiveData.postValue(this)
//                            Log.d(TAG, "meta : " + it.toString())
//                        }
//                    }, {
//                        Log.d(TAG, "response error, message : ${it.message}")
//                    })
//            )
//        }else{
//            Log.d(TAG, "getManagementData:: accessToken 또는 nickname 값이 없습니다.")
//        }
//    }

    fun setDeleteStudyData(studyIdx : Int) {
        val accessToken = model.getAccessToken()

        if (!accessToken.isNullOrEmpty()) {
            addDisposable(
                model.setDeleteStudyData(accessToken, studyIdx)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        it.run {
                            _ApiResponseLiveData.postValue(this)
                            Log.d(TAG, "meta : " + it.toString())
                        }
                    }, {
                        Log.d(TAG, "response error, message : ${it.message}")
                    })
            )
        }else{
            Log.d(TAG, "getManagementData:: accessToken 또는 nickname 값이 없습니다.")
        }
    }

    fun setWithdrawStudyData(studyIdx : Int) {
        val accessToken = model.getAccessToken()

        if (!accessToken.isNullOrEmpty()) {
            addDisposable(
                model.setWithdrawStudyData(accessToken, studyIdx)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        it.run {
                            _ApiResponseLiveData.postValue(this)
                            Log.d(TAG, "meta : " + it.toString())
                        }
                    }, {
                        Log.d(TAG, "response error, message : ${it.message}")
                    })
            )
        }else{
            Log.d(TAG, "getManagementData:: accessToken 또는 nickname 값이 없습니다.")
        }
    }
}