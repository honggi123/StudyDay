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

    //내 스터디
    private val _StudyDeleteResponseLiveData = MutableLiveData<Response<ApiRequest>>()
    val StudyDeleteResponseLiveData: LiveData<Response<ApiRequest>>
        get() = _StudyDeleteResponseLiveData

    //내스터디 페이징
    private val _MyStudyManagementPagingData = model.getMyStudyManagePagingData()
        .cachedIn(viewModelScope) as MutableLiveData<PagingData<MyStudyManagePagingResponse.Result.Group>>
    val MyStudyManagementPagingData: LiveData<PagingData<MyStudyManagePagingResponse.Result.Group>>
        get() = _MyStudyManagementPagingData

    fun setDeleteStudyData(studyIdx : Int) {
        val accessToken = model.getAccessToken()

        if (!accessToken.isNullOrEmpty()) {
            addDisposable(
                model.setDeleteStudyData(accessToken, studyIdx)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        it.run {
                            Log.d(TAG, "meta : $it")

                            when {
                                it.code() == 401 -> {
                                    //액세스토큰이 만료된 경우
                                    Log.d(TAG, "액세스토큰이 만료된 경우")

                                    //액세스 토큰 재발급
                                    getReissuanceToken(TAG,model,setDeleteStudyData(studyIdx))
                                }
                                it.code() > 500 -> {
                                    //서비스 서버에 문제가 있을 경우
                                    setServiceError(TAG, it.errorBody())
                                }
                                else -> {
                                    //그 외에는 값 Activity에 전달 ( 200, 400번대의 경우 )
                                    _StudyDeleteResponseLiveData.postValue(this)
                                }
                            }
                        }
                    }, {
                        Log.d(TAG, "response error, message : ${it.message}")
                    })
            )
        }else{
            Log.d(TAG, "setDeleteStudyData:: accessToken 또는 nickname 값이 없습니다.")
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
                            Log.d(TAG, "meta : $it")

                            when {
                                it.code() == 401 -> {
                                    //액세스토큰이 만료된 경우
                                    Log.d(TAG, "액세스토큰이 만료된 경우")

                                    //액세스 토큰 재발급
                                    getReissuanceToken(TAG,model,setWithdrawStudyData(studyIdx))
                                }
                                it.code() > 500 -> {
                                    //서비스 서버에 문제가 있을 경우
                                    setServiceError(TAG, it.errorBody())
                                }
                                else -> {
                                    //그 외에는 값 Activity에 전달 ( 200, 400번대의 경우 )
                                    _StudyDeleteResponseLiveData.postValue(this)
                                }
                            }
                        }
                    }, {
                        Log.d(TAG, "response error, message : ${it.message}")
                    })
            )
        }else{
            Log.d(TAG, "setWithdrawStudyData:: accessToken 또는 nickname 값이 없습니다.")
        }
    }
}